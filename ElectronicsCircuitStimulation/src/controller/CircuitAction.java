package controller;

import components.Components;
import components.Wire;

public class CircuitAction {
    public enum ActionType { 
        ADD_COMPONENT, 
        DELETE_COMPONENT, 
        ADD_WIRE, 
        DELETE_WIRE 
    }
    
    private ActionType type;
    private Components component;
    private Wire wire;

    // Constructor for Component actions
    public CircuitAction(ActionType type, Components component) {
        this.type = type;
        this.component = component;
    }

    // Constructor for Wire actions
    public CircuitAction(ActionType type, Wire wire) {
        this.type = type;
        this.wire = wire;
    }

    public ActionType getType() { return type; }
    public Components getComponent() { return component; }
    public Wire getWire() { return wire; }
}
