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
    
    // Added helper constructor to match your usage
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
    public double getImpedance(double frequency) {
        if (children.isEmpty()) return Double.POSITIVE_INFINITY;

        if (mode == Mode.SERIES) {
            double totalZ = 0.0;
            for (Components c : children) {
                totalZ += c.getImpedance(frequency);
            }
            return totalZ;
        } else {
            double inverseZ = 0.0;
            boolean hasShort = false;

            for (Components c : children) {
                double z = c.getImpedance(frequency);
                if (Math.abs(z) < 1e-9) { hasShort = true; break; }
                if (!Double.isInfinite(z)) inverseZ += 1.0 / z;
            }

            if (hasShort) return 0.0; 
            if (inverseZ == 0.0) return Double.POSITIVE_INFINITY; 
            return 1.0 / inverseZ;
        }
    }

    @Override
    public void setSimulationState(double voltage, double current, double frequency) {
        super.setSimulationState(voltage, current, frequency); // Store state

        if (children.isEmpty()) return;

        if (mode == Mode.SERIES) {
            handleSeriesDistribution(voltage, current, frequency);
        } else {
            handleParallelDistribution(voltage, frequency);
        }
    }
    
    private void handleSeriesDistribution(double totalVoltage, double totalCurrent, double frequency) {
        int openComponents = 0;
        
        for(Components c : children) {
            if(Double.isInfinite(c.getImpedance(frequency))) openComponents++;
        }

        for (Components c : children) {
            double z = c.getImpedance(frequency);
            double vDrop;

            if (openComponents > 0) {
                // If open circuit, voltage appears across the open gap
                if (Double.isInfinite(z)) {
                    vDrop = totalVoltage / openComponents; 
                } else {
                    vDrop = 0.0; 
                }
            } else {
                vDrop = totalCurrent * z;
            }
            c.setSimulationState(vDrop, totalCurrent, frequency);
        }
    }

    private void handleParallelDistribution(double totalVoltage, double frequency) {
        for (Components c : children) {
            double z = c.getImpedance(frequency);
            double iBranch;
            if (Math.abs(z) < 1e-9) {
                // If shorted, it conceptually takes max current, but for sim safety:
                iBranch = (this.getImpedance(frequency) < 1e-9) ? this.currentFlow : 0;
            } else if (Double.isInfinite(z)) {
                iBranch = 0.0;
            } else {
                iBranch = totalVoltage / z;
            }
            c.setSimulationState(totalVoltage, iBranch, frequency);
        }
    }

    @Override
    public double getResistance() {
        return getImpedance(0);
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
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            Rectangle bounds = getBounds();
            g2.draw(new Rectangle(bounds.x - 2, bounds.y - 2, bounds.width + 4, bounds.height + 4));
            g2.setStroke(new BasicStroke(1));
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x - width / 2, y - height / 2, width, height);
    }
}