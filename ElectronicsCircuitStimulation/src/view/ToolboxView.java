package view;

import components.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import javax.swing.*;

public class ToolboxView {
    public enum Tool { POWER_SOURCE, BULB, RESISTOR, CAPACITOR, INDUCTOR }
    
    public static final int X = 20;
    public static final int Y = 10;
    public static final int WIDTH = 530;
    public static final int HEIGHT = 120;
    private static final int HEADER_HEIGHT = 30;

    private final Rectangle[] slots = new Rectangle[5];

    private PowerSource sampleSource;
    private final Resistor sampleResistor;
    private final Bulb sampleBulb;
    private final Capacitor sampleCapacitor;
    private final Inductor sampleInductor;

    public ToolboxView() {
        int padding = 10;
        int numSlots = 5;
        int totalPadding = padding * (numSlots + 1);
        int slotWidth = (WIDTH - totalPadding) / numSlots;
        int slotHeight = HEIGHT - HEADER_HEIGHT - padding;

        for (int i = 0; i < numSlots; i++) {
            int sx = X + padding + i * (slotWidth + padding);
            int sy = Y + HEADER_HEIGHT;
            slots[i] = new Rectangle(sx, sy, slotWidth, slotHeight);
        }

        // Initialize Dummies
        sampleSource = new PowerSource("Display", 0, 0, 220.0, 50.0);
        sampleResistor = new Resistor("Display", 0, 0, 0); 
        sampleBulb = new Bulb("Display", 0, 0);
        sampleCapacitor = new Capacitor("Display", 0, 0, 0);
        sampleInductor = new Inductor("Display", 0, 0, 0);
        
        sampleSource.setSelected(false);
        sampleResistor.setSelected(false);
        sampleBulb.setSelected(false);
        sampleCapacitor.setSelected(false);
        sampleInductor.setSelected(false);
    }
    
    public void updatePowerSourceDisplay(double voltage, double frequency) {
        this.sampleSource = new PowerSource("Display", 0, 0, voltage, frequency);
        this.sampleSource.setSelected(false);
    }
    
    public void draw(Graphics2D g2) {
        g2.setColor(new Color(230, 230, 230));
        g2.fillRect(X, Y, WIDTH, HEIGHT);
        g2.setColor(Color.GRAY);
        g2.drawRect(X, Y, WIDTH, HEIGHT);

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Toolbox", X + 10, Y + 20);

        // --- DRAW SLOTS (Swapped Bulb and Resistor positions) ---
        // Slot 0: Power Source
        drawComponentInSlot(g2, slots[0], sampleSource, "Power Source");
        drawComponentInSlot(g2, slots[1], sampleBulb, "Bulb");
        drawComponentInSlot(g2, slots[2], sampleResistor, "Resistor");
        drawComponentInSlot(g2, slots[3], sampleCapacitor, "Capacitor");
        drawComponentInSlot(g2, slots[4], sampleInductor, "Inductor");
    }

    private void drawComponentInSlot(Graphics2D g2, Rectangle r, Components c, String label) {
        g2.setColor(Color.WHITE);
        g2.fill(r);
        g2.setColor(Color.BLACK);
        g2.draw(r);

        int cx = r.x + r.width / 2;
        int cy = r.y + r.height / 2 - 6;

        c.setPosition(cx, cy);
        c.draw(g2); 

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        Rectangle2D bounds = g2.getFontMetrics().getStringBounds(label, g2);
        int lx = r.x + (r.width - (int) bounds.getWidth()) / 2;
        int ly = r.y + r.height - 6;
        g2.setColor(Color.BLACK);
        g2.drawString(label, lx, ly);
    }

    public Tool hitTool(Point p) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i].contains(p)) {
                switch (i) {
                    case 0 -> { return Tool.POWER_SOURCE; }
                    case 1 -> { return Tool.BULB; }     
                    case 2 -> { return Tool.RESISTOR; } 
                    case 3 -> { return Tool.CAPACITOR; }
                    case 4 -> { return Tool.INDUCTOR; }
                }
            }
        }
        return null;
    }
    
    public Double promptForValue(String componentName, String unit) {
        String prompt = "Enter value for " + componentName + " (" + unit + "):";
        String input = JOptionPane.showInputDialog(null, prompt, "Enter " + componentName, JOptionPane.PLAIN_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try { return Double.parseDouble(input.trim()); } catch (NumberFormatException ex) { return 0.0; }
        }
        return null; 
    }

    public double[] promptForPowerSource() {
        JTextField voltageField = new JTextField("220.0", 5);
        JTextField frequencyField = new JTextField("50.0", 5);
        JPanel panel = new JPanel();
        panel.add(new JLabel("Voltage (V):"));
        panel.add(voltageField);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(new JLabel("Frequency (Hz):"));
        panel.add(frequencyField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Enter power source values:", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            double v = 220.0;
            double f = 100.0;

            try {
                double inputV = Double.parseDouble(voltageField.getText());
                if (inputV != 0) {
                    v = inputV;
                }
            } catch (Exception e) {}

            try {
                double inputF = Double.parseDouble(frequencyField.getText());
                if (inputF != 0) {
                    f = inputF;
                }
            } catch (Exception e) {}

            return new double[]{v, f};
        }
        return null; 
    }
}