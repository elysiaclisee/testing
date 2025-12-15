package components;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class PowerSource extends Components {
    private double voltage;
    private double frequency;

    public PowerSource(String id, int x, int y, double voltage, double frequency) {
        super(id, x, y);
        this.voltage = voltage;
        this.frequency = frequency;
        this.width = 60;
        this.height = 40;
    }

    @Override
    public void draw(Graphics2D g2) {
        draw(g2, java.awt.Color.GREEN);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String vLabel = formatDouble(voltage) + "V";
        String fLabel = formatDouble(frequency) + "Hz";
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D vBounds = fm.getStringBounds(vLabel, g2);
        Rectangle2D fBounds = fm.getStringBounds(fLabel, g2);
        int vx = x - (int) vBounds.getWidth() / 2;
        int vy = y - 2;
        int fx = x - (int) fBounds.getWidth() / 2;
        int fy = y + fm.getAscent() + 2;
        g2.setColor(java.awt.Color.BLACK);
        g2.drawString(vLabel, vx, vy);
        g2.drawString(fLabel, fx, fy);
    }

    private String formatDouble(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        } else {
            return String.format("%.2f", d);
        }
    }

    @Override
    public double getResistance() {
        return 0;
    }

    public double getFrequency() {
		return frequency;
	}

	@Override
    public Rectangle getBounds() {
        return new Rectangle(x - width / 2, y - height / 2, width, height);
    }

    public double getVoltage() {
        return voltage;
    }
    
    @Override
    public double getImpedance(double frequency) {
        return 0.0; // Ideal source
    }
}
