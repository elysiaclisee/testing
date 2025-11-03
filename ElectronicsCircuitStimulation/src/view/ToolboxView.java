package view;

import controller.Toolbox;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * View helper that draws a toolbox rectangle with three horizontally arranged tool slots.
 */
public class ToolboxView {
    public static final int X = 10;
    public static final int Y = 10;
    public static final int WIDTH = 360;
    public static final int HEIGHT = 80;
    private final Rectangle[] slots = new Rectangle[3];

    public ToolboxView() {
        int padding = 10;
        int slotWidth = (WIDTH - padding * 4) / 3;
        int slotHeight = HEIGHT - padding * 2;
        for (int i = 0; i < 3; i++) {
            int sx = X + padding + i * (slotWidth + padding);
            int sy = Y + padding;
            slots[i] = new Rectangle(sx, sy, slotWidth, slotHeight);
        }
    }

    public void draw(Graphics2D g2) {
        // background
        g2.setColor(new Color(230, 230, 230));
        g2.fillRect(X, Y, WIDTH, HEIGHT);
        g2.setColor(Color.GRAY);
        g2.drawRect(X, Y, WIDTH, HEIGHT);

        // draw 3 tool slots horizontally
        g2.setFont(g2.getFont().deriveFont(14f));
        String[] labels = new String[] {"Resistor", "Capacitor", "Bulb"};
        for (int i = 0; i < 3; i++) {
            Rectangle r = slots[i];
            g2.setColor(Color.WHITE);
            g2.fill(r);
            g2.setColor(Color.BLACK);
            g2.draw(r);
            // simple icon
            int cx = r.x + r.width/2;
            int cy = r.y + r.height/2 - 6;
            if (i == 0) { // resistor icon
                g2.setColor(Color.LIGHT_GRAY);
                g2.fillRoundRect(cx - 24, cy - 10, 48, 20, 6, 6);
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(cx - 24, cy - 10, 48, 20, 6, 6);
            } else if (i == 1) { // capacitor icon
                g2.setColor(Color.CYAN);
                g2.fillRect(cx - 20, cy - 10, 40, 20);
                g2.setColor(Color.BLACK);
                g2.drawRect(cx - 20, cy - 10, 40, 20);
            } else { // bulb
                g2.setColor(Color.YELLOW);
                g2.fillOval(cx - 14, cy - 14, 28, 28);
                g2.setColor(Color.BLACK);
                g2.drawOval(cx - 14, cy - 14, 28, 28);
            }

            // label
            Rectangle2D bounds = g2.getFontMetrics().getStringBounds(labels[i], g2);
            int lx = r.x + (r.width - (int)bounds.getWidth())/2;
            int ly = r.y + r.height - 6;
            g2.setColor(Color.BLACK);
            g2.drawString(labels[i], lx, ly);
        }
    }

    // returns the tool clicked, or null
    public controller.Toolbox.Tool hitTool(Point p) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i].contains(p)) {
                switch (i) {
                    case 0: return Toolbox.Tool.RESISTOR;
                    case 1: return Toolbox.Tool.CAPACITOR;
                    case 2: return Toolbox.Tool.BULB;
                }
            }
        }
        return null;
    }
}
