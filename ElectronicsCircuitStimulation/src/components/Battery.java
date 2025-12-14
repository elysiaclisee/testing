package components;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;

public class Battery extends Components {
    private double voltage;
    private double frequency;

    public Battery(String id, int x, int y, double voltage, double frequency) {
        super(id, x, y);
        this.voltage = voltage;
        this.frequency = frequency;
        this.width = 60;
        this.height = 40;
    }

    @Override
    public void draw(Graphics2D g2) {
        int w = width;
        int h = height;
        int left = x - w/2;
        int top = y - h/2;
        g2.setColor(java.awt.Color.GREEN);
        g2.fillRect(left, top, w, h);
        g2.setColor(java.awt.Color.BLACK);
        g2.drawRect(left, top, w, h);
        String vLabel = formatDouble(voltage) + "V";
        String fLabel = formatDouble(frequency) + "Hz";
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D vBounds = fm.getStringBounds(vLabel, g2);
        Rectangle2D fBounds = fm.getStringBounds(fLabel, g2);
        int vx = x - (int) vBounds.getWidth() / 2;
        int vy = y - 2;
        int fx = x - (int) fBounds.getWidth() / 2;
        int fy = y + fm.getAscent() + 2;
        g2.drawString(vLabel, vx, vy);
        g2.drawString(fLabel, fx, fy);
        if (selected) {
            g2.setColor(java.awt.Color.BLUE);
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.drawRect(left-4, top-4, w+8, h+8);
        }
    }

    private String formatDouble(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        } else {
            return String.format("%.2f", d);
        }
    }

    @Override
    public double getResistanceOhms() {
        return 0;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x - width / 2, y - height / 2, width, height);
    }

    public double getVoltage() {
        return voltage;
    }
}
