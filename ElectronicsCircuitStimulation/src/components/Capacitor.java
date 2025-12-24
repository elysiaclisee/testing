package components;

import utils.Complex;
import java.awt.*;

public class Capacitor extends Components {
    private double capacitance; 

    public Capacitor(String id, int x, int y, double capacitance) {
        super(id, x, y);
        this.capacitance = capacitance;
    }

    @Override
    public Complex getImpedance(double frequency) {
        // ZC = -1 / (2 * pi * f * C) (Phần ảo âm)
        if (frequency <= 1e-9) {
            // Tần số thấp (DC) -> Hở mạch (trở kháng ảo cực lớn)
            return new Complex(0, -1e9); 
        }
        double xc = -1.0 / (2 * Math.PI * frequency * capacitance);
        return new Complex(0, xc);
    }

    @Override
    protected Color getFillColor() {
        return Color.CYAN;
    }

    @Override
    protected String getLabel() {
        return "C: " + utils.FormatUtils.formatMetric(capacitance, "F");
    }
    
    public double getCapacitance() {
        return capacitance;
    }

	@Override
	public double getResistance() {
		return 0;
	}
}