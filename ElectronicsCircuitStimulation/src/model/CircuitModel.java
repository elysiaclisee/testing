package model;

import components.*;
import utils.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class CircuitModel {
    private static final int MAX_UNDO_STACK_SIZE = 20; // Limit undo history to prevent memory issues
    
    public Circuit circuit;
    public List<Wire> wires;
    public final List<CircuitAction> undoStack;
    public Components firstSelected = null;
    public Components secondSelected = null;
    public BoardTerminal termLeft;
    public BoardTerminal termRight;
	public enum ConnectionMode {SERIES_WITH_BULB, PARALLEL_WITH_BULB}
	private String simulationStatus = "Circuit: -";

    public CircuitModel() {
        this.circuit = new Circuit();
        this.wires = new ArrayList<>();
        this.undoStack = new ArrayList<>();
        termLeft = new BoardTerminal("(-)", 0, 0);
        termRight = new BoardTerminal("(+)", 0, 0);
        
        circuit.addComponent(termLeft);
        circuit.addComponent(termRight);
    }

    public Circuit getCircuit() { 
    	return circuit; 
    }
    
    public List<Wire> getWires() { 
    	return wires; 
    }
    
    public String getSimulationStatus() { 
		return simulationStatus; 
	}
    
    public void updatePhysics(ConnectionMode mode) {
        double sourceVoltage = circuit.getSourceVoltage();
        double sourceFrequency = circuit.getSourceFrequency();
        
        // Reset if invalid source
        if (sourceVoltage <= 0) {
            simulationStatus = "Configure Power Source first (click battery icon)";
            return;
        }

        CompositeComponent root = circuit.getRoot();
        if (root == null) {
            simulationStatus = "Circuit incomplete";
            return;
        }

        // --- EXACT CALCULATION LOGIC PRESERVED ---
        Complex zUser = root.getImpedance(sourceFrequency);
        Complex zBulb = new Complex(Bulb.R_BULB, 0);
        
        double totalZ, totalI, iBulb, pBulb;
        
        if (mode == ConnectionMode.SERIES_WITH_BULB) {
            // Nối tiếp: I_bulb = I_total
            Complex zTotalComplex = zUser.add(zBulb);
            totalZ = zTotalComplex.getMagnitude();
            totalI = (totalZ > 0) ? sourceVoltage / totalZ : 0.0;
            iBulb = totalI;
            pBulb = iBulb * iBulb * Bulb.R_BULB;
        } else {
            // Song song: V_bulb = V_source
            Complex zTotalComplex = Connections.parallel(zUser, zBulb);
            totalZ = zTotalComplex.getMagnitude();
            totalI = (totalZ > 0) ? sourceVoltage / totalZ : 0.0;
            iBulb = sourceVoltage / zBulb.getMagnitude();
            pBulb = iBulb * iBulb * Bulb.R_BULB;
        }
        
        // STATUS LOGIC
        double powerRatio = pBulb / Bulb.P_RATED;
        String status;
        
        if (powerRatio > 1.5) {
            status = "BURNT";
        } else if (powerRatio >= 0.8) {
            status = "BRIGHT";
        } else if (powerRatio >= 0.2) {
            status = "WEAK";
        } else {
            status = "OFF";
        }
        
        // UPDATE COMPONENT STATES
        root.setSimulationState(sourceVoltage, totalI, sourceFrequency);
        
        for (Components c : circuit.getComponents()) {
            if (c instanceof Bulb) {
                Bulb bulb = (Bulb) c;
                bulb.setLighted(powerRatio >= 0.2 && powerRatio <= 1.5);
                bulb.setSimulationState(iBulb * Bulb.R_BULB, iBulb, sourceFrequency);
                break;
            }
        }
        
        // FORMAT OUTPUT STRING
        String formattedStatus = status;
        if (status.equals("BURNT")) {
            formattedStatus = "<font color='red'><b>BURNT</b></font>";
        } else if (status.equals("BRIGHT")) {
            formattedStatus = "<font color='green'><b>BRIGHT</b></font>";
        } else if (status.equals("WEAK")) {
            formattedStatus = "<font color='orange'><b>WEAK</b></font>";
        } else if (status.equals("OFF")) {
            formattedStatus = "<font color='gray'><b>OFF</b></font>";
        }
        
        this.simulationStatus = String.format("<html>V: %.2fV | Z: %.2fΩ | I: %.2fA | Bulb: %.2fW/%.0fW [%s]</html>",
                sourceVoltage, totalZ, totalI, pBulb, Bulb.P_RATED, formattedStatus);
    }
    
    public void addComponent(Components c) {
        circuit.addComponent(c);
        addActionToUndoStack(new CircuitAction(CircuitAction.ActionType.ADD_COMPONENT, c));
    }

    public void removeComponent(Components c) {
        circuit.removeComponent(c);
        addActionToUndoStack(new CircuitAction(CircuitAction.ActionType.DELETE_COMPONENT, c));
    }

    public void addWire(Components c1, Components c2, Wire.Type type) {
        Wire wire = new Wire(c1, c2, type);
        wires.add(wire);
        addActionToUndoStack(new CircuitAction(CircuitAction.ActionType.ADD_CONNECTION, wire));
        CompositeComponent.Mode mode = (type == Wire.Type.SERIES) ? CompositeComponent.Mode.SERIES : CompositeComponent.Mode.PARALLEL;
        circuit.connect(c1, c2, mode);
    }
    
    public void addActionToUndoStack(CircuitAction action) {
        undoStack.add(action);
        if (undoStack.size() > MAX_UNDO_STACK_SIZE) {
            undoStack.remove(0); // Remove the oldest action
        }
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
    
    public Components componentAt(Point p) {
        List<Components> components = circuit.getComponents();
        for (int i = components.size() - 1; i >= 0; i--) {
            Components c = components.get(i);
            if (c.contains(p)) return c;
        }
        return null;
    }
}