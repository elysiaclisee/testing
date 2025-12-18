package model;

import components.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class CircuitModel {
    public Circuit circuit;
    public List<Wire> wires;
    public final List<CircuitAction> undoStack;
    public Components dragging = null;
    public Point dragOffset = null;
    public Components firstSelected = null;
    public Components secondSelected = null;
    public BoardTerminal termLeft;
    public BoardTerminal termRight;

    public CircuitModel() {
        this.circuit = new Circuit();
        this.wires = new ArrayList<>();
        this.undoStack = new ArrayList<>();
        termLeft = new BoardTerminal("(-)", 0, 0);
        termRight = new BoardTerminal("(+)", 0, 0);
        
        circuit.addComponent(termLeft);
        circuit.addComponent(termRight);
    }

    public Circuit getCircuit() { return circuit; }
    public List<Wire> getWires() { return wires; }

    public void addComponent(Components c) {
        circuit.addComponent(c);
        undoStack.add(new CircuitAction(CircuitAction.ActionType.ADD_COMPONENT, c));
    }

    public void removeComponent(Components c) {
        circuit.removeComponent(c);
        undoStack.add(new CircuitAction(CircuitAction.ActionType.DELETE_COMPONENT, c));
    }

    public void addWire(Components c1, Components c2, Wire.Type type) {
        Wire wire = new Wire(c1, c2, type);
        wires.add(wire);
        undoStack.add(new CircuitAction(CircuitAction.ActionType.ADD_CONNECTION, wire));
        CompositeComponent.Mode mode = (type == Wire.Type.SERIES) ? CompositeComponent.Mode.SERIES : CompositeComponent.Mode.PARALLEL;
        circuit.connect(c1, c2, mode);
    }

    public void removeWire(Wire wire) {
        wires.remove(wire);
        undoStack.add(new CircuitAction(CircuitAction.ActionType.DELETE_CONNECTION, wire));
    }

    // --- Command Pattern Undo Logic ---
    // Instead of replacing the entire board, we reverse the last action
    public void undo() {
        if (undoStack.isEmpty()) return;
        
        CircuitAction action = undoStack.remove(undoStack.size() - 1);
        
        switch (action.getType()) {
            case ADD_COMPONENT:
                // Special case: If undoing a parallel composite group, restore the children
                Components comp = action.getComponent();
                if (comp instanceof CompositeComponent) {
                    CompositeComponent composite = (CompositeComponent) comp;
                    if (composite.getMode() == CompositeComponent.Mode.PARALLEL) {
                        // Remove the composite group
                        circuit.removeComponent(composite);
                        // Restore the original components
                        for (Components child : composite.getChildren()) {
                            circuit.addComponent(child);
                        }
                        break;
                    }
                }
                // Normal case: Just remove the component
                circuit.removeComponent(comp);
                break;
            case DELETE_COMPONENT:
                // Reverse: Add back the component that was deleted
                circuit.addComponent(action.getComponent());
                break;
            case ADD_CONNECTION:
                // Reverse: Remove the wire that was added
                wires.remove(action.getConnection());
                break;
            case DELETE_CONNECTION:
                // Reverse: Add back the wire that was deleted
                wires.add(action.getConnection());
                break;
        }
    }
}