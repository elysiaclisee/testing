package components;

import java.awt.*;

public abstract class Components implements Cloneable {
    protected int x, y;
    protected int width = 60;
    protected int height = 30;
    protected boolean selected = false;
    protected String id;
    
    // Physical Properties
    protected double resistanceOhms; // Base DC resistance (for resistors/inductors)

    // Simulation State (Calculated during simulation)
    protected double voltageDrop = 0.0;
    protected double currentFlow = 0.0;

    public Components(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    // --- Core Physics Methods ---

    /**
     * Calculates Impedance (Z) based on frequency.
     * @param frequency Hz (0 for DC)
     * @return Impedance in Ohms
     */
    public abstract double getImpedance(double frequency);

    /**
     * Distributes Voltage and Current to this component (and children if composite).
     * @param voltage The voltage drop across this component.
     * @param current The current flowing through this component.
     * @param frequency The frequency of the source (needed for distribution ratios).
     */
    public void setSimulationState(double voltage, double current, double frequency) {
        this.voltageDrop = voltage;
        this.currentFlow = current;
    }

    // --- Getters & Setters ---

    public double getVoltageDrop() { return voltageDrop; }
    public double getCurrentFlow() { return currentFlow; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point getPosition() {
        return new Point(x, y);
    }

    public boolean contains(Point p) {
        Rectangle r = new Rectangle(x - width/2, y - height/2, width, height);
        return r.contains(p);
    }

    public void setSelected(boolean sel) {
        this.selected = sel;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getId() {
        return id;
    }

    // kept for compatibility, but getImpedance is preferred
    public abstract double getResistanceOhms(); 
    public abstract Rectangle getBounds();

    // --- Drawing ---

    public void draw(Graphics2D g2) {
        draw(g2, Color.LIGHT_GRAY);
    }

    public void draw(Graphics2D g2, Color fillColor) {
        int w = width;
        int h = height;
        int left = x - w / 2;
        int top = y - h / 2;

        g2.setColor(fillColor);
        g2.fillRoundRect(left, top, w, h, 8, 8);
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(left, top, w, h, 8, 8);

        if (selected) {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            Rectangle bounds = getBounds();
            g2.draw(new Rectangle(bounds.x - 2, bounds.y - 2, bounds.width + 4, bounds.height + 4));
            g2.setStroke(new BasicStroke(1));
        }
    }

    @Override
    public Components clone() {
        try {
            return (Components) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); 
        }
    }
}