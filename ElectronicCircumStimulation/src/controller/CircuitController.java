package controller;

import model.CircuitMemento;
import model.CircuitModel;
import view.CircuitPanel;
import components.*;

import java.awt.*;
import java.awt.event.*;

public class CircuitController {

    private final CircuitModel model;
    private final CircuitPanel view;

    // Bulb mode: bật khi user chọn 1 trong 2 nút bulbParallel/bulbSeries
    private boolean bulbMode = false;

    public CircuitController(CircuitModel model, CircuitPanel view) {
        this.model = model;
        this.view = view;

        view.seriesBtn.addActionListener(e -> connectSelected(Wire.Type.SERIES));
        view.parallelBtn.addActionListener(e -> connectSelected(Wire.Type.PARALLEL));
        view.undoBtn.addActionListener(e -> undo());

        // ===== Bulb toggles -> bật bulbMode + update dòng 2 =====
        view.bulbParallelBtn.addActionListener(e -> {
            bulbMode = true;
            updateCircuit();
            view.repaint();
        });
        view.bulbSeriesBtn.addActionListener(e -> {
            bulbMode = true;
            updateCircuit();
            view.repaint();
        });

        MouseAdapter ma = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();

                // ===== TOOLBOX CLICK =====
                Toolbox.Tool t = null;
                if (view.isInToolboxArea(p)) {
                    Point local = view.toToolboxLocal(p);
                    t = view.toolboxView.hitTool(local);
                }

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
                            // no inputs
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
                Components hit = model.circuit.selectableAt(p);
                if (hit != null) {
                    model.dragging = hit;
                    model.dragOffset = new Point(
                            p.x - hit.getPosition().x,
                            p.y - hit.getPosition().y
                    );

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

        updateCircuit();
    }

    private void selectWithoutCtrl(Components hit) {
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

        if (model.firstSelected == null) {
            model.firstSelected = hit;
            hit.setSelected(true);
            return;
        }

        if (model.secondSelected == null) {
            model.secondSelected = hit;
            hit.setSelected(true);
            return;
        }

        model.firstSelected.setSelected(false);
        model.secondSelected.setSelected(false);

        model.firstSelected = hit;
        model.secondSelected = null;
        hit.setSelected(true);
    }

    private void connectSelected(Wire.Type type) {
        if (model.firstSelected == null || model.secondSelected == null) {
            view.circuitStatsLabel.setText("Select two components first.");
            view.circuitStatsLabel.setForeground(Color.RED);
            return;
        }

        saveState();

        model.wires.add(new Wire(model.firstSelected, model.secondSelected, type));

        CompositeComponent.Mode mode = (type == Wire.Type.SERIES)
                ? CompositeComponent.Mode.SERIES
                : CompositeComponent.Mode.PARALLEL;

        model.circuit.connect(model.firstSelected, model.secondSelected, mode);

        updateCircuit();
        clearSelection();
        view.repaint();
    }

    private void updateCircuit() {
        // ===== 1) Source =====
        double vSource = 0.0;
        double fSource = 0.0;

        for (Components c : model.circuit.getComponents()) {
            if (c instanceof PowerSource ps) {
                vSource = ps.getVoltage();
                fSource = ps.getFrequency();
                break;
            }
        }

        // ===== 2) Find Bulb =====
        Bulb bulb = null;
        for (Components c : model.circuit.getComponents()) {
            if (c instanceof Bulb b) {
                bulb = b;
                break;
            }
        }

        // ===== 3) Input check: Source voltage =====
        if (vSource <= 0) {
            view.instructionLabel.setText("U_Src: 0V | I_Total: 0.00A | Z_Total: 0.00Ω");

            if (bulbMode || view.bulbParallelBtn.isSelected() || view.bulbSeriesBtn.isSelected()) {
                view.circuitStatsLabel.setText("Please set Source Voltage.");
                view.circuitStatsLabel.setForeground(Color.RED);
            } else {
                view.circuitStatsLabel.setText("Set source.");
                view.circuitStatsLabel.setForeground(new Color(70, 70, 70));
            }

            updateSelectionLine();
            return;
        }

        // ===== 4) Circuit root =====
        CompositeComponent root = model.circuit.getRoot();
        double zeq = (root != null) ? root.getImpedance(fSource) : Double.POSITIVE_INFINITY;
        double itotal = (Double.isInfinite(zeq) || zeq <= 1e-12) ? 0.0 : (vSource / zeq);

        String zText = Double.isInfinite(zeq) ? "∞" : String.format("%.2f", zeq);
        view.instructionLabel.setText(String.format(
                "U_Src: %.0fV | I_Total: %.2fA | Z_Total: %s\u03A9",
                vSource, itotal, zText
        ));

        // ===== 5) Line 2: bulb status only when user chose bulb-mode =====
        boolean bulbChosen = view.bulbParallelBtn.isSelected() || view.bulbSeriesBtn.isSelected();
        if (!bulbMode && !bulbChosen) {
            view.circuitStatsLabel.setText("Set source.");
            view.circuitStatsLabel.setForeground(new Color(70, 70, 70));
        } else {
            // bật bulbMode nếu user đã chọn 1 trong 2
            bulbMode = true;

            if (bulb == null) {
                view.circuitStatsLabel.setText("Status: Missing Bulb!");
                view.circuitStatsLabel.setForeground(Color.RED);
            } else {
                // P xấp xỉ: P = I^2 * Rbulb
                double rBulb = bulb.getResistanceOhms();
                double pReal = itotal * itotal * rBulb;
                double pRated = bulb.getPowerLimit();
                if (pRated <= 1e-9) pRated = 1.0;

                String stateText;
                Color stateColor;

                if (pReal >= pRated * 1.5) {
                    stateText = "BLOWN (Cháy)";
                    stateColor = Color.RED;
                } else if (pReal >= pRated * 0.4) {
                    stateText = "BRIGHT (Sáng)";
                    stateColor = new Color(0, 153, 0);
                } else {
                    stateText = "OFF (Yếu/Tắt)";
                    stateColor = Color.DARK_GRAY;
                }

                view.circuitStatsLabel.setText(String.format("Bulb: %s (P: %.2fW)", stateText, pReal));
                view.circuitStatsLabel.setForeground(stateColor);
            }
        }

        // ===== 6) Line 3 =====
        updateSelectionLine();
    }

    private void updateSelectionLine() {
        if (model.firstSelected != null) {
            view.componentValuesLabel.setText("Selected: " + model.firstSelected.getId());
        } else {
            view.componentValuesLabel.setText("Selected: None");
        }
    }

    private void undo() {
        if (model.undoStack.isEmpty()) return;
        CircuitMemento m = model.undoStack.remove(model.undoStack.size() - 1);
        model.redoStack.add(new CircuitMemento(model.circuit, model.wires));
        restoreState(m);
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

    private void clearSelection() {
        for (Components c : model.circuit.getComponents()) c.setSelected(false);
        model.firstSelected = null;
        model.secondSelected = null;
    }
}
