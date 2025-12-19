package utils;

import java.awt.*;

public class FormatUtils {
    public static String formatMetric(double value, String unit) {
        if (Double.isInfinite(value) || Double.isNaN(value)) return "- " + unit;
        if (value == 0) return "0 " + unit;
        double abs = Math.abs(value);
        String prefix = "";
        double scaled = value;
        if (abs >= 1_000_000) { scaled /= 1_000_000; prefix = "M"; }
        else if (abs >= 1_000) { scaled /= 1_000; prefix = "k"; }
        else if (abs >= 1) { prefix = ""; }
        else if (abs >= 0.001) { scaled *= 1_000; prefix = "m"; }
        else if (abs >= 0.000_001) { scaled *= 1_000_000; prefix = "µ"; }
        else if (abs >= 0.000_000_001) { scaled *= 1_000_000_000; prefix = "n"; }
        String s = String.format("%.2f", scaled);
        if (s.endsWith(".00")) s = s.substring(0, s.length() - 3);
        else if (s.endsWith("0") && s.contains(".")) s = s.substring(0, s.length() - 1);
        return s + " " + prefix + unit;
    }
    
    /**
     * Draw text centered within a rectangle.
     */
    public static void drawCenteredString(Graphics2D g2, String text, Rectangle rect) {
        FontMetrics fm = g2.getFontMetrics();
        int x = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int y = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, x, y);
    }
}
