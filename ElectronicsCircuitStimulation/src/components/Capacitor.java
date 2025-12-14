package components;

import java.awt.*;

public class Capacitor extends Components {
    private double capacitance; // farads (not actively used for DC resistance)

    public Capacitor(String id, int x, int y, double capacitance) {
        super(id, x, y);
        this.capacitance = capacitance;
        this.width = 50;
        this.height = 30;
    }

    public Capacitor(String id, int x, int y) {
        this(id, x, y, 1e-6);
    }

    @Override
    public void draw(Graphics2D g2) {
        int w = width;
        int h = height;
        int left = x - w/2;
        int top = y - h/2;
        g2.setColor(Color.CYAN);
        g2.fillRect(left, top, w, h);
        g2.setColor(Color.BLACK);
        g2.drawRect(left, top, w, h);
        g2.setFont(g2.getFont().deriveFont(12f));
        String label = formatDouble(capacitance) + "F";
        drawCenteredString(g2, label, new Rectangle(left, top, w, h));

        if (selected) {
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(left-4, top-4, w+8, h+8);
        }
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
		// TODO Auto-generated method stub
		return null;
	}
}