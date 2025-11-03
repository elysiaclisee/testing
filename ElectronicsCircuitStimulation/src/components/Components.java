package components;

import java.awt.*;

/**
 * Base class for a component placed on the board.
 */
public abstract class Components {
    protected int x, y; // center position
    protected int width = 60;
    protected int height = 30;
    protected boolean selected = false;
    protected String id;

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

    // draw the component
    public abstract void draw(Graphics2D g2);

    // return the (DC) equivalent resistance in ohms for this component when used as a resistor
    public abstract double getResistanceOhms();
}