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

    public List<Components> getComponents() {
        return components;
    }

    public CompositeComponent getRoot() {
        return root;
    }

 // Inside model/Circuit.java

    public void connect(Components c1, Components c2, CompositeComponent.Mode mode) {
        // --- CASE 1: PARALLEL (Create a Group Box) ---
        if (mode == CompositeComponent.Mode.PARALLEL) {
            // 1. Create a list of the parts we are grouping
            List<Components> parts = new ArrayList<>();
            parts.add(c1);
            parts.add(c2);
            
            // 2. Calculate the center position for the new Group
            int mx = (c1.getPosition().x + c2.getPosition().x) / 2;
            int my = (c1.getPosition().y + c2.getPosition().y) / 2;
            
            // 3. Create the Composite Component (The Parallel Group)
            CompositeComponent group = new CompositeComponent("Group_" + System.currentTimeMillis(), mx, my, mode, parts);
            
            // 4. CRITICAL: Remove the original separate parts from the board list
            components.remove(c1);
            components.remove(c2);
            
            // 5. Add the new Group to the board so the View draws it
            components.add(group);
            
            // 6. Rebuild the physics tree to include this new group
            rebuildCircuit(); 
            
        } else {
            // --- CASE 2: SERIES (Logical Connection Only) ---
            // For series, we often keep components separate visually (connected by a Wire),
            // so we just update the logical root tree for calculations.
            
            if (root == null) {
                root = new CompositeComponent("RootSeries", 0, 0);
                root.add(c1);
                root.add(c2);
                root.setMode(mode);
            } else {
                // If adding to an existing circuit, wrap the old root
                CompositeComponent newRoot = new CompositeComponent("NewRoot", 0, 0);
                newRoot.add(root);
                
                // Add the new component that isn't already in the tree
                // (This is a simplified tree builder; complex circuits might need a more robust graph approach)
                boolean c1InTree = root.getChildren().contains(c1) || root == c1; // simplified check
                boolean c2InTree = root.getChildren().contains(c2) || root == c2;
                
                if (!c1InTree) newRoot.add(c1);
                else if (!c2InTree) newRoot.add(c2);
                
                newRoot.setMode(mode);
                root = newRoot;
            }
        }
    }

    public void rebuildCircuit() {
        this.root = null; // <-- THÊM DÒNG NÀY: Xóa sạch cây logic cũ để không để lại dấu vết
        
        if (components.size() < 2) return;

        // Luôn tạo mới root dựa trên danh sách linh kiện hiện có
        root = new CompositeComponent("C_root", 0, 0);
        for(Components c : components) {
            root.add(c);
        }
        root.setMode(CompositeComponent.Mode.SERIES);
    }
}