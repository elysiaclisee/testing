package model;

import components.Bulb;
import components.Capacitor;
import components.Components;
import components.CompositeComponent;

import java.util.ArrayList;
import java.util.List;

public class Circuit {
    private final List<Components> components = new ArrayList<>();
    private CompositeComponent root;

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
        // For simplicity, we'll just rebuild the root composite component
        // with the new connection. A more sophisticated implementation would
        // merge the new connection into the existing composite structure.
        if (root == null) {
            root = new CompositeComponent("C1", 0, 0);
            root.add(c1);
            root.add(c2);
            root.setMode(mode);
        } else {
            CompositeComponent newRoot = new CompositeComponent("C_new", 0, 0);
            newRoot.add(root);
            // Find the component to connect to that is not already in the root
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

        // If the root already exists and is complex, this simple rebuild might be incorrect.
        // However, for the current user flow (add 2 components, then connect), this works.
        // A more robust solution would involve graph-based circuit analysis.
        if (root == null) {
            root = new CompositeComponent("C_root", 0, 0);
            for(Components c : components) {
                root.add(c);
            }
            // Default to series, the user will define the connection type next.
            root.setMode(CompositeComponent.Mode.SERIES);
        }
    }

    public void updateBulbStates(double voltage) {
        boolean hasCapacitor = components.stream().anyMatch(c -> c instanceof Capacitor);
        double totalResistance = (root != null) ? root.getResistanceOhms() : Double.POSITIVE_INFINITY;
        boolean circuitClosed = (root != null) && !Double.isInfinite(totalResistance);

        for (Components component : components) {
            if (component instanceof Bulb) {
                Bulb bulb = (Bulb) component;
                boolean shouldBeLit = circuitClosed && hasCapacitor;

                if (shouldBeLit) {
                    if (totalResistance > 0) {
                        double current = voltage / totalResistance;
                        double power = current * current * bulb.getResistanceOhms();
                        if (power > bulb.getPowerLimit()) {
                            shouldBeLit = false; // Surpassed power limit
                        }
                    } else {
                        // If total resistance is 0, we might have a short circuit,
                        // but for this simulation, we'll assume it lights up
                        // without calculating power, to avoid division by zero.
                        shouldBeLit = true;
                    }
                }
                bulb.setLighted(shouldBeLit);
            }
        }
    }
}