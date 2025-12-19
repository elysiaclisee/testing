package components;

import utils.Complex;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Bulb extends Components {
    // --- CÁC ĐẠI LƯỢNG CỐ ĐỊNH (RATED VALUES) ---
    public static final double V_RATED = 220.0; 
    public static final double P_RATED = 50.0;  
    // R = V^2 / P = 220^2 / 50 = 968 Ω
    public static final double R_BULB = (V_RATED * V_RATED) / P_RATED;

    private boolean isLighted = false;

    public Bulb(String id, int x, int y) {
        super(id, x, y);
        this.width = 60;
        this.height = 40;
    }

    @Override
    public Complex getImpedance(double frequency) {
        return new Complex(R_BULB, 0);
    }

    public void setLighted(boolean lighted) {
        this.isLighted = lighted;
    }

    @Override
    public double getResistance() {
        return R_BULB;
    }

    @Override
    public void draw(Graphics2D g2) {
        Color c = isLighted ? Color.YELLOW : Color.DARK_GRAY;
        super.draw(g2, c); 
        
        g2.setColor(isLighted ? Color.BLACK : Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(12f));
        
        // Hiển thị thông số cố định định mức
        String vLabel = "220V";
        String pLabel = "50W";
        
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D vBounds = fm.getStringBounds(vLabel, g2);
        Rectangle2D pBounds = fm.getStringBounds(pLabel, g2);
        
        int vx = x - (int) vBounds.getWidth() / 2;
        int vy = y - 2;
        int px = x - (int) pBounds.getWidth() / 2;
        int py = y + fm.getAscent() + 2;
        
        g2.drawString(vLabel, vx, vy);
        g2.drawString(pLabel, px, py);
        
        drawSelection(g2);
    }
}