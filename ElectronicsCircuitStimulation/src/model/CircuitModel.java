package model;

import components.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class CircuitModel {
    public Circuit circuit;
    public List<Wire> wires;
    public final List<CircuitMemento> undoStack;
    public final List<CircuitMemento> redoStack;
    public Components dragging = null;
    public Point dragOffset = null;
    public Components firstSelected = null;
    public Components secondSelected = null;

    public CircuitModel() {
        this.circuit = new Circuit();
        this.wires = new ArrayList<>();
        this.undoStack = new ArrayList<>();
        this.redoStack = new ArrayList<>();
    }

    public Circuit getCircuit() { return circuit; }
    public List<Wire> getWires() { return wires; }

    public void addComponent(Components c) {
        circuit.addComponent(c);
    }

    public void removeComponent(Components c) {
        circuit.removeComponent(c);
    }

    public void addWire(Components c1, Components c2, Wire.Type type) {
        wires.add(new Wire(c1, c2, type));
        CompositeComponent.Mode mode = (type == Wire.Type.SERIES) ? CompositeComponent.Mode.SERIES : CompositeComponent.Mode.PARALLEL;
        circuit.connect(c1, c2, mode);
    }

    // --- Memento / Undo Logic ---
    public void saveState() {
        redoStack.clear();
        undoStack.add(new CircuitMemento(circuit, wires));
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        CircuitMemento memento = undoStack.remove(undoStack.size() - 1);
        redoStack.add(new CircuitMemento(circuit, wires));
        
        // Restore
        this.circuit.setComponents(memento.getComponents());
        this.wires.clear();
        this.wires.addAll(memento.getWires());
    }
}