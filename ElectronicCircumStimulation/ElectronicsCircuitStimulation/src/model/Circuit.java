package model;

import components.Components;
import components.CompositeComponent;
import java.util.ArrayList;
import java.util.List;

public class Circuit {
    private final List<Components> components = new ArrayList<>();
    private CompositeComponent root;
    
    // --- NEW: Global Power Settings ---
    private double sourceVoltage = 0.0;
    private double sourceFrequency = 60.0; // Default to 60Hz

    public void setPowerSupply(double voltage, double frequency) {
        this.sourceVoltage = voltage;
        this.sourceFrequency = frequency;
    }

    public double getSourceVoltage() { return sourceVoltage; }
    public double getSourceFrequency() { return sourceFrequency; }
    // ----------------------------------

    public void addComponent(Components component) {
        components.add(component);
        rebuildCircuit();
    }

    public void removeComponent(Components component) {
        components.remove(component);
        rebuildCircuit();
    }

    public void setComponents(List<Components> components) {
        this.components.clear();
        this.components.addAll(components);
        rebuildCircuit();
    }

    public List<Components> getComponents() {
        return components;
    }

    public CompositeComponent getRoot() {
        return root;
    }

    public void connect(Components c1, Components c2, CompositeComponent.Mode mode) {
        if (root == null) {
            root = new CompositeComponent("C1", 0, 0);
            root.add(c1);
            root.add(c2);
            root.setMode(mode);
        } else {
            CompositeComponent newRoot = new CompositeComponent("C_new", 0, 0);
            newRoot.add(root);
            if (!root.getChildren().contains(c1)) {
                newRoot.add(c1);
            } else if (!root.getChildren().contains(c2)) {
                newRoot.add(c2);
            }
            newRoot.setMode(mode);
            root = newRoot;
        }
    }

    private void rebuildCircuit() {
        if (components.size() < 2) {
            root = null;
            return;
        }
        if (root == null) {
            root = new CompositeComponent("C_root", 0, 0);
            for(Components c : components) {
                root.add(c);
            }
            root.setMode(CompositeComponent.Mode.SERIES);
        }
    }
}