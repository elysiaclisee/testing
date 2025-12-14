package view;

import controller.Toolbox;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class ToolboxView {
    public static final int X = 10;
    public static final int Y = 10;
    public static final int WIDTH = 530;
    public static final int HEIGHT = 120;
    private static final int HEADER_HEIGHT = 30;

    private final Rectangle[] slots = new Rectangle[5];

    public ToolboxView() {
        int padding = 10;
        int numSlots = 5;
        int totalPadding = padding * (numSlots + 1);
        int slotWidth = (WIDTH - totalPadding) / numSlots;
        int slotHeight = HEIGHT - HEADER_HEIGHT - padding;

        for (int i = 0; i < numSlots; i++) {
            int sx = X + padding + i * (slotWidth + padding);
            int sy = Y + HEADER_HEIGHT;
            slots[i] = new Rectangle(sx, sy, slotWidth, slotHeight);
        }
    }

    public void draw(Graphics2D g2) {
        g2.setColor(new Color(230, 230, 230));
        g2.fillRect(X, Y, WIDTH, HEIGHT);
        g2.setColor(Color.GRAY);
        g2.drawRect(X, Y, WIDTH, HEIGHT);

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Toolbox", X + 10, Y + 20);

        drawTool(g2, slots[0], "Battery");
        drawTool(g2, slots[1], "Resistor");
        drawTool(g2, slots[2], "Bulb");
        drawTool(g2, slots[3], "Capacitor");
        drawTool(g2, slots[4], "Inductor");
    }

    private void drawTool(Graphics2D g2, Rectangle r, String label) {
        g2.setColor(Color.WHITE);
        g2.fill(r);
        g2.setColor(Color.BLACK);
        g2.draw(r);

        int cx = r.x + r.width / 2;
        int cy = r.y + r.height / 2 - 6;

        switch (label) {
            case "Resistor" -> {
                g2.setColor(Color.LIGHT_GRAY);
                g2.fillRoundRect(cx - 24, cy - 10, 48, 20, 6, 6);
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(cx - 24, cy - 10, 48, 20, 6, 6);
            }
            case "Capacitor" -> {
                g2.setColor(Color.CYAN);
                g2.fillRect(cx - 20, cy - 10, 40, 20);
                g2.setColor(Color.BLACK);
                g2.drawRect(cx - 20, cy - 10, 40, 20);
            }
            case "Bulb" -> {
                g2.setColor(Color.YELLOW);
                g2.fillOval(cx - 14, cy - 14, 28, 28);
                g2.setColor(Color.BLACK);
                g2.drawOval(cx - 14, cy - 14, 28, 28);
            }
            case "Battery" -> {
                g2.setColor(Color.GREEN);
                g2.fillRect(cx - 20, cy - 10, 40, 20);
                g2.setColor(Color.BLACK);
                g2.drawRect(cx - 20, cy - 10, 40, 20);
                g2.drawLine(cx + 20, cy - 5, cx + 25, cy - 5);
                g2.drawLine(cx + 22, cy - 8, cx + 22, cy - 2);
            }
            case "Inductor" -> {
                g2.setColor(Color.GREEN);
                g2.drawArc(cx - 15, cy - 15, 30, 30, 0, 180);
                g2.drawArc(cx - 15, cy - 15, 30, 30, 180, 180);
            }
        }

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        Rectangle2D bounds = g2.getFontMetrics().getStringBounds(label, g2);
        int lx = r.x + (r.width - (int) bounds.getWidth()) / 2;
        int ly = r.y + r.height - 6;
        g2.setColor(Color.BLACK);
        g2.drawString(label, lx, ly);
    }

    public controller.Toolbox.Tool hitTool(Point p) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i].contains(p)) {
                switch (i) {
                    case 0 -> {
                        return Toolbox.Tool.BATTERY;
                    }
                    case 1 -> {
                        return Toolbox.Tool.RESISTOR;
                    }
                    case 2 -> {
                        return Toolbox.Tool.BULB;
                    }
                    case 3 -> {
                        return Toolbox.Tool.CAPACITOR;
                    }
                    case 4 -> {
                        return Toolbox.Tool.INDUCTOR;
                    }
                }
            }
        }
        return null;
    }
}