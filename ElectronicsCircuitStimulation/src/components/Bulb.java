package components;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Bulb extends Components {
    private double resistance;
    private double powerLimit = 50; 
    private boolean isLighted = false;
    private double voltage = 220.0; 

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
        this.width = 60;
        this.height = 40;
    }

    public Bulb(String id, int x, int y) {
        this(id, x, y, 220.0, 50.0); 
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
        g2.setFont(g2.getFont().deriveFont(12f));
        String vLabel = formatDouble(voltage) + "V";
        String fLabel = formatDouble(powerLimit) + "W";
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D vBounds = fm.getStringBounds(vLabel, g2);
        Rectangle2D fBounds = fm.getStringBounds(fLabel, g2);
        int vx = x - (int) vBounds.getWidth() / 2;
        int vy = y - 2;
        int fx = x - (int) fBounds.getWidth() / 2;
        int fy = y + fm.getAscent() + 2;
        g2.setColor(java.awt.Color.WHITE);
        g2.drawString(vLabel, vx, vy);
        g2.drawString(fLabel, fx, fy);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x - width / 2, y - height / 2, width, height);
    }
    
    private String formatDouble(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        } else {
            return String.format("%.2f", d);
        }
    }
}