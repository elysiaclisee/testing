package controller;

import components.*;
import javax.swing.*;

public class Toolbox {
    public enum Tool { BATTERY, RESISTOR, CAPACITOR, BULB, INDUCTOR }

    private static int counter = 1;

    public static Components create(Tool tool, int x, int y) {
        String id = tool.name().substring(0, 1) + counter++;

        switch (tool) {
            case BATTERY: {
                JTextField voltageField = new JTextField("5.0", 5);
                JTextField frequencyField = new JTextField("0", 5);

                JPanel panel = new JPanel();
                panel.add(new JLabel("Voltage (V):"));
                panel.add(voltageField);
                panel.add(Box.createHorizontalStrut(15)); // a spacer
                panel.add(new JLabel("Frequency (Hz):"));
                panel.add(frequencyField);

                int result = JOptionPane.showConfirmDialog(null, panel,
                        "Enter Battery Values", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    double voltage = 5.0;
                    double frequency = 0.0;
                    try {
                        voltage = Double.parseDouble(voltageField.getText());
                    } catch (NumberFormatException e) {
                        // keep default
                    }
                    try {
                        frequency = Double.parseDouble(frequencyField.getText());
                    } catch (NumberFormatException e) {
                        // keep default
                    }
                    return new Battery(id, x, y, voltage, frequency);
                } else {
                    return null; // User cancelled
                }
            }
            case BULB:
                return new Bulb(id, x, y); // Uses default constructor
            case RESISTOR:
            case CAPACITOR:
            case INDUCTOR: {
                String unit = "";
                String title = "";
                switch (tool) {
                    case RESISTOR:
                        unit = "Ohms";
                        title = "Enter Resistance";
                        break;
                    case CAPACITOR:
                        unit = "F";
                        title = "Enter Capacitance";
                        break;
                    case INDUCTOR:
                        unit = "H";
                        title = "Enter Inductance";
                        break;
                }

                String prompt = "Enter value for " + tool.name().toLowerCase() + " (" + unit + "):";
                String input = JOptionPane.showInputDialog(null, prompt, title, JOptionPane.PLAIN_MESSAGE);
                double val = 0.0;
                if (input != null && !input.trim().isEmpty()) {
                    try {
                        val = Double.parseDouble(input.trim());
                    } catch (NumberFormatException ex) {
                        val = 0.0;
                    }
                }

                switch (tool) {
                    case RESISTOR:
                        return new Resistor(id, x, y, val);
                    case CAPACITOR:
                        return new Capacitor(id, x, y, val);
                    case INDUCTOR:
                        return new Inductor(id, x, y, val);
                }
            }
            default:
                return null;
        }
    }
}