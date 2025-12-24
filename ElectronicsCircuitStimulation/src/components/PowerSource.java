package components;

import utils.Complex;
import utils.FormatUtils;
import java.awt.*;

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
            super.draw(g2);
            
            g2.setColor(Color.BLACK);
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
            
            String vLabel = FormatUtils.formatMetric(voltage, "V");
            String fLabel = FormatUtils.formatMetric(frequency, "Hz");
            
            FontMetrics fm = g2.getFontMetrics();
            int vx = x - (int) fm.getStringBounds(vLabel, g2).getWidth() / 2;
            int vy = y - 2;
            int fx = x - (int) fm.getStringBounds(fLabel, g2).getWidth() / 2;
            int fy = y + fm.getAscent() + 2;
            
            g2.drawString(vLabel, vx, vy);
            g2.drawString(fLabel, fx, fy);
        }

    @Override
    protected String getLabel() {
        return ""; 
    }
    
    @Override
    protected Color getFillColor() {
        return Color.GREEN;
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