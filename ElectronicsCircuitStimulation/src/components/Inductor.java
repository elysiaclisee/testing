package components;

import utils.Complex;
import utils.FormatUtils;
import java.awt.*;

public class Inductor extends Components {
    private double inductance; 

    public Inductor(String id, int x, int y, double inductance) {
        super(id, x, y);
        this.inductance = inductance;
    }

    public double getInductance() {
        return inductance;
    }

    @Override
    public void draw(Graphics2D g2) {
        draw(g2, new Color(200, 200, 255)); 
        String label = "L: " + FormatUtils.formatMetric(inductance, "H");
        g2.setFont(g2.getFont().deriveFont(12f));
        FormatUtils.drawCenteredString(g2, label, new Rectangle(x - width / 2, y - height / 2, width, height));
    }

    @Override
    public double getResistance() {
        return 0;
    }
    
    @Override
    public Complex getImpedance(double frequency) {
        // ZL = R_internal + j(2 * pi * f * L)
        double xl = 2.0 * Math.PI * frequency * inductance;
        return new Complex(0.1, xl); // 0.1 là nội trở dây dẫn cố định
    }
}