package components;

import utils.Complex;
import java.awt.*;

public abstract class Components {
    protected int x, y; // center position
    protected int width = 60;
    protected int height = 30;
    protected boolean selected = false;
    protected String id;
    protected double voltageDrop = 0.0;
    protected double currentFlow = 0.0;
    protected abstract Color getFillColor();
    protected abstract String getLabel();

    public Components(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }
    public abstract Complex getImpedance(double frequency);

    public Point getPosition() {
        return new Point(x, y);
    }

    public abstract double getResistance();

    public double getVoltageDrop() {
        return voltageDrop;
    }

    public double getCurrentFlow() {
        return currentFlow;
    }
    
    /**
     * Default implementation returns a rectangle centered at (x, y) with the component's width and height.
     * Override this method if your component needs custom bounds calculation.
     */
    public Rectangle getBounds() {
        return new Rectangle(x - width / 2, y - height / 2, width, height);
    }
    
    public String getId() {
        return id;
    }
    
    public void setSelected(boolean sel) {
        this.selected = sel;
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void setSimulationState(double voltage, double current, double frequency) {
        this.voltageDrop = voltage;
        this.currentFlow = current;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public boolean contains(Point p) {
        Rectangle r = new Rectangle(x - width/2, y - height/2, width, height);
        return r.contains(p);
    }
    
    protected void drawSelection(Graphics2D g2) {
        if (selected) {

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            
            Rectangle bounds = getBounds();
            if (bounds != null) {
                g2.draw(new Rectangle(bounds.x - 2, bounds.y - 2, bounds.width + 4, bounds.height + 4));
            }
            g2.setStroke(new BasicStroke(1));
        }
    } //later can inherit for different shapes 

    public void draw(Graphics2D g2) {
        int w = width;
        int h = height;
        int left = x - w / 2;
        int top = y - h / 2;
        Rectangle rect = new Rectangle(left, top, w, h);

        g2.setColor(getFillColor());
        g2.fillRect(left, top, w, h);
        
        g2.setColor(Color.BLACK);
        g2.drawRect(left, top, w, h);

        drawSelection(g2);

        g2.setFont(g2.getFont().deriveFont(12f)); 
        utils.FormatUtils.drawCenteredString(g2, getLabel(), rect); //centered text
    }

    public Point getConnectorPoint(Components other) {
        Point p1 = this.getPosition();
        Point p2 = other.getPosition();
        
        // Calculate angle between components
        double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);
        
        // Calculate point on an oval/ellipse inscribed in the component
        int cx = (int) (p1.x + (width / 2.0) * Math.cos(angle));
        int cy = (int) (p1.y + (height / 2.0) * Math.sin(angle));
        
        return new Point(cx, cy);
    }
}