package components;

import java.awt.*;

/**
 * Base class for a component placed on the board.
 */
public abstract class Components implements Cloneable {
    protected int x, y; // center position
    protected int width = 60;
    protected int height = 30;
    protected boolean selected = false;
    protected String id;
    protected double resistanceOhms;

    public Components(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

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

    // return the (DC) equivalent resistance in ohms for this component when used as a resistor
    public abstract double getResistanceOhms();

    public abstract Rectangle getBounds();

    // draw the component
    public void draw(Graphics2D g2) {
        if (selected) {
            g2.setColor(Color.RED);
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
            throw new AssertionError(); // Should not happen
        }
    }
}