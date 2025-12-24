package components;

import utils.Complex;
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
    protected Color getFillColor() {
        return new Color(200, 200, 255);
    }

    @Override
    protected String getLabel() {
        return "L: " + utils.FormatUtils.formatMetric(inductance, "H");
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