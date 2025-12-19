package model;

import components.Components;
import utils.Wire;

public class CircuitAction {
    public enum ActionType {
        ADD_COMPONENT,
        DELETE_COMPONENT,
        ADD_CONNECTION,
        DELETE_CONNECTION
    }

    private final ActionType type;
    private final Components component;
    private final Wire connection;

    public CircuitAction(ActionType type, Components component) {
        this.type = type;
        this.component = component;
        this.connection = null;
    }

    public CircuitAction(ActionType type, Wire connection) {
        this.type = type;
        this.component = null;
        this.connection = connection;
    }

    public ActionType getType() {
        return type;
    }

    public Components getComponent() {
        return component;
    }

    public Wire getConnection() {
        return connection;
    }
}
