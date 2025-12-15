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

    // --- Recursive Physics Calculations ---

    @Override
    public double getImpedance(double frequency) {
        if (children == null || children.isEmpty()) return Double.POSITIVE_INFINITY;
        
        if (mode == Mode.SERIES) {
            double totalZ = 0.0;
            for (Components c : children) {
                totalZ += c.getImpedance(frequency);
            }
            return totalZ;
        } else { // PARALLEL
            double inverseZ = 0.0;
            boolean hasShort = false;
            
            for (Components c : children) {
                double z = c.getImpedance(frequency);
                
                // Check for short circuit (0 impedance)
                if (Math.abs(z) < 1e-9) hasShort = true;
                
                // Add admittance (1/Z) only if Z is valid
                if (!Double.isInfinite(z) && z > 1e-9) {
                    inverseZ += 1.0 / z;
                }
            }
            
            if (hasShort) return 0.0; // Short circuit dominates parallel
            if (inverseZ == 0.0) return Double.POSITIVE_INFINITY; // Open circuit
            return 1.0 / inverseZ;
        }
    }

    @Override
    public void setSimulationState(double voltage, double current, double frequency) {
        // 1. Store state for this container
        super.setSimulationState(voltage, current, frequency);

        if (children.isEmpty()) return;

        if (mode == Mode.SERIES) {
            // SERIES: Current is constant, Voltage splits based on Impedance
            // I_child = I_total
            // V_child = I_total * Z_child
            
            for (Components c : children) {
                double childZ = c.getImpedance(frequency);
                double childV;
                
                if (Double.isInfinite(childZ)) {
                     // If one component is open in series, it takes full voltage
                     childV = voltage; 
                } else {
                     childV = current * childZ;
                }
                
                c.setSimulationState(childV, current, frequency);
            }
            
        } else {
            // PARALLEL: Voltage is constant, Current splits based on Impedance
            // V_child = V_total
            // I_child = V_total / Z_child
            
            for (Components c : children) {
                double childZ = c.getImpedance(frequency);
                double childI;
                
                if (childZ < 1e-9) {
                    // Avoid divide by zero on short circuit
                    childI = current; // Simplification: short takes all available current
                } else if (Double.isInfinite(childZ)) {
                    childI = 0.0;
                } else {
                    childI = voltage / childZ;
                }
                
                c.setSimulationState(voltage, childI, frequency);
            }
        }
    }
    
    // --- Standard Methods ---

    @Override
    public double getResistanceOhms() {
        return getImpedance(0); // DC resistance
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