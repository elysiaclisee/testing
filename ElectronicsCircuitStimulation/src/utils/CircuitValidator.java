package utils; 

import components.*;
import model.CircuitModel;

import java.awt.Rectangle;
import java.util.List;

public class CircuitValidator {
    private final CircuitModel model;

    public CircuitValidator(CircuitModel model) {
        this.model = model;
    }

    // The ONE main method the Controller will call
    public void validateConnectionRequest(Components c1, Components c2, CompositeComponent.Mode mode) throws CircuitValidationException {
        if (c1 == null || c2 == null) {
            throw new CircuitValidationException("Select two components first.");
        }

        validateTerminals(c1, c2, mode);
        validateCapacity(c1);
        validateCapacity(c2);
        validateParallelLogic(c1, c2, mode);
        validateParallelBoxSides(c1, c2);
        validateParallelBoxSides(c2, c1);
        validateDuplicateConnection(c1, c2);
    }

    // --- Internal Logic (Moved from Controller) ---

    private void validateTerminals(Components c1, Components c2, CompositeComponent.Mode mode) throws CircuitValidationException {
        boolean isTerm1 = (c1 instanceof BoardTerminal);
        boolean isTerm2 = (c2 instanceof BoardTerminal);
        
        if ((isTerm1 || isTerm2) && mode == CompositeComponent.Mode.PARALLEL) {
            throw new CircuitValidationException("Board terminals only support SERIES connections.");
        }
        if (isTerm1 && getConnectionCount(c1) >= 1) throw new CircuitValidationException("Terminal " + c1.getId() + " occupied.");
        if (isTerm2 && getConnectionCount(c2) >= 1) throw new CircuitValidationException("Terminal " + c2.getId() + " occupied.");
    }

    private void validateCapacity(Components c) throws CircuitValidationException {
        int max = (c instanceof BoardTerminal) ? 1 : 2;
        if (getConnectionCount(c) >= max) {
            throw new CircuitValidationException("Component " + c.getId() + " is full (max " + max + ").");
        }
    }

    private void validateParallelLogic(Components c1, Components c2, CompositeComponent.Mode mode) throws CircuitValidationException {
        if (mode == CompositeComponent.Mode.PARALLEL) {
            if (getConnectionCount(c1) > 0 || getConnectionCount(c2) > 0) {
                throw new CircuitValidationException("Cannot group: Components are already connected in series.");
            }
        }
    }

    private void validateParallelBoxSides(Components box, Components other) throws CircuitValidationException {
        if (box instanceof CompositeComponent) {
            CompositeComponent comp = (CompositeComponent) box;
            if (comp.getMode() == CompositeComponent.Mode.PARALLEL) {
                if (getConnectionCountOnSide(comp, other) >= 1) {
                    throw new CircuitValidationException("Parallel box side is full.");
                }
            }
        }
    }

    private void validateDuplicateConnection(Components c1, Components c2) throws CircuitValidationException {
        String id1 = c1.getId();
        String id2 = c2.getId();
        for (Wire w : model.getWires()) {
            String wa = w.getA().getId();
            String wb = w.getB().getId();
            if ((wa.equals(id1) && wb.equals(id2)) || (wa.equals(id2) && wb.equals(id1))) {
                throw new CircuitValidationException("Already connected.");
            }
        }
    }

    // Helper methods moved here because they are only used for validation
    public int getConnectionCount(Components c) {
        int count = 0;
        for (Wire w : model.getWires()) {
            if (w.getA().getId().equals(c.getId()) || w.getB().getId().equals(c.getId())) count++;
        }
        return count;
    }

	private int getConnectionCountOnSide(CompositeComponent parallelBox, Components otherComponent) {
	    if (parallelBox.getMode() != CompositeComponent.Mode.PARALLEL) {
	        return 0;
	    }
	
	    // 1. Calculate the exact positions of the Left and Right connection dots
	    // We replicate the logic from CompositeComponent.draw() to be 100% sure
	    int minX = Integer.MAX_VALUE;
	    int maxX = Integer.MIN_VALUE;
	    
	    List<Components> children = parallelBox.getChildren();
	    if (children.isEmpty()) {
	        minX = parallelBox.getPosition().x - 20;
	        maxX = parallelBox.getPosition().x + 20;
	    } else {
	        for (Components c : children) {
	            Rectangle b = c.getBounds();
	            if (b.x < minX) minX = b.x;
	            if (b.x + b.width > maxX) maxX = b.x + b.width;
	        }
	    }
	    
	    int padding = 30;
	    int railLeftX = minX - padding;
	    int railRightX = maxX + padding;
	    
	    // 2. Determine which side the NEW component is targeting
	    // We compare distances: Is it closer to the Left Rail or Right Rail?
	    int targetX = otherComponent.getPosition().x;
	    boolean targettingLeft = Math.abs(targetX - railLeftX) < Math.abs(targetX - railRightX);
	
	    int count = 0;
	    
	    // 3. Check existing connections
	    for (Wire w : model.wires) {
	        Components connected = null;
	        
	        // Use ID comparison to be safe against Undo/Redo cloning issues
	        if (w.getA().getId().equals(parallelBox.getId())) {
	            connected = w.getB();
	        } else if (w.getB().getId().equals(parallelBox.getId())) {
	            connected = w.getA();
	        }
	        
	        if (connected != null) {
	            // Check which side this EXISTING wire is connected to
	            int existingX = connected.getPosition().x;
	            boolean existingIsOnLeft = Math.abs(existingX - railLeftX) < Math.abs(existingX - railRightX);
	            
	            // If they are targeting the same side, increment count
	            if (existingIsOnLeft == targettingLeft) {
	                count++;
	            }
	        }
	    }
	    return count;
	}
}
