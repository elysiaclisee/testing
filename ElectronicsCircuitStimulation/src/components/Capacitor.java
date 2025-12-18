package components;

import java.awt.*;

public class Capacitor extends Components {
    private double capacitance; 

    public Capacitor(String id, int x, int y, double capacitance) {
        super(id, x, y);
        this.capacitance = capacitance;
    }

    public Capacitor(String id, int x, int y) {
        this(id, x, y, 1e-6);
    }

    @Override
    public Complex getImpedance(double frequency) {
        // ZC = -1 / (2 * pi * f * C) (Phần ảo âm)
        if (frequency <= 1e-9) {
            // Tần số thấp (DC) -> Hở mạch (trở kháng ảo cực lớn)
            return new Complex(0, -1e9); 
        }
        double xc = -1.0 / (2 * Math.PI * frequency * capacitance);
        return new Complex(0, xc);
    }

    @Override
    public void draw(Graphics2D g2) {
        draw(g2, Color.CYAN);
        g2.setColor(Color.BLACK);
        g2.setFont(g2.getFont().deriveFont(12f));
        String label = "C: " + formatDouble(capacitance) + "F";
        drawCenteredString(g2, label, new Rectangle(x-width/2, y-height/2, width, height));
    }

    private String formatDouble(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        } else {
            return String.format("%.2e", d); // Dùng định dạng E cho điện dung nhỏ
        }
    }

    private void drawCenteredString(Graphics2D g2, String text, Rectangle rect) {
        FontMetrics fm = g2.getFontMetrics();
        int x = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int y = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, x, y);
    }
    public double getCapacitance() {
        return capacitance;
    }
    @Override
    public double getResistance() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x - width / 2, y - height / 2, width, height);
    }
}