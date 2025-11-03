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
        private final Rectangle boardRect = new Rectangle(10, 110, 960, 540);

        // view toolbox
        private final view.ToolboxView toolboxView = new view.ToolboxView();

        private final java.util.List<components.Components> comps = new ArrayList<>();
        private final java.util.List<Wire> wires = new ArrayList<>();

        private Components dragging = null;
        private Point dragOffset = null;

        private Components firstSelected = null;
        private Components secondSelected = null;

        private JTextField voltageField;
        private JLabel infoLabel;

        CircuitPanel() {
            setLayout(null);
            setBackground(Color.WHITE);

            // create controls
            JButton seriesBtn = new JButton("Connect Series (Continuous)");
            JButton parallelBtn = new JButton("Connect Parallel");
            voltageField = new JTextField("5.0");
            infoLabel = new JLabel("Select two components and click a connect button.");

            seriesBtn.setBounds(380, 10, 220, 28);
            parallelBtn.setBounds(610, 10, 180, 28);
            voltageField.setBounds(800, 10, 80, 28);
            infoLabel.setBounds(380, 44, 500, 28);

            add(seriesBtn);
            add(parallelBtn);
            add(voltageField);
            add(infoLabel);

            seriesBtn.addActionListener(e -> connectSelected(Wire.Type.SERIES));
            parallelBtn.addActionListener(e -> connectSelected(Wire.Type.PARALLEL));

            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    Point p = e.getPoint();
                    // toolbox click?
                    controller.Toolbox.Tool t = toolboxView.hitTool(p);
                    if (t != null) {
                        // create component and start dragging it
                        Components c = controller.Toolbox.create(t, boardRect.x + boardRect.width/2, boardRect.y + 40);
                        comps.add(c);
                        dragging = c;
                        dragOffset = new Point(0,0);
                        // ensure selection state
                        clearSelection();
                        c.setSelected(true);
                        firstSelected = c;
                        repaint();
                        return;
                    }

                    // click on component?
                    Components hit = componentAt(p);
                    if (hit != null) {
                        // start dragging
                        dragging = hit;
                        dragOffset = new Point(p.x - hit.getPosition().x, p.y - hit.getPosition().y);
                        // toggle selection on click without dragging (we'll handle on release)
                        // but mark temporarily selected
                        repaint();
                        return;
                    }

                    // clicked on empty board area: clear selection
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
                        // clamp within board
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
                        // if released inside board keep, else remove
                        if (!boardRect.contains(p)) {
                            // remove the component
                            comps.remove(dragging);
                        }
                        // update selection: toggle if click without movement
                        // For simplicity, if the mouse release happens near where it started, toggle selection
                        if (Math.abs(dragOffset.x) < 6 && Math.abs(dragOffset.y) < 6) {
                            // toggle selection on dragging component
                            if (dragging.isSelected()) {
                                dragging.setSelected(false);
                                if (firstSelected == dragging) firstSelected = null;
                                if (secondSelected == dragging) secondSelected = null;
                            } else {
                                // add to selection slots
                                if (firstSelected == null) {
                                    firstSelected = dragging;
                                    dragging.setSelected(true);
                                } else if (secondSelected == null && dragging != firstSelected) {
                                    secondSelected = dragging;
                                    dragging.setSelected(true);
                                } else {
                                    // rotate selection
                                    clearSelection();
                                    firstSelected = dragging;
                                    dragging.setSelected(true);
                                }
                            }
                        }
                        dragging = null;
                        repaint();
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    // allow clicking board to clear selection
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

        private void connectSelected(Wire.Type type) {
            if (firstSelected == null || secondSelected == null) {
                infoLabel.setText("Please select two components first.");
                return;
            }
            wires.add(new Wire(firstSelected, secondSelected, type));
            // compute equivalent
            double req = (type == Wire.Type.SERIES)
                    ? SeriesConnection.equivalent(firstSelected, secondSelected)
                    : ParallelConnections.equivalent(firstSelected, secondSelected);
            double voltage = 0.0;
            try { voltage = Double.parseDouble(voltageField.getText()); } catch (Exception ex) { voltage = 0.0; }
            double current = Connections.current(voltage, req);
            infoLabel.setText(String.format("Req = %.3f Ω, I = %.3f A @ V=%.3fV", req, current, voltage));
            // clear selection after connecting
            clearSelection();
            repaint();
        }

        private void clearSelection() {
            for (Components c : comps) c.setSelected(false);
            firstSelected = null;
            secondSelected = null;
        }

        // renamed to avoid collision with Container.findComponentAt(Point)
        private Components componentAt(Point p) {
            for (int i = comps.size()-1; i >= 0; i--) {
                Components c = comps.get(i);
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
            for (Components c : comps) {
                c.draw(g2);
            }

            // instructions
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Click a toolbox item to add it to the board; drag to move. Select two components and click connect.", 10, boardRect.y - 6);
        }
    }
}