package components;

import utils.Complex;
import utils.FormatUtils;
import java.awt.*;

public class Resistor extends Components {
    private double resistance; // ohms

    public Resistor(String id, int x, int y, double resistance) {
        super(id, x, y);
        this.resistance = resistance;
    }

    public Resistor(String id, int x, int y) {
        this(id, x, y, 0.00);
    }

    @Override
    public void draw(Graphics2D g2) {
        super.draw(g2);
        FormatUtils.drawCenteredString(g2, "R: " + FormatUtils.formatMetric(resistance, "Ω"), new Rectangle(x - width/2, y - height/2, width, height));
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