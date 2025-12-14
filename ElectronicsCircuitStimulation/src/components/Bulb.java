package components;

import java.awt.*;

public class Bulb extends Components {
    private double resistance;
    private boolean isLighted = false;
    private double powerLimit;

    public Bulb(String id, int x, int y, double resistance, double powerLimit) {
        super(id, x, y);
        this.resistance = resistance;
        this.powerLimit = powerLimit;
    }

    public Bulb(String id, int x, int y) {
        this(id, x, y, 220.0, 100.0); // Default resistance and power limit
    }

    public void setLighted(boolean lighted) {
        isLighted = lighted;
    }

    public double getPowerLimit() {
        return powerLimit;
    }

    @Override
    public void draw(Graphics2D g2) {
        Color fillColor = isLighted ? Color.YELLOW : Color.DARK_GRAY;
        draw(g2, fillColor);

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(12f));
        FontMetrics fm = g2.getFontMetrics();
        String s = "Bulb";
        int sx = x - fm.stringWidth(s)/2;
        int sy = y + fm.getAscent()/2;
        g2.drawString(s, sx, sy);
    }

    @Override
    public double getResistanceOhms() {
        return resistance;
    }

	@Override
	public Rectangle getBounds() {
		return new Rectangle(x - width / 2, y - height / 2, width, height);
	}
}