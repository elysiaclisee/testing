package components;

import java.awt.*;

public class Inductor extends Components {
    private double inductance; 

    public Inductor(String id, int x, int y, double inductance) {
        super(id, x, y);
        this.inductance = inductance;
        this.resistanceOhms = 0.1; // Small internal wire resistance
    }

    @Override
    public double getImpedance(double frequency) {
        // Z = 2 * pi * f * L + R_internal
        return (2.0 * Math.PI * frequency * inductance) + resistanceOhms;
    }

    public double getInductance() {
        return inductance;
    }

    @Override
    public void draw(Graphics2D g2) {
        draw(g2, new Color(200, 200, 255)); 

        String label = "L: " + formatDouble(inductance) + "H";
        g2.setFont(g2.getFont().deriveFont(12f));
        drawCenteredString(g2, label, new Rectangle(x - width / 2, y - height / 2, width, height));
    }

    private void drawCenteredString(Graphics2D g2, String text, Rectangle rect) {
        FontMetrics fm = g2.getFontMetrics();
        int x = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int y = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, x, y);
    }

    private String formatDouble(double d) {
        if (d == (long) d) return String.format("%d", (long) d);
        else return String.format("%.2f", d);
    }

    @Override
    public double getResistanceOhms() {
        return resistanceOhms;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x - width / 2, y - height / 2, width, height);
    }
}