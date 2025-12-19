package view;

import model.CircuitModel;
import components.*;
import utils.Wire;
import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class CircuitPanel extends JPanel {
    public final Rectangle boardRect = new Rectangle(20, 150, 1140, 500);
    public final ToolboxView toolboxView = new ToolboxView();
    public final JButton seriesBtn = new JButton("Series");
    public final JButton parallelBtn = new JButton("Parallel");
    public final JButton undoBtn = new JButton("Undo");
    public final JButton helpBtn = new JButton("Help");
    public final JLabel instructionLabel = new JLabel("Click two components, then click Series or Parallel to connect.");
    public final JLabel circuitStatsLabel = new JLabel("Circuit: -");
    public final JLabel componentValuesLabel = new JLabel("Selection: None");
    public final JRadioButton rbSeriesBulb = new JRadioButton("Circuit connects series to bulb", true); // Mặc định chọn
    public final JRadioButton rbParallelBulb = new JRadioButton("Circuit connects parallel to bulb");
    public final ButtonGroup modeGroup = new ButtonGroup();
    private final CircuitModel model;

    public CircuitPanel(CircuitModel model) {
        this.model = model;
        setLayout(null);
        setBackground(Color.WHITE);

        seriesBtn.setBounds(560, 30, 90, 28);
        parallelBtn.setBounds(650, 30, 90, 28);
        undoBtn.setBounds(740, 30, 90, 28);
        helpBtn.setBounds(830, 30, 90, 28);
        instructionLabel.setBounds(565, 65, 400, 20);
        circuitStatsLabel.setBounds(565, 85, 400, 20);
        componentValuesLabel.setBounds(565, 105, 400, 20);

        instructionLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        circuitStatsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        componentValuesLabel.setForeground(new Color(0, 100, 0));
        
        rbSeriesBulb.setBounds(950, 30, 250, 28);
        rbParallelBulb.setBounds(950, 70, 250, 28);
        rbSeriesBulb.setBackground(Color.WHITE);
        rbParallelBulb.setBackground(Color.WHITE);
        
        modeGroup.add(rbSeriesBulb);
        modeGroup.add(rbParallelBulb);
        
        add(rbSeriesBulb);
        add(rbParallelBulb);
        add(seriesBtn);
        add(parallelBtn);
        add(undoBtn);
        add(helpBtn);
        add(instructionLabel);
        add(circuitStatsLabel);
        add(componentValuesLabel);
        helpBtn.addActionListener(_ -> showHelpWindow());
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
        g2.setColor(Color.BLACK);
        g2.drawString("Circuit Board", boardRect.x + 8, boardRect.y + 16);

        for (Wire w : model.getWires()) w.draw(g2);

        g2.setStroke(originalStroke);
        g2.setColor(originalColor);

        for (Components c : model.getCircuit().getComponents()) {
            c.draw(g2);
            g2.setStroke(originalStroke);
            g2.setColor(originalColor);
        }

        g2.setColor(Color.BLACK);
        g2.drawString("Click toolbox to add. Drag to move. Click two components to select, then connect.", 20, boardRect.y - 6);
    }
    
    private void showHelpWindow() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        
        // Create a Dialog (Modal means you must close it to go back to the app)
        JDialog helpDialog = new JDialog(parentWindow, "Circuit Simulator Help", Dialog.ModalityType.APPLICATION_MODAL);
        
        // Create the text area
        JTextArea helpText = new JTextArea();
        helpText.setText(getHelpContent()); // Helper method for the text
        helpText.setEditable(false);        // User cannot edit
        helpText.setLineWrap(true);         
        helpText.setWrapStyleWord(true);    
        helpText.setFont(new Font("SansSerif", Font.PLAIN, 14));
        helpText.setMargin(new Insets(10, 10, 10, 10)); 

        JScrollPane scrollPane = new JScrollPane(helpText);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        helpDialog.add(scrollPane);

        if (parentWindow != null) {
            helpDialog.setSize(parentWindow.getSize());
            helpDialog.setLocationRelativeTo(parentWindow); 
        } else {
            helpDialog.setSize(1195, 710); 
        }

        helpDialog.setVisible(true);
    }
    
    private String getHelpContent() {
        return """
Welcome to our Circuit Simulator!
This application allows you to create and simulate simple electronic circuits using components like resistors, capacitors, inductors, bulbs, and a power source.
 
Before you start, here are some key notes you must read:
- Default power source type is AC (Alternating Current). This is why only one power source is allowed in the circuit, and 0Hz frequency is not accepted.
Design your circuit freely, then connect 2 last components on 2 end of your circuit each to a terminal on the board. That's how we connect to power source.
This ensures our circuit is closed.  	                      
- Dragging components to borders of the circuit board may make them inaccessible. Please keep components within the visible area. 
- If you want to connect 2 components, select the first one, then hit CTRL and select the second one. Then click connection buttons to connect them.
- For simplicity, right now only 1 bulb is supported, and there are 2 options: series or parallel connection to the whole circuit on board.
Each connection option will have a state update for you to check, along with displayed values.

Thank you for understanding these limitations as we work to improve the simulator in future versions!
                """;
    }
}