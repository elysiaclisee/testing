package controller;

import model.CircuitMemento;
import model.CircuitModel;
import view.CircuitPanel;
import components.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class CircuitController {
    private final CircuitModel model;
    private final CircuitPanel view;
    private CircuitManager.ConnectionMode gameMode = CircuitManager.ConnectionMode.SERIES_WITH_BULB;
    
    public CircuitController(CircuitModel model, CircuitPanel view) {
        this.model = model;
        this.view = view;
        
     // THÊM: Listener cho việc đổi chế độ Game
        ActionListener modeListener = e -> {
            if (view.rbSeriesBulb.isSelected()) {
                this.gameMode = CircuitManager.ConnectionMode.SERIES_WITH_BULB;
            } else {
                this.gameMode = CircuitManager.ConnectionMode.PARALLEL_WITH_BULB;
            }
            updateCircuit(); 
        };

        view.rbSeriesBulb.addActionListener(modeListener);
        view.rbParallelBulb.addActionListener(modeListener);        
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

             // --- CASE 1: Power Source Tool ---
                if (t == Toolbox.Tool.POWER_SOURCE) {
                    double[] vals = view.toolboxView.promptForPowerSource();
                    if (vals == null) return; 

                    // VALIDATION: Voltage và Frequency phải > 0
                    if (vals[0] <= 0 || vals[1] <= 0) {
                        javax.swing.JOptionPane.showMessageDialog(null, "Voltage và Frequency phải lớn hơn 0!");
                        return;
                    }

                    view.toolboxView.updatePowerSourceDisplay(vals[0], vals[1]);
                    model.circuit.setPowerSupply(vals[0], vals[1]);
                    updateCircuit();
                    view.repaint();
                    return; 
                }

                switch (t) {
                    case RESISTOR:
                        Double r = view.toolboxView.promptForValue("Resistor", "Ohms");
                        if (r == null || r <= 0) return; // Thêm check > 0
					break;
                    case CAPACITOR:
                        Double c = view.toolboxView.promptForValue("Capacitor", "F");
                        if (c == null || c <= 0) return; // Thêm check > 0
					break;
                    case INDUCTOR:
                        Double l = view.toolboxView.promptForValue("Inductor", "H");
                        if (l == null || l <= 0) return; // Thêm check > 0
					break;
                    case BULB:
                        break;
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
        
        if (mode == CompositeComponent.Mode.PARALLEL) {
            if (getConnectionCount(c1) > 0 || getConnectionCount(c2) > 0) {
                view.instructionLabel.setText("Cannot group: Components are already connected in series.");
                return;
            }
        }
        
        if (areConnected(c1, c2)) {
            view.instructionLabel.setText("Already connected.");
            return;
        }
        
        saveState();
        
        if (mode == CompositeComponent.Mode.PARALLEL) {
            model.circuit.connect(c1, c2, mode);
        } else {
            model.wires.add(new Wire(c1, c2, Wire.Type.SERIES));
            model.circuit.connect(c1, c2, mode);
        }
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

    private void updateCircuit() {
        // 1. Get power supply settings from circuit model
        double sourceVoltage = model.circuit.getSourceVoltage();
        double sourceFrequency = model.circuit.getSourceFrequency();
        
        // 2. Check if both terminals are connected
        int leftConnections = getConnectionCount(model.termLeft);
        int rightConnections = getConnectionCount(model.termRight);
        
        if (leftConnections == 0 || rightConnections == 0) {
            view.circuitStatsLabel.setText("Circuit incomplete: Connect both terminals");
            view.componentValuesLabel.setText("Selected: None");
            // Reset all component states
            for (Components c : model.circuit.getComponents()) {
                if (!(c instanceof BoardTerminal)) {
                    c.setSimulationState(0, 0, 0);
                }
            }
            view.repaint();
            return;
        }
        
        // 3. Check if power source is configured
        if (sourceVoltage <= 0) {
            view.circuitStatsLabel.setText("Configure Power Source first (click battery icon)");
            view.repaint();
            return;
        }

     // 4. Tìm linh kiện Bulb và cụm linh kiện người dùng
        Bulb targetBulb = null;
        for (Components c : model.circuit.getComponents()) {
            if (c instanceof Bulb) { targetBulb = (Bulb) c; break; }
        }

        CompositeComponent root = model.circuit.getRoot();
        if (root == null || targetBulb == null) {
            view.circuitStatsLabel.setText("Circuit incomplete: Add components and a bulb");
            view.repaint();
            return;
        }

        // 5. GỌI SIMULATOR ĐỂ TÍNH TOÁN VÀ XÉT TRẠNG THÁI ĐÈN
        CircuitManager.simulate(new PowerSource("src", 0, 0, sourceVoltage, sourceFrequency), root, targetBulb, gameMode);

        // 6. Tính toán Z total (Magnitude) để hiển thị
        Complex zUser = root.getImpedance(sourceFrequency);
        Complex zBulb = targetBulb.getImpedance(sourceFrequency);
        Complex zTotalComplex = (gameMode == CircuitManager.ConnectionMode.SERIES_WITH_BULB) 
                                ? zUser.add(zBulb) : Connections.parallel(zUser, zBulb);

        double totalZ = zTotalComplex.getMagnitude();
        double totalI = (totalZ > 0) ? sourceVoltage / totalZ : 0.0;

        // 7. Update UI với nhãn mới và ký hiệu Ω
        view.circuitStatsLabel.setText(String.format("V source: %.2fV | Z total: %.2fΩ | i total: %.2fA",
                sourceVoltage, totalZ, totalI));
    }

    private String getComponentDetails(Components c, double freq) {
        // Gọi .getMagnitude() vì getImpedance hiện tại trả về Complex
        if (c instanceof Resistor) return String.format("R: %.2f Ω", c.getResistance());
        if (c instanceof Capacitor) return String.format("C: %.2e F (Z: %.2f Ω)", ((Capacitor) c).getCapacitance(), c.getImpedance(freq).getMagnitude());
        if (c instanceof Inductor) return String.format("L: %.2e H (Z: %.2f Ω)", ((Inductor) c).getInductance(), c.getImpedance(freq).getMagnitude());
        if (c instanceof PowerSource) {
            PowerSource b = (PowerSource) c;
            return String.format("%.2f V / %.2f Hz", b.getVoltage(), b.getFrequency());
        }
        if (c instanceof Bulb) return String.format("Bulb: 50W | R: %.2f Ω", c.getResistance());
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