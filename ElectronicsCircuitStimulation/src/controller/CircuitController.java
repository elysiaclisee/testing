package controller;

import model.CircuitMemento;
import model.CircuitModel;
import view.CircuitPanel;
import components.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class CircuitController {
    private final CircuitModel model;
    private final CircuitPanel view;

    public CircuitController(CircuitModel model, CircuitPanel view) {
        this.model = model;
        this.view = view;
        
        // Setup Terminals
        int midY = view.boardRect.y + view.boardRect.height / 2;
        model.termLeft.setPosition(view.boardRect.x, midY);
        model.termRight.setPosition(view.boardRect.x + view.boardRect.width, midY);
        view.repaint();

        // Listeners
        view.seriesBtn.addActionListener(e -> connectSelected(CompositeComponent.Mode.SERIES));
        view.parallelBtn.addActionListener(e -> connectSelected(CompositeComponent.Mode.PARALLEL));
        view.undoBtn.addActionListener(e -> undo());

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                view.putClientProperty("pressPoint", p);
                
                Toolbox.Tool t = view.toolboxView.hitTool(p);

                if (t != null) {
                    // --- CASE 1: Power Source Tool (Updates Global Settings) ---
                    if (t == Toolbox.Tool.POWER_SOURCE) {
                        double[] vals = view.toolboxView.promptForPowerSource();
                        if (vals == null) return; 

                        view.toolboxView.updatePowerSourceDisplay(vals[0], vals[1]);
                        model.circuit.setPowerSupply(vals[0], vals[1]);

                        updateCircuit();
                        view.repaint();
                        return; 
                    }
                    
                    // --- CASE 2: Component Tools (Creates new parts) ---
                    double[] inputs = {};
                    switch (t) {
                        case RESISTOR:
                            Double r = view.toolboxView.promptForValue("Resistor", "Ohms");
                            if (r == null) return; 
                            inputs = new double[]{r};
                            break;
                        case CAPACITOR:
                            Double c = view.toolboxView.promptForValue("Capacitor", "F");
                            if (c == null) return;
                            inputs = new double[]{c};
                            break;
                        case INDUCTOR:
                            Double l = view.toolboxView.promptForValue("Inductor", "H");
                            if (l == null) return;
                            inputs = new double[]{l};
                            break;
                        case BULB:
                        	for (Components comp : model.circuit.getComponents()) {
                                if (comp instanceof Bulb) return; // Limit 1 bulb
                        	}
                            break;
                    }
                    saveState();
                    
                    Components c = Toolbox.create(t, 
                        view.boardRect.x + view.boardRect.width / 2, 
                        view.boardRect.y + 40, 
                        inputs);
                    
                    if (c == null) return;

                    model.circuit.addComponent(c);                   
                    clearSelection();
                    c.setSelected(true);
                    model.firstSelected = c;
                    
                    updateCircuit();
                    view.repaint();
                    return;
                }

                // --- CASE 3: Clicking on Existing Components ---
                Components hit = componentAt(p);
                if (hit != null) {
                	if (hit instanceof BoardTerminal) {
                        // Toggle selection for terminals
                        handleSelection(hit, e.isControlDown());
                        view.repaint();
                        return; 
                    }
                    
                    // Dragging logic
                    model.dragging = hit;
                    model.dragOffset = new Point(p.x - hit.getPosition().x, p.y - hit.getPosition().y);
                    handleSelection(hit, e.isControlDown());
                    
                    updateCircuit();
                    view.repaint();
                    return;
                }
                
                // --- CASE 4: Clicking Empty Board (Deselect) ---
                if (view.boardRect.contains(p)) {
                    clearSelection();
                    updateCircuit();
                    view.repaint();
                }
            }
            
            // Helper to clean up selection logic
            private void handleSelection(Components hit, boolean isCtrl) {
                if (isCtrl) {
                    if (hit.isSelected()) {
                        hit.setSelected(false);
                        if (model.firstSelected == hit) model.firstSelected = null;
                        if (model.secondSelected == hit) model.secondSelected = null;
                    } else {
                        hit.setSelected(true);
                        if (model.firstSelected == null) model.firstSelected = hit;
                        else if (model.secondSelected == null) model.secondSelected = hit;
                    }
                } else {
                    if (!hit.isSelected()) {
                        clearSelection();
                        hit.setSelected(true);
                        model.firstSelected = hit;
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (model.dragging == null) return;
                Point p = e.getPoint();
                Rectangle bounds = model.dragging.getBounds();
                if (bounds == null) bounds = new Rectangle(0,0,40,40);

                int halfWidth = bounds.width / 2;
                int halfHeight = bounds.height / 2;
                int offsetX = (model.dragOffset != null) ? model.dragOffset.x : 0;
                int offsetY = (model.dragOffset != null) ? model.dragOffset.y : 0;

                int nx = p.x - offsetX;
                int ny = p.y - offsetY;

                // Clamping to board boundaries
                int minX = view.boardRect.x + halfWidth;
                int maxX = view.boardRect.x + view.boardRect.width - halfWidth;
                int minY = view.boardRect.y + halfHeight;
                int maxY = view.boardRect.y + view.boardRect.height - halfHeight;

                nx = Math.max(minX, Math.min(maxX, nx));
                ny = Math.max(minY, Math.min(maxY, ny));

                model.dragging.setPosition(nx, ny);
                updateCircuit();
                view.repaint();                  
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Point p = e.getPoint();
                if (model.dragging != null) {
                    // Delete if dropped outside
                    if (!view.boardRect.contains(p)) {
                        model.circuit.removeComponent(model.dragging);
                        // Also re-enable bulb tool if we deleted a bulb
                        if(model.dragging instanceof Bulb) {
                             // view.toolboxView.setToolEnabled(Toolbox.Tool.BULB, true); // Uncomment if implemented
                        }
                    }
                    model.dragging = null;
                    updateCircuit();
                    view.repaint();
                }
            }
        };
        view.addMouseListener(ma);
        view.addMouseMotionListener(ma);
    }

    private void undo() {
        if (model.undoStack.isEmpty()) return;
        CircuitMemento memento = model.undoStack.remove(model.undoStack.size() - 1);
        model.redoStack.add(new CircuitMemento(model.circuit, model.wires));
        restoreState(memento);
        view.repaint();
    }

    private void saveState() {
        model.redoStack.clear();
        model.undoStack.add(new CircuitMemento(model.circuit, model.wires));
    }

    private void restoreState(CircuitMemento memento) {
        model.circuit.setComponents(memento.getComponents());
        model.wires.clear();
        model.wires.addAll(memento.getWires());
        updateCircuit();
    }

    // --- CONNECT LOGIC ---
    private void connectSelected(CompositeComponent.Mode mode) {
        if (model.firstSelected == null || model.secondSelected == null) {
            view.instructionLabel.setText("Select two components first.");
            return;
        }

        Components c1 = model.firstSelected;
        Components c2 = model.secondSelected;

        boolean isTerm1 = (c1 instanceof BoardTerminal);
        boolean isTerm2 = (c2 instanceof BoardTerminal);
        boolean involveTerminal = isTerm1 || isTerm2;

        if (involveTerminal) {
            // FIX: Use 'mode' instead of 'type'
            if (mode == CompositeComponent.Mode.PARALLEL) {
                view.instructionLabel.setText("Board terminals only support SERIES connections.");
                return;
            }

            if (isTerm1 && getConnectionCount(c1) >= 1) {
                view.instructionLabel.setText("Terminal " + c1.getId() + " is already occupied.");
                return;
            }
            if (isTerm2 && getConnectionCount(c2) >= 1) {
                view.instructionLabel.setText("Terminal " + c2.getId() + " is already occupied.");
                return;
            }
            
            if (!isTerm1 && getConnectionCount(c1) >= 2) {
                view.instructionLabel.setText("Component " + c1.getId() + " is full (max 2).");
                return;
            }
            if (!isTerm2 && getConnectionCount(c2) >= 2) {
                view.instructionLabel.setText("Component " + c2.getId() + " is full (max 2).");
                return;
            }

        } else {
            if (getConnectionCount(c1) >= 2) {
                view.instructionLabel.setText("Component " + c1.getId() + " full.");
                return;
            }
            if (getConnectionCount(c2) >= 2) {
                view.instructionLabel.setText("Component " + c2.getId() + " full.");
                return;
            }
        }
        
        if (areConnected(c1, c2)) {
            view.instructionLabel.setText("Already connected.");
            return;
        }
        
        saveState();
        
        // FIX: Convert Mode to Wire.Type locally
        Wire.Type wireType = (mode == CompositeComponent.Mode.SERIES) ? Wire.Type.SERIES : Wire.Type.PARALLEL;
        model.wires.add(new Wire(c1, c2, wireType));
        
        model.circuit.connect(c1, c2, mode);
        
        updateCircuit();
        clearSelection();
        view.repaint();
        view.instructionLabel.setText("Connected successfully.");
    }
    
    private int getConnectionCount(Components c) {
        int count = 0;
        for (Wire w : model.wires) {
            if (w.getA() == c || w.getB() == c) {
                count++;
            }
        }
        return count;
    }

    private boolean areConnected(Components c1, Components c2) {
        for (Wire w : model.wires) {
            if ((w.getA() == c1 && w.getB() == c2) || 
                (w.getA() == c2 && w.getB() == c1)) {
                return true;
            }
        }
        return false;
    }

    // --- PHYSICS ENGINE & UI UPDATE ---
    private void updateCircuit() {
        double voltage = model.circuit.getSourceVoltage();
        double freq = model.circuit.getSourceFrequency();
        CompositeComponent root = model.circuit.getRoot();

        // 1. If empty, just show source info
        if (root == null || model.circuit.getComponents().isEmpty()) {
            view.circuitStatsLabel.setText(String.format("Source: %.1fV %.1fHz (Empty)", voltage, freq));
            return;
        }

        // 2. Step 1: Calculate Total Impedance (Bottom-Up)
        double zTotal = root.getImpedance(freq);
        double iTotal;

        // 3. Calculate Total Current (Ohm's Law: I = V / Z)
        if (Double.isInfinite(zTotal)) {
            iTotal = 0.0; // Open circuit
        } else if (Math.abs(zTotal) < 1e-9) {
            iTotal = 0.0; // Short Circuit protection
        } else {
            iTotal = voltage / zTotal;
        }

        // 4. Step 2: Distribute Physics to Components (Top-Down)
        root.setSimulationState(voltage, iTotal, freq);
        
        // 5. Step 3: Check Bulb State (The "Light Up" Logic)
        for (Components c : model.circuit.getComponents()) {
            if (c instanceof Bulb) {
                Bulb b = (Bulb) c;
                // Calculate real power dissipated: P = I^2 * R
                double current = b.getCurrentFlow();
                double power = current * current * b.getResistance();
                
                // Logic: Light up if powered, turn off if blown
                boolean isPowered = power > 0.1; // Threshold to prevent flickering
                boolean isBlown = power > b.getPowerLimit();
                
                if (isBlown) {
                   // System.out.println("Bulb blew out!"); // Optional debug
                   b.setLighted(false);
                } else {
                   b.setLighted(isPowered);
                }
            }
        }

        // 6. Update Stats Label
        view.circuitStatsLabel.setText(String.format("Source: %.1fV %.1fHz | Total Z: %.2fΩ | I: %.2fA",
                voltage, freq, zTotal, iTotal));

        // 7. Update Selection Label
        if (model.firstSelected != null) {
            String details = getComponentDetails(model.firstSelected, freq);
            if (model.secondSelected != null) {
                details += " + " + getComponentDetails(model.secondSelected, freq);
            }
            view.componentValuesLabel.setText("Selected: " + details);
        } else {
            view.componentValuesLabel.setText("Selected: None");
        }
        
        view.repaint();
    }

    // FIX: Added 'freq' parameter to show live Impedance
    private String getComponentDetails(Components c, double freq) {
        if (c instanceof Resistor) return String.format("R: %.2f Ω", c.getResistance());
        if (c instanceof Capacitor) return String.format("C: %.2e F (Z: %.2f Ω)", ((Capacitor) c).getCapacitance(), c.getImpedance(freq));
        if (c instanceof Inductor) return String.format("L: %.2e H (Z: %.2f Ω)", ((Inductor) c).getInductance(), c.getImpedance(freq));
        if (c instanceof PowerSource) {
            PowerSource b = (PowerSource) c;
            return String.format("%.2f V / %.2f Hz", b.getVoltage(), b.getFrequency());
        }
        if (c instanceof Bulb) return String.format("Bulb (%.2f V)", c.getVoltageDrop());
        return c.getId();
    }

    private void clearSelection() {
        for (Components c : model.circuit.getComponents()) c.setSelected(false);
        model.firstSelected = null;
        model.secondSelected = null;
    }

    private Components componentAt(Point p) {
        List<Components> components = model.circuit.getComponents();
        for (int i = components.size() - 1; i >= 0; i--) {
            Components c = components.get(i);
            if (c.contains(p)) return c;
        }
        return null;
    }
}