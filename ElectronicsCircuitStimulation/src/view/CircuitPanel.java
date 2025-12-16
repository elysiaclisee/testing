package view;

import model.CircuitModel;
import components.*;
import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class CircuitPanel extends JPanel {
    // UI Constants
    public final Rectangle boardRect = new Rectangle(10, 150, 965, 500);
    public final ToolboxView toolboxView = new ToolboxView();

    // UI Components (Made public so Controller can access them like before)
    public final JButton seriesBtn = new JButton("Series");
    public final JButton parallelBtn = new JButton("Parallel");
    public final JButton undoBtn = new JButton("Undo");
    public final JButton helpBtn = new JButton("Help");
    public final JLabel instructionLabel = new JLabel("Select two components and click a connect button.");
    public final JLabel circuitStatsLabel = new JLabel("Circuit: -");
    public final JLabel componentValuesLabel = new JLabel("Selection: None");
    
 // THÊM: Khu vực chọn chế độ bóng đèn
    public final JRadioButton rbSeriesBulb = new JRadioButton("Bulb Series Mode", true); // Mặc định chọn
    public final JRadioButton rbParallelBulb = new JRadioButton("Bulb Parallel Mode");
    public final ButtonGroup modeGroup = new ButtonGroup();

    private final CircuitModel model;

    public CircuitPanel(CircuitModel model) {
        this.model = model;
        setLayout(null);
        setBackground(Color.WHITE);

        // UI Setup logic moved from constructor
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
        
     // Thêm nút vào GUI (Vị trí tùy bạn chỉnh lại cho đẹp)
        rbSeriesBulb.setBounds(10, 110, 150, 30);
        rbParallelBulb.setBounds(170, 110, 150, 30);
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
        helpBtn.addActionListener(e -> showHelpWindow());
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
    
//If the window size is not the issue, it might be a Z-Order issue (the component sliding under the board). 
//This happens if the component is drawn before the board background.
//Check your CircuitPanel.java -> paintComponent method. The order MUST be:
//Draw Board Background (First)
//Draw Grid/Wires
//Draw Components (Last)
    
    private void showHelpWindow() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        
        // Create a Dialog (Modal means you must close it to go back to the app)
        JDialog helpDialog = new JDialog(parentWindow, "Circuit Simulator Help", Dialog.ModalityType.APPLICATION_MODAL);
        
        // Create the text area
        JTextArea helpText = new JTextArea();
        helpText.setText(getHelpContent()); // Helper method for the text
        helpText.setEditable(false);        // User cannot edit
        helpText.setLineWrap(true);         // Wrap long lines
        helpText.setWrapStyleWord(true);    // Wrap at word boundaries
        helpText.setFont(new Font("SansSerif", Font.PLAIN, 14));
        helpText.setMargin(new Insets(10, 10, 10, 10)); // Padding

        JScrollPane scrollPane = new JScrollPane(helpText);
        // Ensure scrollbar always shows if needed
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        helpDialog.add(scrollPane);

        // Match the size of the parent app
        if (parentWindow != null) {
            helpDialog.setSize(parentWindow.getSize());
            helpDialog.setLocationRelativeTo(parentWindow); // Center it
        } else {
            helpDialog.setSize(800, 600); // Fallback size
        }

        helpDialog.setVisible(true);
    }

    // Put your long text here to keep code clean
    private String getHelpContent() {
        return """
Welcome to our Circuit Simulator!
This application allows you to create and simulate simple electronic circuits using components like resistors, capacitors, inductors, bulbs, and a power source.
 
Before you start, here are some key notes you must read:
- Default power source type is AC (Alternating Current). This is why only one power source is allowed in the circuit.
Design your circuit freely, then connect 2 components on 2 end each to a point on 2 opposite edges. That's how we connect to power source.
This ensures our circuit is closed.  	                      
- Dragging components to borders of the circuit board may make them inaccessible. Please keep components within the visible area. 
- If you want to connect 2 components, select the first one, then hit CTRL and select the second one. Then click connection buttons to connect them.
- For simplicity, right now only 1 bulb is supported. 

Thank you for understanding these limitations as we work to improve the simulator in future versions!
                """;
    }
}