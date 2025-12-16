package controller;

import components.*;

public class Toolbox {
    public enum Tool { POWER_SOURCE, RESISTOR, CAPACITOR, BULB, INDUCTOR }

    private static int counter = 1;

    // Updated create method: It accepts values as arguments, instead of asking for them.
    public static Components create(Tool tool, int x, int y, double... params) {
        String id = tool.name().substring(0, 1) + counter++;
        
        // Helper to safely get params
        double val1 = (params.length > 0) ? params[0] : 0.0;
        double val2 = (params.length > 1) ? params[1] : 0.0;

        switch (tool) {
            case POWER_SOURCE:
                return new PowerSource(id, x, y, val1, val2);
            case RESISTOR:
                return new Resistor(id, x, y, val1);
            case CAPACITOR:
                return new Capacitor(id, x, y, val1);
            case INDUCTOR:
                return new Inductor(id, x, y, val1);
            case BULB:
                return new Bulb(id, x, y);
            default:
                return null;
        }
    }
}