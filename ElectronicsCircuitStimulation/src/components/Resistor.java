package components;

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
        drawCenteredString(g2, "R: " + (int)resistance + "Ω", new Rectangle(x - width/2, y - height/2, width, height));
    }

    private void drawCenteredString(Graphics2D g2, String text, Rectangle rect) {
        FontMetrics fm = g2.getFontMetrics();
        int x = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int y = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, x, y);
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

    public void setResistance(double r) {
        this.resistance = r;
    }

	@Override
	public Rectangle getBounds() {
	    return new Rectangle(x - width / 2, y - height / 2, width, height);
	}

}