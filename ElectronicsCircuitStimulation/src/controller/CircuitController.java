package controller;

import model.CircuitAction;
import model.CircuitModel;
import view.CircuitPanel;
import view.ToolboxView.Tool;
import components.*;
import utils.Complex;
import utils.Connections;
import utils.Wire;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class CircuitController {
	public enum ConnectionMode {
	    SERIES_WITH_BULB,
	    PARALLEL_WITH_BULB
	}
    private final CircuitModel model;
    private final CircuitPanel view;
    private ConnectionMode gameMode = ConnectionMode.SERIES_WITH_BULB;
    
    public CircuitController(CircuitModel model, CircuitPanel view) {
        this.model = model;
        this.view = view;
        
     // THÊM: Listener cho việc đổi chế độ Game
        ActionListener modeListener = _ -> {
            if (view.rbSeriesBulb.isSelected()) {
                this.gameMode = ConnectionMode.SERIES_WITH_BULB;
            } else {
                this.gameMode = ConnectionMode.PARALLEL_WITH_BULB;
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
        view.seriesBtn.addActionListener(_ -> connectSelected(CompositeComponent.Mode.SERIES));
        view.parallelBtn.addActionListener(_ -> connectSelected(CompositeComponent.Mode.PARALLEL));
        view.undoBtn.addActionListener(_ -> undo());

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                view.putClientProperty("pressPoint", p);
                
                Tool t = view.toolboxView.hitTool(p);

                // --- CASE 1: Power Source Tool (Updates Global Settings) ---
                if (t == Tool.POWER_SOURCE) {
                    double[] vals = view.toolboxView.promptForPowerSource();
                    if (vals == null) return; 

                    view.toolboxView.updatePowerSourceDisplay(vals[0], vals[1]);
                    model.circuit.setPowerSupply(vals[0], vals[1]);

                    updateCircuit();
                    view.repaint();
                    return; 
                }
                
                // --- CASE 2: Component Tools (Creates new parts) ---
                if (t != null) {
                    Components newComponent = null;
                    int spawnX = view.boardRect.x + view.boardRect.width / 2;
                    int spawnY = view.boardRect.y + view.boardRect.height / 2;
                    
                    switch (t) {
                        case RESISTOR:
                            Double r = view.toolboxView.promptForValue("Resistor", "Ohms");
                            if (r == null || r <= 0) return;
                            newComponent = new Resistor("R" + System.currentTimeMillis(), spawnX, spawnY, r);
                            break;
                        case CAPACITOR:
                            Double c = view.toolboxView.promptForValue("Capacitor", "F");
                            if (c == null || c <= 0) return;
                            newComponent = new Capacitor("C" + System.currentTimeMillis(), spawnX, spawnY, c);
                            break;
                        case INDUCTOR:
                            Double l = view.toolboxView.promptForValue("Inductor", "H");
                            if (l == null || l <= 0) return;
                            newComponent = new Inductor("L" + System.currentTimeMillis(), spawnX, spawnY, l);
                            break;
                        case BULB:
                            break;
                        default:
                            break;
                    }
                    
                    if (newComponent != null) {
                        model.addComponent(newComponent);
                        updateCircuit();
                        view.repaint();
                    }
                    return;
                }

                // --- CASE 3: Clicking on Existing Components ---
                Components hit = componentAt(p);
                if (hit != null) {
                	if (hit instanceof BoardTerminal) {
                        // Terminals are fixed - only allow selection, no dragging
                        handleSelection(hit, e.isControlDown());
                        view.paintImmediately(view.getBounds());
                        return; 
                    }
                    
                    // Dragging logic - only allow dragging if click is within the board
                    if (view.boardRect.contains(p)) {
                        model.dragging = hit;
                        model.dragOffset = new Point(p.x - hit.getPosition().x, p.y - hit.getPosition().y);
                        handleSelection(hit, e.isControlDown());
                        
                        updateCircuit();
                        view.paintImmediately(view.getBounds());
                    }
                    return;
                }
                
                // --- CASE 4: Clicking Empty Board (Deselect) ---
                if (view.boardRect.contains(p)) {
                    clearSelection();
                    updateCircuit();
                    view.repaint();
                }
            }
            
            // Helper to clean up selection logic - allows selecting up to 2 components without Ctrl
            private void handleSelection(Components hit, boolean isCtrl) {
                // If Ctrl is held, allow toggle behavior (for advanced users)
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
                    // NEW BEHAVIOR: Without Ctrl, allow selecting up to 2 components sequentially
                    if (hit.isSelected()) {
                        // Clicking on already selected component deselects it
                        hit.setSelected(false);
                        if (model.firstSelected == hit) model.firstSelected = null;
                        if (model.secondSelected == hit) model.secondSelected = null;
                    } else if (model.firstSelected == null) {
                        // No selection yet - select as first
                        hit.setSelected(true);
                        model.firstSelected = hit;
                    } else if (model.secondSelected == null) {
                        // First is selected, select this as second
                        hit.setSelected(true);
                        model.secondSelected = hit;
                    } else {
                        // Both already selected - clear all and start over with this one
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
                
                // First, clamp the mouse position itself to the board boundaries
                int clampedX = Math.max(view.boardRect.x, Math.min(view.boardRect.x + view.boardRect.width, p.x));
                int clampedY = Math.max(view.boardRect.y, Math.min(view.boardRect.y + view.boardRect.height, p.y));
                
                Rectangle bounds = model.dragging.getBounds();
                if (bounds == null) bounds = new Rectangle(0,0,40,40);

                int halfWidth = bounds.width / 2;
                int halfHeight = bounds.height / 2;
                int offsetX = (model.dragOffset != null) ? model.dragOffset.x : 0;
                int offsetY = (model.dragOffset != null) ? model.dragOffset.y : 0;

                int nx = clampedX - offsetX;
                int ny = clampedY - offsetY;

                // Ensure the component stays fully within board boundaries
                int minX = view.boardRect.x + halfWidth;
                int maxX = view.boardRect.x + view.boardRect.width - halfWidth;
                int minY = view.boardRect.y + halfHeight;
                int maxY = view.boardRect.y + view.boardRect.height - halfHeight;

                nx = Math.max(minX, Math.min(maxX, nx));
                ny = Math.max(minY, Math.min(maxY, ny));

                model.dragging.setPosition(nx, ny);
                updateCircuit();
                view.paintImmediately(view.getBounds());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (model.dragging != null) {
                    // Component is guaranteed to be within board boundaries due to drag constraints
                    model.dragging = null;
                    updateCircuit();
                    view.paintImmediately(view.getBounds());
                }
            }
        };
        view.addMouseListener(ma);
        view.addMouseMotionListener(ma);
    }

    private void undo() {
        model.undo();
        updateCircuit();
        view.repaint();
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
        	if (mode == CompositeComponent.Mode.PARALLEL) {
        	    view.instructionLabel.setText("Board terminals only support SERIES connections.");
        	    return;
        	}
        }
        
        if (isTerm1 && getConnectionCount(c1) >= 1) {
            view.instructionLabel.setText("Terminal " + c1.getId() + " is already occupied.");
            return;
        }
        if (isTerm2 && getConnectionCount(c2) >= 1) {
            view.instructionLabel.setText("Terminal " + c2.getId() + " is already occupied.");
            return;
        }
        
        // Validate connection capacity for both components
        String c1Error = validateConnectionCapacity(c1);
        if (c1Error != null) {
            view.instructionLabel.setText(c1Error);
            return;
        }
        
        String c2Error = validateConnectionCapacity(c2);
        if (c2Error != null) {
            view.instructionLabel.setText(c2Error);
            return;
        }
        
        if (mode == CompositeComponent.Mode.PARALLEL) {
            if (getConnectionCount(c1) > 0 || getConnectionCount(c2) > 0) {
                view.instructionLabel.setText("Cannot group: Components are already connected in series.");
                return;
            }
        }
        
     // Check limit for C1 (if it is a Parallel Box)
        if (c1 instanceof CompositeComponent) {
            CompositeComponent comp1 = (CompositeComponent) c1;
            if (comp1.getMode() == CompositeComponent.Mode.PARALLEL) {
                // Check how many wires are already on the side C2 is trying to connect to
                int connectionsOnSide = getConnectionCountOnSide(comp1, c2);
                if (connectionsOnSide >= 1) {
                    view.instructionLabel.setText("Parallel box connection point is full (max 1 per side).");
                    return;
                }
            }
        }
        
        // Check limit for C2 (if it is a Parallel Box)
        if (c2 instanceof CompositeComponent) {
            CompositeComponent comp2 = (CompositeComponent) c2;
            if (comp2.getMode() == CompositeComponent.Mode.PARALLEL) {
                // Check how many wires are already on the side C1 is trying to connect to
                int connectionsOnSide = getConnectionCountOnSide(comp2, c1);
                if (connectionsOnSide >= 1) {
                    view.instructionLabel.setText("Parallel box connection point is full (max 1 per side).");
                    return;
                }
            }
        }
        
        if (areConnected(c1, c2)) {
            view.instructionLabel.setText("Already connected.");
            return;
        }
        
        // Clear selection BEFORE making the connection to avoid referencing removed components
        clearSelection();
        
        if (mode == CompositeComponent.Mode.PARALLEL) {
            // For parallel connections, create a composite group box (no wire line)
            // The circuit.connect() method will remove c1 and c2, and add a new CompositeComponent
            model.circuit.connect(c1, c2, mode);
            
            // Find the newly created composite group to record the action
            CompositeComponent newGroup = null;
            for (Components comp : model.circuit.getComponents()) {
                if (comp instanceof CompositeComponent) {
                    CompositeComponent cc = (CompositeComponent) comp;
                    if (cc.getMode() == CompositeComponent.Mode.PARALLEL && 
                        cc.getChildren().contains(c1) && cc.getChildren().contains(c2)) {
                        newGroup = cc;
                        break;
                    }
                }
            }
            
            // Record the action as adding the new composite component
            if (newGroup != null) {
                model.addActionToUndoStack(new CircuitAction(CircuitAction.ActionType.ADD_COMPONENT, newGroup));
            }
            view.instructionLabel.setText("Parallel group created.");
        } else {
            // For series connections, use addWire which records the action automatically
            model.addWire(c1, c2, Wire.Type.SERIES);
            view.instructionLabel.setText("Series connection created.");
        }
        
        // Update circuit calculations and force immediate synchronous repaint
        updateCircuit();
        view.paintImmediately(view.getBounds());
    }
    
    private int getConnectionCount(Components c) {
        int count = 0;
        for (Wire w : model.wires) {
            String id = c.getId();
            if (w.getA().getId().equals(id) || w.getB().getId().equals(id)) {
                count++;
            }
        }
        return count;
    }

    private boolean areConnected(Components c1, Components c2) {
        String id1 = c1.getId();
        String id2 = c2.getId();
        
        for (Wire w : model.wires) {
            String wa = w.getA().getId();
            String wb = w.getB().getId();

            // FIX: Check IDs to survive Undo/Redo object swapping
            if ((wa.equals(id1) && wb.equals(id2)) || 
                (wa.equals(id2) && wb.equals(id1))) {
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
            // Still show selected component details even when circuit is incomplete
            updateComponentDetailsLabel(sourceFrequency);
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
            // Still show selected component details
            updateComponentDetailsLabel(sourceFrequency);
            view.repaint();
            return;
        }

     // 4. Get root circuit and calculate based on gameMode
        CompositeComponent root = model.circuit.getRoot();
        if (root == null) {
            view.circuitStatsLabel.setText("Circuit incomplete");
            // Still show selected component details
            updateComponentDetailsLabel(sourceFrequency);
            view.repaint();
            return;
        }

        // 5. TÍNH TOÁN CÔNG SUẤT DỰA TRÊN GAMEMODE (Virtual Bulb)
        Complex zUser = root.getImpedance(sourceFrequency);
        
        // Use bulb properties from Bulb class (220V, 50W -> R = 968Ω)
        Complex zBulb = new Complex(Bulb.R_BULB, 0);
        
        double totalZ, totalI, iBulb, pBulb;
        
        if (gameMode == ConnectionMode.SERIES_WITH_BULB) {
            // Nối tiếp: I_bulb = I_total
            Complex zTotalComplex = zUser.add(zBulb);
            totalZ = zTotalComplex.getMagnitude();
            totalI = (totalZ > 0) ? sourceVoltage / totalZ : 0.0;
            iBulb = totalI;
            pBulb = iBulb * iBulb * Bulb.R_BULB;
        } else {
            // Song song: V_bulb = V_source
            Complex zTotalComplex = Connections.parallel(zUser, zBulb);
            totalZ = zTotalComplex.getMagnitude();
            totalI = (totalZ > 0) ? sourceVoltage / totalZ : 0.0;
            iBulb = sourceVoltage / zBulb.getMagnitude();
            pBulb = iBulb * iBulb * Bulb.R_BULB;
        }
        
        // 6. XÉT TRẠNG THÁI SÁNG DỰA TRÊN SO SÁNH VỚI P_RATED
        double powerRatio = pBulb / Bulb.P_RATED;
        String status;
        
        if (powerRatio > 1.5) {
            status = "BURNT";
        } else if (powerRatio >= 0.8) {
            status = "BRIGHT";
        } else if (powerRatio >= 0.2) {
            status = "WEAK";
        } else {
            status = "OFF";
        }
        
        // Cập nhật trạng thái mô phỏng cho root
        root.setSimulationState(sourceVoltage, totalI, sourceFrequency);
        
        // Update any physical bulb on board if exists (optional visual feedback)
        for (Components c : model.circuit.getComponents()) {
            if (c instanceof Bulb) {
                Bulb bulb = (Bulb) c;
                bulb.setLighted(powerRatio >= 0.2 && powerRatio <= 1.5);
                bulb.setSimulationState(iBulb * Bulb.R_BULB, iBulb, sourceFrequency);
                break;
            }
        }
        
        // Format status with color using HTML
        String formattedStatus = status;
        if (status.equals("BURNT")) {
            formattedStatus = "<font color='red'><b>BURNT</b></font>";
        } else if (status.equals("BRIGHT")) {
            formattedStatus = "<font color='green'><b>BRIGHT</b></font>";
        } else if (status.equals("WEAK")) {
            formattedStatus = "<font color='orange'><b>WEAK</b></font>";
        } else if (status.equals("OFF")) {
            formattedStatus = "<font color='gray'><b>OFF</b></font>";
        }
        
        view.circuitStatsLabel.setText(String.format("<html>V: %.2fV | Z: %.2fΩ | I: %.2fA | Bulb: %.2fW/%.0fW [%s]</html>",
                sourceVoltage, totalZ, totalI, pBulb, Bulb.P_RATED, formattedStatus));
        
        // 8. Update component details when selected
        updateComponentDetailsLabel(sourceFrequency);
    }

    private String getComponentDetails(Components c, double freq) {
        // Gọi .getMagnitude() vì getImpedance hiện tại trả về Complex
        if (c instanceof Resistor) return String.format("R: %.2f Ω", c.getResistance());
        if (c instanceof Capacitor) return String.format("C: %.2e F (Z: %.2f Ω)", ((Capacitor) c).getCapacitance(), c.getImpedance(freq).getMagnitude());
        if (c instanceof Inductor) return String.format("L: %.2e H (Z: %.2f Ω)", ((Inductor) c).getInductance(), c.getImpedance(freq).getMagnitude());
        return c.getId();
    }
    private void clearSelection() {
        for (Components c : model.circuit.getComponents()) c.setSelected(false);
        model.firstSelected = null;
        model.secondSelected = null;
    }
    
    /**
     * Check if a component can accept more connections.
     * Returns error message if full, null if available.
     */
    private String validateConnectionCapacity(Components c) {
        int maxConnections = (c instanceof BoardTerminal) ? 1 : 2;
        int currentConnections = getConnectionCount(c);
        
        if (currentConnections >= maxConnections) {
            if (c instanceof BoardTerminal) {
                return "Terminal " + c.getId() + " is already occupied.";
            } else {
                return "Component " + c.getId() + " is full (max 2).";
            }
        }
        return null;
    }

    private Components componentAt(Point p) {
        List<Components> components = model.circuit.getComponents();
        for (int i = components.size() - 1; i >= 0; i--) {
            Components c = components.get(i);
            if (c.contains(p)) return c;
        }
        return null;
    }
    
    /**
     * Update the component details label based on current selection.
     * Centralizes the update logic to follow DRY principle.
     */
    private void updateComponentDetailsLabel(double sourceFrequency) {
        if (model.firstSelected != null) {
            view.componentValuesLabel.setText("Selected: " + getComponentDetails(model.firstSelected, sourceFrequency));
        } else {
            view.componentValuesLabel.setText("Selected: None");
        }
    }
    
private int getConnectionCountOnSide(CompositeComponent parallelBox, Components otherComponent) {
    if (parallelBox.getMode() != CompositeComponent.Mode.PARALLEL) {
        return 0;
    }

    // 1. Calculate the exact positions of the Left and Right connection dots
    // We replicate the logic from CompositeComponent.draw() to be 100% sure
    int minX = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    
    List<Components> children = parallelBox.getChildren();
    if (children.isEmpty()) {
        minX = parallelBox.getPosition().x - 20;
        maxX = parallelBox.getPosition().x + 20;
    } else {
        for (Components c : children) {
            Rectangle b = c.getBounds();
            if (b.x < minX) minX = b.x;
            if (b.x + b.width > maxX) maxX = b.x + b.width;
        }
    }
    
    int padding = 30;
    int railLeftX = minX - padding;
    int railRightX = maxX + padding;
    
    // 2. Determine which side the NEW component is targeting
    // We compare distances: Is it closer to the Left Rail or Right Rail?
    int targetX = otherComponent.getPosition().x;
    boolean targettingLeft = Math.abs(targetX - railLeftX) < Math.abs(targetX - railRightX);

    int count = 0;
    
    // 3. Check existing connections
    for (Wire w : model.wires) {
        Components connected = null;
        
        // Use ID comparison to be safe against Undo/Redo cloning issues
        if (w.getA().getId().equals(parallelBox.getId())) {
            connected = w.getB();
        } else if (w.getB().getId().equals(parallelBox.getId())) {
            connected = w.getA();
        }
        
        if (connected != null) {
            // Check which side this EXISTING wire is connected to
            int existingX = connected.getPosition().x;
            boolean existingIsOnLeft = Math.abs(existingX - railLeftX) < Math.abs(existingX - railRightX);
            
            // If they are targeting the same side, increment count
            if (existingIsOnLeft == targettingLeft) {
                count++;
            }
        }
    }
    return count;
}
}