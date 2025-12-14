package components;

import java.awt.*;

public class Capacitor extends Components {
    private double capacitance; 

    public Capacitor(String id, int x, int y, double capacitance) {
        super(id, x, y);
        this.capacitance = capacitance;
    }

    public Capacitor(String id, int x, int y) {
        this(id, x, y, 1e-6);
    }

    @Override
    public void draw(Graphics2D g2) {
        draw(g2, Color.CYAN);
        g2.setColor(Color.BLACK);
        g2.setFont(g2.getFont().deriveFont(12f));
        String label = "C: " +formatDouble(capacitance) + "F";
        drawCenteredString(g2, label, new Rectangle(x-width/2, y-height/2, width, height));
    }

    private String formatDouble(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        } else {
            return String.format("%.2f", d);
        }
    }

    private void drawCenteredString(Graphics2D g2, String text, Rectangle rect) {
        FontMetrics fm = g2.getFontMetrics();
        int x = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int y = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, x, y);
    }

    @Override
    public double getResistanceOhms() {
        // For DC steady-state a capacitor is an open circuit => infinite resistance
        return Double.POSITIVE_INFINITY;
    }

    public double getCapacitance() {
        return capacitance;
    }

	@Override
	public Rectangle getBounds() {
		return new Rectangle(x - width / 2, y - height / 2, width, height);
	}
}