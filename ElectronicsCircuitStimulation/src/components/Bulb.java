package components;

import utils.Complex;
import java.awt.*;

public class Bulb extends Components {
    public static final double V_RATED = 220.0; 
    public static final double P_RATED = 50.0;  
    // R = V^2 / P = 220^2 / 50 = 968 Ω
    public static final double R_BULB = (V_RATED * V_RATED) / P_RATED;

    public Bulb(String id, int x, int y) {
        super(id, x, y);
        this.width = 60;
        this.height = 40;
    }

    @Override
    public Complex getImpedance(double frequency) {
        return new Complex(R_BULB, 0);
    }

    @Override
    public double getResistance() {
        return R_BULB;
    }

    @Override
    protected Color getFillColor() {
        return Color.DARK_GRAY;
    }

    @Override
    protected String getLabel() {
        return ""; // Return EMPTY so parent doesn't draw text. We will draw it ourselves.
    }
    @Override
    public void draw(Graphics2D g2) {
        // 1. Let parent draw the Box and Selection border
        super.draw(g2); 
        
        // 2. Custom Text Drawing (White Text)
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(12f));
        
        String vLabel = "220V";
        String pLabel = "50W";
        
        FontMetrics fm = g2.getFontMetrics();
        int vx = x - (int) fm.getStringBounds(vLabel, g2).getWidth() / 2;
        int vy = y - 2;
        int px = x - (int) fm.getStringBounds(pLabel, g2).getWidth() / 2;
        int py = y + fm.getAscent() + 2;
        
        g2.drawString(vLabel, vx, vy);
        g2.drawString(pLabel, px, py);
    }
}