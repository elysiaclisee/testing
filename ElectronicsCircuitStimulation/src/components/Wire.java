package components;

import java.awt.*;

public class Wire {
    public enum Type { SERIES, PARALLEL }
    private Components a, b;
    private Type type;

    public Wire(Components a, Components b, Type type) {
        this.a = a;
        this.b = b;
        this.type = type;
    }

    public Components getA() { return a; }
    public Components getB() { return b; }
    public Type getType() { return type; }

    public void draw(Graphics2D g2) {
        Point pa = a.getPosition();
        Point pb = b.getPosition();
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(3f));
        if (type == Type.SERIES) {
            // simple straight line for series
            g2.drawLine(pa.x, pa.y, pb.x, pb.y);
            int mx = (pa.x + pb.x) / 2;
            int my = (pa.y + pb.y) / 2;
            g2.setColor(Color.BLACK);
            g2.setFont(g2.getFont().deriveFont(12f));
            g2.drawString("S", mx + 6, my - 6);
            return;
        }

        // PARALLEL: draw a rectangular loop that passes above/below the two components
        int padding = 30;
        int left = Math.min(pa.x, pb.x) - padding;
        int right = Math.max(pa.x, pb.x) + padding;
        int top = Math.min(pa.y, pb.y) - padding;
        int bottom = Math.max(pa.y, pb.y) + padding;

        // draw rectangle loop
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(left, top, right - left, bottom - top);

        // draw short connectors from component centers to rectangle sides
        // connector for a
        int axConnectorX = (pa.x < (left + right)/2) ? left : right;
        int ay = pa.y;
        g2.drawLine(pa.x, pa.y, axConnectorX, ay);
        // connector for b
        int bxConnectorX = (pb.x < (left + right)/2) ? left : right;
        int by = pb.y;
        g2.drawLine(pb.x, pb.y, bxConnectorX, by);

        // label P near top midpoint
        int mx = (left + right) / 2;
        int my = top;
        g2.setColor(Color.BLACK);
        g2.setFont(g2.getFont().deriveFont(12f));
        g2.drawString("P", mx + 6, my - 6);
    }
}