package controller;

import components.*;
import javax.swing.*;

public class Toolbox {
    public enum Tool { RESISTOR, CAPACITOR, BULB }

    private static int counter = 1;

    public static Components create(Tool tool, int x, int y) {
        String id = tool.name().substring(0,1) + counter++;
        // prompt user for a numeric value for the component (default 0)
        String prompt = "Enter value for " + tool.name().toLowerCase() + " (numeric, default 0):";
        String input = JOptionPane.showInputDialog(null, prompt, "0");
        double val = 0.0;
        if (input != null && !input.trim().isEmpty()) {
            try {
                val = Double.parseDouble(input.trim());
            } catch (NumberFormatException ex) {
                // if parsing fails, keep default 0
                val = 0.0;
            }
        }

        switch (tool) {
            case RESISTOR:
                return new Resistor(id, x, y, val);
            case CAPACITOR:
                return new Capacitor(id, x, y, val);
            case BULB:
                return new Bulb(id, x, y, val);
            default:
                return null;
        }
    }
}