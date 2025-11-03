package components;

import java.awt.*;

public class Bulb extends Components {
    private double resistance;

    public Bulb(String id, int x, int y, double resistance) {
        super(id, x, y);
        this.resistance = resistance;
        this.width = 40;
        this.height = 40;
    }

    public Bulb(String id, int x, int y) {
        this(id, x, y, 220.0);
    }

    @Override
    public void draw(Graphics2D g2) {
        int d = Math.max(width, height);
        int left = x - d/2;
        int top = y - d/2;
        g2.setColor(Color.YELLOW);
        g2.fillOval(left, top, d, d);
        g2.setColor(Color.BLACK);
        g2.drawOval(left, top, d, d);
        if (selected) {
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(left-4, top-4, d+8, d+8);
        }
        g2.setColor(Color.BLACK);
        g2.setFont(g2.getFont().deriveFont(12f));
        FontMetrics fm = g2.getFontMetrics();
        String s = "Bulb";
        int sx = x - fm.stringWidth(s)/2;
        int sy = y + d/2 + fm.getAscent();
        g2.drawString(s, sx, sy);
    }

    @Override
    public double getResistanceOhms() {
        return resistance;
    }
}