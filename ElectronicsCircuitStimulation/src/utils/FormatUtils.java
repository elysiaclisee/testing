package utils;

import java.awt.*;
import components.*;

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
    
    public static String formatComponentInfo(Components c, double freq) {
        if (c == null) return "None";
        
        String staticInfo = "";
        
        // 1. Get the static properties (Resistance, Capacitance, etc.)
        if (c instanceof Resistor) {
            staticInfo = String.format("R: %s", formatMetric(c.getResistance(), "Ω"));
        } else if (c instanceof Capacitor) {
            double z = c.getImpedance(freq).getMagnitude();
            staticInfo = String.format("C: %s (Z: %s)", 
                formatMetric(((Capacitor) c).getCapacitance(), "F"), 
                formatMetric(z, "Ω"));
        } else if (c instanceof Inductor) {
            double z = c.getImpedance(freq).getMagnitude();
            staticInfo = String.format("L: %s (Z: %s)", 
                formatMetric(((Inductor) c).getInductance(), "H"), 
                formatMetric(z, "Ω"));
        } else {
            staticInfo = c.getId();
        }

        // 2. Append the LIVE simulation data (Voltage & Current)
        // We only show this if there is actually current flowing or voltage present
        if (c.getVoltageDrop() > 0.001 || c.getCurrentFlow() > 0.001) {
            String liveInfo = String.format(" | V: %s | I: %s", 
                formatMetric(c.getVoltageDrop(), "V"),
                formatMetric(c.getCurrentFlow(), "A"));
            return staticInfo + liveInfo;
        }
        
        return staticInfo;
    }
}
