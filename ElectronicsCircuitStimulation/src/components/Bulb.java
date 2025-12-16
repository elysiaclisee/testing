package components;

import java.awt.*;

public class Bulb extends Components {
    private double resistance;
    private double powerLimit; // The "Wattage" (e.g., 100W)
    private boolean isLighted = false;

    // 1. Constructor: We ask for Voltage and Power Limit (Wattage)
    public Bulb(String id, int x, int y, double voltage, double powerLimit) {
        super(id, x, y);
        this.powerLimit = powerLimit;
        // Physics Formula: R = V^2 / P
        if (powerLimit > 0) {
            this.resistance = (voltage * voltage) / powerLimit;
        } else {
            this.resistance = Double.POSITIVE_INFINITY;
        }
    }

    public Bulb(String id, int x, int y) {
        this(id, x, y, 220.0, 100.0); 
    }

    public void setLighted(boolean lighted) {
        isLighted = lighted;
    }

    public double getPowerLimit() {
        return powerLimit;
    }

    @Override
    public double getResistance() {
        return resistance;
    }

    @Override
    public double getImpedance(double frequency) {
        return resistance;
    }

    @Override
    public void draw(Graphics2D g2) {
        Color c = isLighted ? Color.YELLOW : Color.DARK_GRAY;
        super.draw(g2, c); 
        g2.setColor(isLighted ? Color.BLACK : Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(10f));
        
        String label = (int)powerLimit + "W";
        FontMetrics fm = g2.getFontMetrics();
        int tx = x - fm.stringWidth(label) / 2;
        int ty = y + fm.getAscent() / 2;
        g2.drawString(label, tx, ty);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x - width / 2, y - height / 2, width, height);
    }
}