package components;

import java.awt.*;
import java.util.List;

public class CompositeComponent extends Components {
    public enum Mode { SERIES, PARALLEL }
    private final List<Components> parts;
    private final Mode mode;

    public CompositeComponent(String id, int x, int y, Mode mode, List<Components> parts) {
        super(id, x, y);
        this.parts = parts;
        this.mode = mode;
        this.width = 100;
        this.height = 60;
    }

    public List<Components> getParts() {
        return parts;
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public void draw(Graphics2D g2) {
        int left = x - width/2;
        int top = y - height/2;
        // draw a box representing the composite
        g2.setColor(new Color(220, 220, 250));
        g2.fillRect(left, top, width, height);
        g2.setColor(Color.BLACK);
        g2.drawRect(left, top, width, height);

        g2.setFont(g2.getFont().deriveFont(12f));
        String label = mode == Mode.SERIES ? "Series" : "Parallel";
        FontMetrics fm = g2.getFontMetrics();
        int lx = x - fm.stringWidth(label)/2;
        int ly = y + fm.getAscent()/2;
        g2.drawString(label, lx, ly);

        if (selected) {
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(left-4, top-4, width+8, height+8);
        }
    }

    @Override
    public double getResistanceOhms() {
        if (parts == null || parts.isEmpty()) return Double.POSITIVE_INFINITY;
        if (parts.size() == 1) return parts.get(0).getResistanceOhms();
        if (mode == Mode.SERIES) {
            double sum = 0.0;
            for (Components c : parts) {
                double r = c.getResistanceOhms();
                if (Double.isInfinite(r)) return Double.POSITIVE_INFINITY;
                sum += r;
            }
            return sum;
        } else { // PARALLEL
            double inv = 0.0;
            boolean anyFinite = false;
            for (Components c : parts) {
                double r = c.getResistanceOhms();
                if (Double.isInfinite(r)) continue;
                anyFinite = true;
                inv += 1.0 / r;
            }
            if (!anyFinite) return Double.POSITIVE_INFINITY;
            return 1.0 / inv;
        }
    }
}
