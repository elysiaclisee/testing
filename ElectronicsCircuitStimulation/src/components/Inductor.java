package components;

import java.awt.*;

public class Inductor extends Components {
    private double inductance; // In Henrys

    public Inductor(String id, int x, int y, double inductance) {
        super(id, x, y);
        this.inductance = inductance;
        this.resistanceOhms = 0.1; // Default resistance for the inductor coil
    }

    public double getInductance() {
        return inductance;
    }

    @Override
    public void draw(Graphics2D g2) {
        super.draw(g2); // Draws the selection halo
        g2.setColor(Color.BLUE);
        g2.drawLine(x, y, x + 20, y);
        for (int i = 0; i < 4; i++) {
            g2.drawArc(x + 20 + i * 10, y - 5, 10, 10, 0, 180);
        }
        g2.drawLine(x + 60, y, x + 80, y);
        String label = formatDouble(inductance) + "H";
        g2.drawString(label, x + 35, y - 10);
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
        return resistanceOhms;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y - 5, 80, 10);
    }
}
