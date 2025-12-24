package model;

import components.*;
import utils.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class CircuitModel {
    private static final int MAXUNDOSTACK = 20; // Limit undo history to prevent memory issues
    
    public Circuit circuit;
    public List<Wire> wires;
    public final List<CircuitAction> undoStack;
    public Components firstSelected = null;
    public Components secondSelected = null;
    public BoardTerminal termLeft;
    public BoardTerminal termRight;
	public enum ConnectionMode {BULBSERIES, BULBPARALLEL}
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
    
    public Components componentAt(Point p) {
	    List<Components> components = circuit.getComponents();
	    for (int i = components.size() - 1; i >= 0; i--) {
	        Components c = components.get(i);
	        if (c.contains(p)) return c;
	    }
	    return null;
	}

	public void updatePhysics(ConnectionMode mode) {
        double sourceVoltage = circuit.getSourceVoltage();
        double sourceFrequency = circuit.getSourceFrequency();
        
        // Reset if invalid source
        if (sourceVoltage <= 0) {
            simulationStatus = "Configure power source first (click icon on toolbox)";
            return;
        }

        CompositeComponent root = circuit.getRoot();
        if (root == null) {
            simulationStatus = "Circuit incomplete";
            return;
        }

        Complex zUser = root.getImpedance(sourceFrequency);
        Complex zBulb = new Complex(Bulb.R_BULB, 0);
        
        double totalZ, totalI, iBulb, pBulb;
        
        if (mode == ConnectionMode.BULBSERIES) {
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
        
        root.setSimulationState(sourceVoltage, totalI, sourceFrequency);
        
        for (Components c : circuit.getComponents()) {
            if (c instanceof Bulb) {
                Bulb bulb = (Bulb) c;
                bulb.setSimulationState(iBulb * Bulb.R_BULB, iBulb, sourceFrequency);
                break;
            }
        }
        
        this.simulationStatus = FormatUtils.formatBulbStatus(sourceVoltage, totalZ, totalI, pBulb, status);
    }
    
    //instead of replacing the entire board, we reverse the last action
	public void undo() {
	    if (undoStack.isEmpty()) return;
	    
	    CircuitAction action = undoStack.remove(undoStack.size() - 1);
	    
	    switch (action.getType()) {
	        case ADD_COMPONENT:
	            Components comp = action.getComponent();
	            
	            if (comp instanceof CompositeComponent) {
	                CompositeComponent cc = (CompositeComponent) comp;
	                if (cc.getMode() == CompositeComponent.Mode.PARALLEL) {
	                    circuit.ungroup(cc); 
	                    break;
	                }
	            }
	            circuit.removeComponent(comp);
	            break;
	
	        case DELETE_COMPONENT:
	            circuit.addComponent(action.getComponent());
	            break;
	
	        case ADD_CONNECTION:
	            wires.remove(action.getConnection());
	            break;
	
	        case DELETE_CONNECTION:
	            wires.add(action.getConnection());
	            break;
	    }
	}

	public void addActionToUndoStack(CircuitAction action) {
	    undoStack.add(action);
	    if (undoStack.size() > MAXUNDOSTACK) {
	        undoStack.remove(0); // Remove the oldest action
	    }
	}

    public void addWire(Components c1, Components c2, Wire.Type type) {
        Wire wire = new Wire(c1, c2, type);
        wires.add(wire);
        addActionToUndoStack(new CircuitAction(CircuitAction.ActionType.ADD_CONNECTION, wire));
        CompositeComponent.Mode mode = (type == Wire.Type.SERIES) ? CompositeComponent.Mode.SERIES : CompositeComponent.Mode.PARALLEL;
        circuit.connect(c1, c2, mode);
    }
    
    public void connectComponent(Components c1, Components c2, CompositeComponent.Mode mode) {
        if (mode == CompositeComponent.Mode.SERIES) {
            addWire(c1, c2, Wire.Type.SERIES);
        } else {
            CompositeComponent newGroup = circuit.connect(c1, c2, mode);
            if (newGroup != null) {
                addActionToUndoStack(new CircuitAction(CircuitAction.ActionType.ADD_COMPONENT, newGroup));
            }
        }
    }
}