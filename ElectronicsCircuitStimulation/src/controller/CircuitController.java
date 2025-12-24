package controller;

import model.*;
import model.CircuitModel.ConnectionMode;
import view.CircuitPanel;
import view.ToolboxView.Tool;
import components.*;
import utils.*;
import java.awt.*;
import java.awt.event.*;

public class CircuitController {
    private final CircuitModel model;
    private final CircuitPanel view;
    private final CircuitValidator validator;
    private Components dragging = null;
    private Point dragOffset = null;
    private ConnectionMode gameMode = CircuitModel.ConnectionMode.BULBSERIES;
    
    public CircuitController(CircuitModel model, CircuitPanel view) {
        this.model = model;
        this.view = view;
        this.validator = new CircuitValidator(model);
        
        ActionListener modeListener = _ -> {
            if (view.rbSeriesBulb.isSelected()) {
                this.gameMode = ConnectionMode.BULBSERIES;
            } else {
                this.gameMode = ConnectionMode.BULBPARALLEL;
            }
            updateCircuit(); 
        };

        view.rbSeriesBulb.addActionListener(modeListener);
        view.rbParallelBulb.addActionListener(modeListener);        
        // Setup Terminals
        int midY = view.board.y + view.board.height / 2;
        model.termLeft.setPosition(view.board.x, midY);
        model.termRight.setPosition(view.board.x + view.board.width, midY);
        view.repaint();

        // Listeners
        view.seriesBtn.addActionListener(_ -> connectSelected(CompositeComponent.Mode.SERIES));
        view.parallelBtn.addActionListener(_ -> connectSelected(CompositeComponent.Mode.PARALLEL));
        view.undoBtn.addActionListener(_ -> undo());

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                
                Tool t = view.toolboxView.hitTool(p);

                // --- CASE 1: Power Source Tool (Updates Global Settings) ---
                if (t == Tool.POWER_SOURCE) {
                    double[] vals = view.toolboxView.promptPower();
                    if (vals == null) return; 

                    view.toolboxView.updatePower(vals[0], vals[1]);
                    model.circuit.setPowerSupply(vals[0], vals[1]);

                    updateCircuit();
                    view.repaint();
                    return; 
                }
                
                // --- CASE 2: Component Tools (Creates new parts) ---
                if (t != null) {
                    Components newComponent = null;
                    int spawnX = view.board.x + view.board.width / 2;
                    int spawnY = view.board.y + view.board.height / 2;
                    
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
                        model.circuit.addComponent(newComponent);
                        model.addActionToUndoStack(new CircuitAction(CircuitAction.ActionType.ADD_COMPONENT, newComponent));
                        updateCircuit();
                        view.repaint();
                    }
                    return;
                }

                // --- CASE 3: Clicking on Existing Components ---
                Components hit = model.componentAt(p);
                if (hit != null) {
                	if (hit instanceof BoardTerminal) {
                        // Terminals are fixed - only allow selection, no dragging
                        handleSelection(hit, e.isControlDown());
                        view.paintImmediately(view.getBounds());
                        return; 
                    }
                    
                    // Dragging logic - only allow dragging if click is within the board
                    if (view.board.contains(p)) {
                        dragging = hit;
                        dragOffset = new Point(p.x - hit.getPosition().x, p.y - hit.getPosition().y);
                        handleSelection(hit, e.isControlDown());
                        
                        updateCircuit();
                        view.paintImmediately(view.getBounds());
                    }
                    return;
                }
                
                // --- CASE 4: Clicking Empty Board (Deselect) ---
                if (view.board.contains(p)) {
                    clearSelection();
                    updateCircuit();
                    view.repaint();
                }
            }
            
            private void handleSelection(Components hit, boolean isCtrl) {
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

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging == null) return;
                
                Point p = e.getPoint();
                
                // First, clamp the mouse position itself to the board boundaries
                int clampedX = Math.max(view.board.x, Math.min(view.board.x + view.board.width, p.x));
                int clampedY = Math.max(view.board.y, Math.min(view.board.y + view.board.height, p.y));
                
                Rectangle bounds = dragging.getBounds();
                if (bounds == null) bounds = new Rectangle(0,0,40,40);

                int halfWidth = bounds.width / 2;
                int halfHeight = bounds.height / 2;
                int offsetX = (dragOffset != null) ? dragOffset.x : 0;
                int offsetY = (dragOffset != null) ? dragOffset.y : 0;

                int nx = clampedX - offsetX;
                int ny = clampedY - offsetY;

                // Ensure the component stays fully within board boundaries
                int minX = view.board.x + halfWidth;
                int maxX = view.board.x + view.board.width - halfWidth;
                int minY = view.board.y + halfHeight;
                int maxY = view.board.y + view.board.height - halfHeight;

                nx = Math.max(minX, Math.min(maxX, nx));
                ny = Math.max(minY, Math.min(maxY, ny));

                dragging.setPosition(nx, ny);
                updateCircuit();
                view.paintImmediately(view.getBounds());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragging != null) {
                    // Component is guaranteed to be within board boundaries due to drag constraints
                    dragging = null;
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
        try {
            Components c1 = model.firstSelected;
            Components c2 = model.secondSelected;
			
            validator.validateConnectionRequest(c1, c2, mode);

            clearSelection();

            model.connectComponent(c1, c2, mode);

            if (mode == CompositeComponent.Mode.PARALLEL) {
                view.insLabel.setText("Parallel connection created.");
            } else {
                view.insLabel.setText("Series connection created.");
            }

            updateCircuit();
            view.paintImmediately(view.getBounds());

        } catch (CircuitValidationException e) {
            view.insLabel.setText(e.getMessage());
        }
    }

    private void updateCircuit() {
        double freq = model.circuit.getSourceFrequency();

        // 1. Validation Logic
        int left = validator.getConnectionCount(model.termLeft);
        int right = validator.getConnectionCount(model.termRight);
        
        if (left == 0 || right == 0) {
            view.statsLabel.setText("Circuit incomplete: Connect both terminals");
            updateComponentDetailsLabel(freq);
            
            // Reset state for visual feedback
            for (Components c : model.circuit.getComponents()) {
                if (!(c instanceof BoardTerminal)) {
                    c.setSimulationState(0, 0, 0);
                }
            }
            view.repaint();
            return;
        }
        
        // 2. Run Physics Engine (Model)
        model.updatePhysics(gameMode);
        
        // 3. Update UI
        view.statsLabel.setText(model.getSimulationStatus());
        updateComponentDetailsLabel(freq);
        view.repaint();
    }
    
    private void updateComponentDetailsLabel(double sourceFrequency) {
        String info = "Selected: ";
        if (model.firstSelected != null) {
            // Call the static method
            info += utils.FormatUtils.formatComponentInfo(model.firstSelected, sourceFrequency);
        } else {
            info += "None";
        }
        view.valueLabel.setText(info);
    }
    
    private void clearSelection() {
        for (Components c : model.circuit.getComponents()) c.setSelected(false);
        model.firstSelected = null;
        model.secondSelected = null;
    }
}
