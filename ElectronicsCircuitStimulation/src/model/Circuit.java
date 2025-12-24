package model;

import components.Components;
import components.CompositeComponent;
import java.util.ArrayList;
import java.util.List;

public class Circuit {
    private final List<Components> components = new ArrayList<>();
    private CompositeComponent root;
    private int groupCounter = 1;
    private double sourceVoltage = 0.0;
    private double sourceFrequency = 0.0;

    public void setPowerSupply(double voltage, double frequency) {
        this.sourceVoltage = voltage;
        this.sourceFrequency = frequency;
    }

    public double getSourceVoltage() { 
    	return sourceVoltage; 
    }
    
    public double getSourceFrequency() { 
    	return sourceFrequency; 
    }

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

    public void ungroup(CompositeComponent group) {
        if (components.contains(group)) {
            removeComponent(group);
            for (Components child : group.getChildren()) {
                addComponent(child); 
            }
        }
    }
    
    public CompositeComponent connect(Components c1, Components c2, CompositeComponent.Mode mode) {
        if (mode == CompositeComponent.Mode.PARALLEL) {
            List<Components> parts = new ArrayList<>();
            parts.add(c1);
            parts.add(c2);
            
            int mx = (c1.getPosition().x + c2.getPosition().x) / 2;
            int my = (c1.getPosition().y + c2.getPosition().y) / 2;
            
            String uniqueId = "Group_" + (groupCounter++); 
            CompositeComponent group = new CompositeComponent(uniqueId, mx, my, mode, parts);
            
            components.remove(c1);
            components.remove(c2);
            components.add(group);
            
            rebuildCircuit(); 
            return group; 
        } 
        return null; 
    }
    
    private void rebuildCircuit() {
        if (components.isEmpty()) {
            root = null;
            return;
        }

        root = new CompositeComponent("C_root", 0, 0);
        root.setMode(CompositeComponent.Mode.SERIES);
        
        for(Components c : components) {
            root.add(c);
        }
    }
}