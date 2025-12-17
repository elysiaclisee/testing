package model;

import components.Bulb;
import components.Capacitor;
import components.Components;
import components.CompositeComponent;

import java.awt.Point;
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

    public void connect(Components a, Components b, CompositeComponent.Mode mode) {
        if (a == null || b == null || a == b) return;

        // a và b phải là TOP-LEVEL rồi (nhờ selectableAt)
        CompositeComponent group = new CompositeComponent("G_" + System.nanoTime(), 0, 0);
        group.setMode(mode);
        group.add(a);
        group.add(b);

        // remove top-level a,b
        components.remove(a);
        components.remove(b);

        // add new group as top-level
        components.add(group);

        // root = group nếu chỉ muốn root là cụm lớn nhất
        root = group;
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

    public Components selectableAt(Point p) {
        // duyệt từ trên xuống dưới để ưu tiên component vẽ sau
        for (int i = components.size() - 1; i >= 0; i--) {
            Components top = components.get(i);
            if (hitDeep(top, p)) {
                return top; // luôn trả về TOP-LEVEL
            }
        }
        return null;
    }

    private boolean hitDeep(Components c, Point p) {
        if (c.contains(p)) return true;
        if (c instanceof CompositeComponent comp) {
            for (Components child : comp.getChildren()) {
                if (hitDeep(child, p)) return true;
            }
        }
        return false;
    }

}