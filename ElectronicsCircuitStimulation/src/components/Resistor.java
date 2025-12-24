package components;

import utils.Complex;
import java.awt.*;

public class Resistor extends Components {
    private double resistance; 
    
    public Resistor(String id, int x, int y, double resistance) {
        super(id, x, y);
        this.resistance = resistance;
    }

    @Override
    protected Color getFillColor() {
        return Color.LIGHT_GRAY;
    }

    @Override
    protected String getLabel() {
        return "R: " + utils.FormatUtils.formatMetric(resistance, "Ω");
    }

    @Override
    public Complex getImpedance(double frequency) {
        // Trở thuần: Z = R + 0j
        return new Complex(resistance, 0);
    }

    @Override
    public double getResistance() {
        return resistance;
    }
}