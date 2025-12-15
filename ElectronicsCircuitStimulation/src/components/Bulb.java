package components;

import java.awt.*;

public class Bulb extends Components {
    private boolean isLighted = false;
    private double powerLimit;

    public Bulb(String id, int x, int y, double resistance, double powerLimit) {
        super(id, x, y);
        this.resistanceOhms = resistance;
        this.powerLimit = powerLimit;
    }

    public Bulb(String id, int x, int y) {
        this(id, x, y, 220.0, 100.0); // Default resistance and power limit
    }

    public void setLighted(boolean lighted) {
        isLighted = lighted;
    }

    public double getPowerLimit() {
        return powerLimit;
    }

    @Override
    public double getImpedance(double frequency) {
        // Bulb is purely resistive (mostly)
        return resistanceOhms;
    }

    @Override
    public double getResistanceOhms() {
        return resistanceOhms;
    }

    @Override
    public void draw(Graphics2D g2) {
        // Visual feedback based on simulation state
        Color fillColor = isLighted ? Color.YELLOW : Color.DARK_GRAY;
        draw(g2, fillColor);

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(12f));
        FontMetrics fm = g2.getFontMetrics();
        String s = "Bulb";
        int sx = x - fm.stringWidth(s)/2;
        int sy = y + fm.getAscent()/2;
        g2.drawString(s, sx, sy);
        
        // Optional: Draw V/I for debugging
        // String debug = String.format("%.1fV", voltageDrop);
        // g2.drawString(debug, x, y - 20);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x - width / 2, y - height / 2, width, height);
    }
}