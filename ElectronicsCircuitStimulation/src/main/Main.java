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
        // board geometry
        private final Rectangle boardRect = new Rectangle(10, 150, 965, 500);

        // view toolbox
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
            circuitStatsLabel = new JLabel("Circuit stats will be shown here.");
            componentValuesLabel = new JLabel("Component values will be shown here.");

            seriesBtn.setBounds(560, 30, 100, 28);
            parallelBtn.setBounds(670, 30, 100, 28);
            undoBtn.setBounds(780, 30, 80, 28);
            instructionLabel.setBounds(565, 60, 500, 28);
            circuitStatsLabel.setBounds(565, 80, 500, 28);
            componentValuesLabel.setBounds(565, 100, 500, 28);

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
                        Components c = controller.Toolbox.create(t, boardRect.x + boardRect.width/2, boardRect.y + 40);
                        if (c == null) {
                            // creation was cancelled, so don't add component
                            return;
                        }
                        circuit.addComponent(c);
                        dragging = c;
                        dragOffset = new Point(0,0);
                        clearSelection();
                        c.setSelected(true);
                        firstSelected = c;
                        repaint();
                        return;
                    }

                    Components hit = componentAt(p);
                    if (hit != null) {
                        dragging = hit;
                        dragOffset = new Point(p.x - hit.getPosition().x, p.y - hit.getPosition().y);
                        repaint();
                        return;
                    }

                    if (boardRect.contains(p)) {
                        clearSelection();
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
                        if (pressPoint != null && p.distance(pressPoint) < 6) {
                            if (dragging.isSelected()) {
                                dragging.setSelected(false);
                                if (firstSelected == dragging) firstSelected = null;
                                else if (secondSelected == dragging) secondSelected = null;
                            } else {
                                if (firstSelected == null) {
                                    firstSelected = dragging;
                                    dragging.setSelected(true);
                                } else if (secondSelected == null && dragging != firstSelected) {
                                    secondSelected = dragging;
                                    dragging.setSelected(true);
                                } else {
                                    clearSelection();
                                    firstSelected = dragging;
                                    dragging.setSelected(true);
                                }
                            }
                        }
                        dragging = null;
                        updateCircuit();
                        repaint();
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    Point p = e.getPoint();
                    if (!boardRect.contains(p)) return;
                    Components hit = componentAt(p);
                    if (hit == null) {
                        clearSelection();
                        repaint();
                    }
                }
            };

            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        private void undo() {
            if (undoStack.isEmpty()) {
                return;
            }
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
                instructionLabel.setText("Please select two components first.");
                return;
            }
            saveState();
            wires.add(new Wire(firstSelected, secondSelected, type));

            CompositeComponent.Mode mode = (type == Wire.Type.SERIES) ? CompositeComponent.Mode.SERIES : CompositeComponent.Mode.PARALLEL;
            circuit.connect(firstSelected, secondSelected, mode);

            updateCircuit();
            // clear selection after connecting
            clearSelection();
            repaint();
        }

        private void updateCircuit() {
            double voltage = 5.0; // Default voltage
            // Find a battery in the circuit to use its voltage
            for (Components c : circuit.getComponents()) {
                if (c instanceof Battery) {
                    voltage = ((Battery) c).getVoltage();
                    break;
                }
            }
            circuit.updateBulbStates(voltage);

            double req = circuit.getRoot() != null ? circuit.getRoot().getResistanceOhms() : 0.0;
            double current = Connections.current(voltage, req);
            circuitStatsLabel.setText(String.format("R = %s Ω, I = %.3f A @ V=%sV", formatDouble(req), current, formatDouble(voltage)));

            StringBuilder componentValues = new StringBuilder("<html>");
            for (Components c : circuit.getComponents()) {
                componentValues.append(c.getId()).append(": ");
                if (c instanceof Resistor) {
                    componentValues.append(String.format("%s Ω", formatDouble(c.getResistanceOhms())));
                } else if (c instanceof Capacitor) {
                    componentValues.append(String.format("%s F", formatDouble(((Capacitor) c).getCapacitance())));
                } else if (c instanceof Inductor) {
                    componentValues.append(String.format("%s H", formatDouble(((Inductor) c).getInductance())));
                } else if (c instanceof Bulb) {
                    componentValues.append(String.format("%s Ω", formatDouble(c.getResistanceOhms())));
                } else if (c instanceof Battery) {
                    componentValues.append(String.format("%s V", formatDouble(((Battery) c).getVoltage())));
                }
                componentValues.append("<br>");
            }
            componentValues.append("</html>");
            componentValuesLabel.setText(componentValues.toString());
        }

        private String formatDouble(double d) {
            if (d == (long) d) {
                return String.format("%d", (long) d);
            } else {
                return String.format("%.2f", d);
            }
        }

        private void clearSelection() {
            for (Components c : circuit.getComponents()) c.setSelected(false);
            firstSelected = null;
            secondSelected = null;
        }

        // renamed to avoid collision with Container.findComponentAt(Point)
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

            // draw toolbox
            toolboxView.draw(g2);

            // draw circuit board rectangle
            g2.setColor(new Color(245, 255, 245));
            g2.fill(boardRect);
            g2.setColor(Color.GRAY);
            g2.draw(boardRect);
            g2.setFont(g2.getFont().deriveFont(12f));
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Circuit Board", boardRect.x + 8, boardRect.y + 16);

            // draw wires
            for (Wire w : wires) {
                w.draw(g2);
            }

            // draw components
            for (Components c : circuit.getComponents()) {
                c.draw(g2);
            }

            // instructions
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Click a toolbox item to add it to the board; drag to move. Select two components and click connect.", 10, boardRect.y - 6);
        }
    }
}