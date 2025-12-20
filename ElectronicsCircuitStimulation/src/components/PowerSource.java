package components;

import utils.Complex;
import utils.FormatUtils;
import java.awt.*;
import java.awt.geom.Rectangle2D;
//icon on toolbar only, for future development and extension
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
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        String vLabel = FormatUtils.formatMetric(voltage, "V");
        String fLabel = FormatUtils.formatMetric(frequency, "Hz");
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

    @Override
    public double getResistance() {
        return 0;
    }
    
    @Override
    public Complex getImpedance(double frequency) {
    	return new Complex(0, 0); // Ideal source
    }
}