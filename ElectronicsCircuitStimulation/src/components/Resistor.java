package components;

import java.awt.*;

public class Resistor extends Components {
    private double resistance; // ohms

    public Resistor(String id, int x, int y, double resistance) {
        super(id, x, y);
        this.resistance = resistance;
    }

    public Resistor(String id, int x, int y) {
        this(id, x, y, 100.0);
    }

    @Override
    public void draw(Graphics2D g2) {
        int w = width;
        int h = height;
        int left = x - w/2;
        int top = y - h/2;
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRoundRect(left, top, w, h, 8, 8);
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(left, top, w, h, 8, 8);
        String label = "R\n" + (int)resistance + "Ω";
        g2.setFont(g2.getFont().deriveFont(12f));
        drawCenteredString(g2, "R " + (int)resistance + "Ω", new Rectangle(left, top, w, h));
        if (selected) {
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(left-4, top-4, w+8, h+8);
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
        return resistance;
    }

    public void setResistance(double r) {
        this.resistance = r;
    }

	@Override
	public Rectangle getBounds() {
	    return new Rectangle(x - width / 2, y - height / 2, width, height);
	}
}