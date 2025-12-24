package utils; 

import components.*;
import model.CircuitModel;

import java.awt.Point;

public class CircuitValidator {
    private final CircuitModel model;

    public CircuitValidator(CircuitModel model) {
        this.model = model;
    }
    
	//main method
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

        //connect left or right
        Point targetPoint = parallelBox.getConnectorPoint(otherComponent);
        
        //if box hasn't been drawn yet, points might be null
        if (targetPoint == null) return 0; 

        int count = 0;
        for (utils.Wire w : model.getWires()) { 
            Components connected = null;
            
            if (w.getA() == parallelBox) connected = w.getB();
            else if (w.getB() == parallelBox) connected = w.getA();
            
            if (connected != null) {
                Point existingPoint = parallelBox.getConnectorPoint(connected);
                if (targetPoint.equals(existingPoint)) {
                    count++;
                }
            }
        }
        return count;
    }
}
