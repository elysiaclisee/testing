package model;

import components.Components;
import components.Wire;

import java.util.List;
import java.util.stream.Collectors;

public class CircuitMemento {
    private final List<Components> components;
    private final List<Wire> wires;

    public CircuitMemento(Circuit circuit, List<Wire> wires) {
        // Deep copy of components and wires to preserve state
        this.components = circuit.getComponents().stream()
                .map(Components::clone)
                .collect(Collectors.toList());
        this.wires = wires.stream()
                .map(Wire::clone)
                .collect(Collectors.toList());
    }

    public List<Components> getComponents() {
        return components;
    }

    public List<Wire> getWires() {
        return wires;
    }
}