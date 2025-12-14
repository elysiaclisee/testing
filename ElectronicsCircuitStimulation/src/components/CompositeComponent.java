package components;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CompositeComponent extends Components {
    private List<Components> children = new ArrayList<>();
    private Mode mode = Mode.SERIES;

    public enum Mode {
        SERIES, PARALLEL
    }

    public CompositeComponent(String id, int x, int y) {
        super(id, x, y);
    }

    public CompositeComponent(String id, int x, int y, Mode mode, List<Components> parts) {
        super(id, x, y);
        this.mode = mode;
        this.children = parts;
        this.width = 100;
        this.height = 60;
    }

    public void add(Components component) {
        children.add(component);
    }

    public List<Components> getChildren() {
        return children;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
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
        if (children == null || children.isEmpty()) return Double.POSITIVE_INFINITY;
        if (children.size() == 1) return children.get(0).getResistanceOhms();
        if (mode == Mode.SERIES) {
            double sum = 0.0;
            for (Components c : children) {
                double r = c.getResistanceOhms();
                if (Double.isInfinite(r)) return Double.POSITIVE_INFINITY;
                sum += r;
            }
            return sum;
        } else { // PARALLEL
            double inv = 0.0;
            boolean anyFinite = false;
            for (Components c : children) {
                double r = c.getResistanceOhms();
                if (Double.isInfinite(r)) continue;
                anyFinite = true;
                inv += 1.0 / r;
            }
            if (!anyFinite) return Double.POSITIVE_INFINITY;
            return 1.0 / inv;
        }
    }

	@Override
	public Rectangle getBounds() {
	    return new Rectangle(x - width / 2, y - height / 2, width, height);
	}
}