package view;

import model.CircuitModel;
import components.*;
import javax.swing.*;
import java.awt.*;

public class CircuitPanel extends JPanel {
    // UI Constants
    public final Rectangle boardRect = new Rectangle(10, 150, 965, 500);
    public final ToolboxView toolboxView = new ToolboxView();

    // UI Components (Made public so Controller can access them like before)
    public final JButton seriesBtn = new JButton("Series");
    public final JButton parallelBtn = new JButton("Parallel");
    public final JButton undoBtn = new JButton("Undo");
    public final JLabel instructionLabel = new JLabel("Select two components and click a connect button.");
    public final JLabel circuitStatsLabel = new JLabel("Circuit: -");
    public final JLabel componentValuesLabel = new JLabel("Selection: None");

    private final CircuitModel model;

    public CircuitPanel(CircuitModel model) {
        this.model = model;
        setLayout(null);
        setBackground(Color.WHITE);

        // UI Setup logic moved from constructor
        seriesBtn.setBounds(560, 30, 100, 28);
        parallelBtn.setBounds(670, 30, 100, 28);
        undoBtn.setBounds(780, 30, 100, 28);
        instructionLabel.setBounds(565, 65, 400, 20);
        circuitStatsLabel.setBounds(565, 85, 400, 20);
        componentValuesLabel.setBounds(565, 105, 400, 20);

        instructionLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        circuitStatsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        componentValuesLabel.setForeground(new Color(0, 100, 0));

        add(seriesBtn);
        add(parallelBtn);
        add(undoBtn);
        add(instructionLabel);
        add(circuitStatsLabel);
        add(componentValuesLabel);
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

        // Accessing data via model
        for (Wire w : model.getWires()) w.draw(g2);

        g2.setStroke(originalStroke);
        g2.setColor(originalColor);

        for (Components c : model.getCircuit().getComponents()) {
            c.draw(g2);
            g2.setStroke(originalStroke);
            g2.setColor(originalColor);
        }

        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Click toolbox to add. Drag to move. Select two & connect.", 10, boardRect.y - 6);
    }
}