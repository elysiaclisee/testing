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

        // Attach Listeners
        view.seriesBtn.addActionListener(e -> connectSelected(Wire.Type.SERIES));
        view.parallelBtn.addActionListener(e -> connectSelected(Wire.Type.PARALLEL));
        view.undoBtn.addActionListener(e -> undo());

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                view.putClientProperty("pressPoint", p);
                
                Toolbox.Tool t = view.toolboxView.hitTool(p);
                if (t != null) {
                    double[] inputs = {};
                    
                    switch (t) {
                        case RESISTOR:
                            Double r = view.toolboxView.promptForValue("Resistor", "Ohms");
                            if (r == null) return; // User cancelled
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
                        case POWER_SOURCE:
                            double[] pSrc = view.toolboxView.promptForPowerSource();
                            if (pSrc == null) return;
                            inputs = pSrc;
                            break;
                        case BULB:
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
                Components hit = componentAt(p);
                if (hit != null) {
                    model.dragging = hit;
                    model.dragOffset = new Point(p.x - hit.getPosition().x, p.y - hit.getPosition().y);
                    if (e.isControlDown()) {
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
                    updateCircuit();
                    view.repaint();
                    return;
                }
                if (view.boardRect.contains(p)) {
                    clearSelection();
                    updateCircuit();
                    view.repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (model.dragging != null) {
                    Point p = e.getPoint();
                    int nx = p.x - (model.dragOffset != null ? model.dragOffset.x : 0);
                    int ny = p.y - (model.dragOffset != null ? model.dragOffset.y : 0);
                    nx = Math.max(view.boardRect.x + 10, Math.min(view.boardRect.x + view.boardRect.width - 10, nx));
                    ny = Math.max(view.boardRect.y + 10, Math.min(view.boardRect.y + view.boardRect.height - 10, ny));
                    model.dragging.setPosition(nx, ny);
                    view.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Point p = e.getPoint();
                if (model.dragging != null) {
                    Point pressPoint = (Point) view.getClientProperty("pressPoint");
                    if (!view.boardRect.contains(p)) {
                        model.circuit.removeComponent(model.dragging);
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

    private void connectSelected(Wire.Type type) {
        if (model.firstSelected == null || model.secondSelected == null) {
            view.instructionLabel.setText("Select two components first.");
            return;
        }
        saveState();
        model.wires.add(new Wire(model.firstSelected, model.secondSelected, type));
        CompositeComponent.Mode mode = (type == Wire.Type.SERIES) ? CompositeComponent.Mode.SERIES : CompositeComponent.Mode.PARALLEL;
        model.circuit.connect(model.firstSelected, model.secondSelected, mode);
        updateCircuit();
        clearSelection();
        view.repaint();
    }

   private void updateCircuit() {
        double voltage = 5.0;
        for (Components c : model.circuit.getComponents()) {
            if (c instanceof PowerSource) {
                voltage = ((PowerSource) c).getVoltage();
                break;
            }
        }
        model.circuit.updateBulbStates(voltage);
        double req = model.circuit.getRoot() != null ? model.circuit.getRoot().getResistanceOhms() : 0.0;
        double current = Connections.current(voltage, req);

        // REPLACED: Simple %.2f formatting instead of formatMetric
        view.circuitStatsLabel.setText(String.format("Total: V = %.2f V, I = %.2f A, Req = %.2f Ω",
                voltage, current, req));

        if (model.firstSelected != null) {
            String details = getComponentDetails(model.firstSelected);
            if (model.secondSelected != null) {
                details += " + " + getComponentDetails(model.secondSelected);
            }
            view.componentValuesLabel.setText("Selected: " + details);
        } else {
            view.componentValuesLabel.setText("Selected: None");
        }
    }

    private String getComponentDetails(Components c) {
        // REPLACED: Standard string formatting
        if (c instanceof Resistor) return String.format("%.2f Ω", c.getResistanceOhms());
        if (c instanceof Capacitor) return String.format("%.2f F", ((Capacitor) c).getCapacitance());
        if (c instanceof Inductor) return String.format("%.2f H", ((Inductor) c).getInductance());
        if (c instanceof PowerSource) {
            PowerSource b = (PowerSource) c;
            return String.format("%.2f V / %.2f Hz", b.getVoltage(), b.getFrequency());
        }
        if (c instanceof Bulb) return String.format("Bulb (%.2f Ω)", c.getResistanceOhms());
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