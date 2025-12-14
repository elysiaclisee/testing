package main;

import components.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Electronics Circuit Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
            frame.add(new CircuitPanel());
            frame.setVisible(true);
        });
    }

    static class CircuitPanel extends JPanel {
        private final Rectangle boardRect = new Rectangle(10, 150, 965, 500);
        private final view.ToolboxView toolboxView = new view.ToolboxView();
        private final Circuit circuit = new Circuit();
        private final java.util.List<Wire> wires = new ArrayList<>();
        private Components dragging = null;
        private Point dragOffset = null;
        private Components firstSelected = null;
        private Components secondSelected = null;
        private JLabel instructionLabel;
        private JLabel circuitStatsLabel;
        private JLabel componentValuesLabel;
        private final List<CircuitMemento> undoStack = new ArrayList<>();
        private final List<CircuitMemento> redoStack = new ArrayList<>();

        CircuitPanel() {
            setLayout(null);
            setBackground(Color.WHITE);
            JButton seriesBtn = new JButton("Series");
            JButton parallelBtn = new JButton("Parallel");
            JButton undoBtn = new JButton("Undo");
            instructionLabel = new JLabel("Select two components and click a connect button.");
            circuitStatsLabel = new JLabel("Circuit: -");
            componentValuesLabel = new JLabel("Selection: None");
            seriesBtn.setBounds(560, 30, 100, 28);
            parallelBtn.setBounds(670, 30, 100, 28);
            undoBtn.setBounds(780, 30, 100, 28);
            instructionLabel.setBounds(565, 65, 400, 20);
            circuitStatsLabel.setBounds(565, 85, 400, 20);
            componentValuesLabel.setBounds(565, 105, 400, 20);
            instructionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            circuitStatsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            componentValuesLabel.setForeground(new Color(0, 100, 0));
            add(seriesBtn);
            add(parallelBtn);
            add(undoBtn);
            add(instructionLabel);
            add(circuitStatsLabel);
            add(componentValuesLabel);
            seriesBtn.addActionListener(e -> connectSelected(Wire.Type.SERIES));
            parallelBtn.addActionListener(e -> connectSelected(Wire.Type.PARALLEL));
            undoBtn.addActionListener(e -> undo());
            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    Point p = e.getPoint();
                    putClientProperty("pressPoint", p);
                    controller.Toolbox.Tool t = toolboxView.hitTool(p);
                    if (t != null) {
                        saveState();
                        Components c = controller.Toolbox.create(t, boardRect.x + boardRect.width / 2, boardRect.y + 40);
                        if (c == null) return;
                        circuit.addComponent(c);
                        dragging = c;
                        dragOffset = new Point(0, 0);
                        clearSelection();
                        c.setSelected(true);
                        firstSelected = c;
                        updateCircuit();
                        repaint();
                        return;
                    }
                    Components hit = componentAt(p);
                    if (hit != null) {
                        dragging = hit;
                        dragOffset = new Point(p.x - hit.getPosition().x, p.y - hit.getPosition().y);
                        if (e.isControlDown()) {
                            if (hit.isSelected()) {
                                hit.setSelected(false);
                                if (firstSelected == hit) firstSelected = null;
                                if (secondSelected == hit) secondSelected = null;
                            } else {
                                hit.setSelected(true);
                                if (firstSelected == null) {
                                    firstSelected = hit;
                                } else if (secondSelected == null) {
                                    secondSelected = hit;
                                }
                            }
                        } else {
                            if (!hit.isSelected()) {
                                clearSelection();
                                hit.setSelected(true);
                                firstSelected = hit;
                            }
                        }
                        updateCircuit();
                        repaint();
                        return;
                    }
                    if (boardRect.contains(p)) {
                        clearSelection();
                        updateCircuit();
                        repaint();
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragging != null) {
                        Point p = e.getPoint();
                        int nx = p.x - (dragOffset != null ? dragOffset.x : 0);
                        int ny = p.y - (dragOffset != null ? dragOffset.y : 0);
                        nx = Math.max(boardRect.x + 10, Math.min(boardRect.x + boardRect.width - 10, nx));
                        ny = Math.max(boardRect.y + 10, Math.min(boardRect.y + boardRect.height - 10, ny));
                        dragging.setPosition(nx, ny);
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    Point p = e.getPoint();
                    if (dragging != null) {
                        Point pressPoint = (Point) getClientProperty("pressPoint");
                        if (!boardRect.contains(p)) {
                            circuit.removeComponent(dragging);
                        }
                        dragging = null;
                        updateCircuit();
                        repaint();
                    }
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        private void undo() {
            if (undoStack.isEmpty()) return;
            CircuitMemento memento = undoStack.remove(undoStack.size() - 1);
            redoStack.add(new CircuitMemento(circuit, wires));
            restoreState(memento);
            repaint();
        }

        private void saveState() {
            redoStack.clear();
            undoStack.add(new CircuitMemento(circuit, wires));
        }

        private void restoreState(CircuitMemento memento) {
            circuit.setComponents(memento.getComponents());
            wires.clear();
            wires.addAll(memento.getWires());
            updateCircuit();
        }

        private void connectSelected(Wire.Type type) {
            if (firstSelected == null || secondSelected == null) {
                instructionLabel.setText("Select two components first.");
                return;
            }
            saveState();
            wires.add(new Wire(firstSelected, secondSelected, type));
            CompositeComponent.Mode mode = (type == Wire.Type.SERIES) ? CompositeComponent.Mode.SERIES : CompositeComponent.Mode.PARALLEL;
            circuit.connect(firstSelected, secondSelected, mode);
            updateCircuit();
            clearSelection();
            repaint();
        }

        private void updateCircuit() {
            double voltage = 5.0;
            for (Components c : circuit.getComponents()) {
                if (c instanceof PowerSource) {
                    voltage = ((PowerSource) c).getVoltage();
                    break;
                }
            }
            circuit.updateBulbStates(voltage);
            double req = circuit.getRoot() != null ? circuit.getRoot().getResistanceOhms() : 0.0;
            double current = Connections.current(voltage, req);
            circuitStatsLabel.setText(String.format("Total: V = %s, I = %s, Req = %s",
                    formatMetric(voltage, "V"),
                    formatMetric(current, "A"),
                    formatMetric(req, "Ω")));
            if (firstSelected != null) {
                String details = getComponentDetails(firstSelected);
                if (secondSelected != null) {
                    details += " + " + getComponentDetails(secondSelected);
                }
                componentValuesLabel.setText("Selected: " + details);
            } else {
                componentValuesLabel.setText("Selected: None");
            }
        }

        private String getComponentDetails(Components c) {
            if (c instanceof Resistor) return formatMetric(c.getResistanceOhms(), "Ω");
            if (c instanceof Capacitor) return formatMetric(((Capacitor) c).getCapacitance(), "F");
            if (c instanceof Inductor) return formatMetric(((Inductor) c).getInductance(), "H");
            if (c instanceof PowerSource) {
                PowerSource b = (PowerSource) c;
                return formatMetric(b.getVoltage(), "V") + " / " + formatMetric(b.getFrequency(), "Hz");
            }
            if (c instanceof Bulb) return "Bulb (" + formatMetric(c.getResistanceOhms(), "Ω") + ")";
            return c.getId();
        }

        private String formatMetric(double value, String unit) {
            if (Double.isInfinite(value) || Double.isNaN(value)) return "- " + unit;
            if (value == 0) return "0 " + unit;
            double abs = Math.abs(value);
            String prefix = "";
            double scaled = value;
            if (abs >= 1_000_000) { scaled /= 1_000_000; prefix = "M"; }
            else if (abs >= 1_000) { scaled /= 1_000; prefix = "k"; }
            else if (abs >= 1) { prefix = ""; }
            else if (abs >= 0.001) { scaled *= 1_000; prefix = "m"; }
            else if (abs >= 0.000_001) { scaled *= 1_000_000; prefix = "µ"; }
            else if (abs >= 0.000_000_001) { scaled *= 1_000_000_000; prefix = "n"; }
            String s = String.format("%.2f", scaled);
            if (s.endsWith(".00")) s = s.substring(0, s.length() - 3);
            else if (s.endsWith("0") && s.contains(".")) s = s.substring(0, s.length() - 1);
            return s + " " + prefix + unit;
        }

        private void clearSelection() {
            for (Components c : circuit.getComponents()) c.setSelected(false);
            firstSelected = null;
            secondSelected = null;
        }

        private Components componentAt(Point p) {
            List<Components> components = circuit.getComponents();
            for (int i = components.size() - 1; i >= 0; i--) {
                Components c = components.get(i);
                if (c.contains(p)) return c;
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Stroke originalStroke = g2.getStroke();
            Color originalColor = g2.getColor();

            toolboxView.draw(g2);

            g2.setColor(new Color(245, 255, 245));
            g2.fill(boardRect);
            g2.setColor(Color.GRAY);
            g2.draw(boardRect);
            g2.setFont(g2.getFont().deriveFont(12f));
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Circuit Board", boardRect.x + 8, boardRect.y + 16);

            for (Wire w : wires) w.draw(g2);

            g2.setStroke(originalStroke);
            g2.setColor(originalColor);

            for (Components c : circuit.getComponents()) {
                c.draw(g2);
                g2.setStroke(originalStroke);
                g2.setColor(originalColor);
            }

            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Click toolbox to add. Drag to move. Select two & connect.", 10, boardRect.y - 6);
        }
    }
}