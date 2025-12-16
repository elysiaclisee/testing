package components;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompositeComponent extends Components {
    private final List<Components> children = new ArrayList<>();
    private Mode mode = Mode.SERIES;

    public enum Mode { SERIES, PARALLEL }

    public CompositeComponent(String id, int x, int y) {
        super(id, x, y);
    }

    public void add(Components c) {
        if (c == null) return;
        children.add(c);
    }

    public List<Components> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void setMode(Mode mode) {
        if (mode != null) this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public double getResistanceOhms() {
        return getImpedance(0); // DC mặc định
    }

    @Override
    public double getImpedance(double frequency) {
        if (children.isEmpty()) return Double.POSITIVE_INFINITY;

        if (mode == Mode.SERIES) {
            double total = 0.0;
            for (Components c : children) total += c.getImpedance(frequency);
            return total;
        } else { // PARALLEL
            double inv = 0.0;
            boolean hasShort = false;

            for (Components c : children) {
                double z = c.getImpedance(frequency);
                if (z <= 1e-9) hasShort = true;
                if (!Double.isInfinite(z) && z > 0) inv += 1.0 / z;
            }

            if (hasShort) return 0.0;
            if (inv == 0.0) return Double.POSITIVE_INFINITY;
            return 1.0 / inv;
        }
    }

    @Override
    public Rectangle getBounds() {
        // Composite không vẽ trên board, nên trả về rỗng cho an toàn
        return new Rectangle(0, 0, 0, 0);
    }
}
