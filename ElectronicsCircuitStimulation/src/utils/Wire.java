package utils;

import components.Components;
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

    public void draw(Graphics2D g2) {
        // 1. Get exact dot positions
        Point p1 = getConnectorPointSafe(a, b);
        Point p2 = getConnectorPointSafe(b, a);
        
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    private Point getConnectorPointSafe(Components source, Components target) {
        Point p = source.getConnectorPoint(target);
        return (p != null) ? p : source.getPosition();
    }

    public Components getA() { return a; }
    public Components getB() { return b; }
    public Type getType() { return type; }
}