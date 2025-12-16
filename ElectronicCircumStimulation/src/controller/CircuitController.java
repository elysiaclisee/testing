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

        // ===== Button Listeners =====
        view.seriesBtn.addActionListener(e -> connectSelected(Wire.Type.SERIES));
        view.parallelBtn.addActionListener(e -> connectSelected(Wire.Type.PARALLEL));
        view.undoBtn.addActionListener(e -> undo());

        MouseAdapter ma = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                view.putClientProperty("pressPoint", p);

                // ===== TOOLBOX CLICK =====
                Toolbox.Tool t = view.toolboxView.hitTool(p);
                if (t != null) {
                    double[] inputs = {};

                    switch (t) {
                        case RESISTOR -> {
                            Double r = view.toolboxView.promptForValue("Resistor", "Ohms");
                            if (r == null) return;
                            inputs = new double[]{r};
                        }
                        case CAPACITOR -> {
                            Double c = view.toolboxView.promptForValue("Capacitor", "F");
                            if (c == null) return;
                            inputs = new double[]{c};
                        }
                        case INDUCTOR -> {
                            Double l = view.toolboxView.promptForValue("Inductor", "H");
                            if (l == null) return;
                            inputs = new double[]{l};
                        }
                        case POWER_SOURCE -> {
                            double[] pSrc = view.toolboxView.promptForPowerSource();
                            if (pSrc == null) return;
                            inputs = pSrc;
                        }
                        case BULB -> {
                        }
                    }

                    saveState();

                    Components c = Toolbox.create(
                            t,
                            view.boardRect.x + view.boardRect.width / 2,
                            view.boardRect.y + 40,
                            inputs
                    );

                    if (c == null) return;

                    model.circuit.addComponent(c);
                    clearSelection();
                    c.setSelected(true);
                    model.firstSelected = c;

                    updateCircuit();
                    view.repaint();
                    return;
                }

                // ===== CLICK COMPONENT =====
                Components hit = componentAt(p);
                if (hit != null) {
                    model.dragging = hit;
                    model.dragOffset = new Point(
                            p.x - hit.getPosition().x,
                            p.y - hit.getPosition().y
                    );

                    // 🔥 NEW SELECTION LOGIC (NO CTRL)
                    selectWithoutCtrl(hit);

                    updateCircuit();
                    view.repaint();
                    return;
                }

                // ===== CLICK EMPTY BOARD =====
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

                    int nx = p.x - model.dragOffset.x;
                    int ny = p.y - model.dragOffset.y;

                    nx = Math.max(view.boardRect.x + 10,
                            Math.min(view.boardRect.x + view.boardRect.width - 10, nx));
                    ny = Math.max(view.boardRect.y + 10,
                            Math.min(view.boardRect.y + view.boardRect.height - 10, ny));

                    model.dragging.setPosition(nx, ny);
                    view.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Point p = e.getPoint();
                if (model.dragging != null) {
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

    /* =====================
       SELECTION (NO CTRL)
       ===================== */

    private void selectWithoutCtrl(Components hit) {

        // Click lại component đang chọn → bỏ chọn
        if (hit.isSelected()) {
            hit.setSelected(false);

            if (model.firstSelected == hit) model.firstSelected = null;
            if (model.secondSelected == hit) model.secondSelected = null;

            if (model.firstSelected == null && model.secondSelected != null) {
                model.firstSelected = model.secondSelected;
                model.secondSelected = null;
            }
            return;
        }

        // Chưa có first
        if (model.firstSelected == null) {
            model.firstSelected = hit;
            hit.setSelected(true);
            return;
        }

        // Có first, chưa có second
        if (model.secondSelected == null) {
            model.secondSelected = hit;
            hit.setSelected(true);
            return;
        }

        // Đã có 2 → reset và chọn lại
        model.firstSelected.setSelected(false);
        model.secondSelected.setSelected(false);

        model.firstSelected = hit;
        model.secondSelected = null;
        hit.setSelected(true);
    }

    /* =====================
       CONNECT
       ===================== */

    private void connectSelected(Wire.Type type) {
        if (model.firstSelected == null || model.secondSelected == null) {
            view.instructionLabel.setText("Select two components first.");
            return;
        }

        saveState();

        model.wires.add(new Wire(model.firstSelected, model.secondSelected, type));

        CompositeComponent.Mode mode =
                (type == Wire.Type.SERIES)
                        ? CompositeComponent.Mode.SERIES
                        : CompositeComponent.Mode.PARALLEL;

        model.circuit.connect(model.firstSelected, model.secondSelected, mode);

        updateCircuit();
        clearSelection();
        view.repaint();
    }

    /* =====================
       UPDATE SIMULATION
       ===================== */

    private void updateCircuit() {
        double voltage = 5.0;
        double frequency = 0.0;

        for (Components c : model.circuit.getComponents()) {
            if (c instanceof PowerSource ps) {
                voltage = ps.getVoltage();
                frequency = ps.getFrequency();
                break;
            }
        }

        CompositeComponent root = model.circuit.getRoot();
        double zeq = (root != null)
                ? root.getImpedance(frequency)
                : Double.POSITIVE_INFINITY;

        double current = (Double.isInfinite(zeq) || zeq <= 0)
                ? 0.0
                : voltage / zeq;

        model.circuit.updateBulbStates(voltage);

        view.circuitStatsLabel.setText(String.format(
                "Simulation Output: V = %.2f V, f = %.2f Hz, Zeq = %.4f Ω, I = %.6f A",
                voltage, frequency, zeq, current
        ));

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

    /* =====================
       UNDO / STATE
       ===================== */

    private void undo() {
        if (model.undoStack.isEmpty()) return;

        CircuitMemento memento =
                model.undoStack.remove(model.undoStack.size() - 1);

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

    /* =====================
       HELPERS
       ===================== */

    private void clearSelection() {
        for (Components c : model.circuit.getComponents()) {
            c.setSelected(false);
        }
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

    private String getComponentDetails(Components c) {
        if (c instanceof Resistor)
            return String.format("%.2f Ω", c.getResistanceOhms());
        if (c instanceof Capacitor cap)
            return String.format("%.2f F", cap.getCapacitance());
        if (c instanceof Inductor ind)
            return String.format("%.2f H", ind.getInductance());
        if (c instanceof PowerSource ps)
            return String.format("%.2f V / %.2f Hz", ps.getVoltage(), ps.getFrequency());
        if (c instanceof Bulb)
            return "Bulb";
        return c.getId();
    }
}
