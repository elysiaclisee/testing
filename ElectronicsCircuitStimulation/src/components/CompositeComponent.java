package components;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CompositeComponent extends Components {
	private List<Components> children = new ArrayList<>();
    private Mode mode = Mode.SERIES;

    public enum Mode { SERIES, PARALLEL }

    public CompositeComponent(String id, int x, int y) { super(id, x, y); }

    // ... (Keep existing constructors and generic methods) ...

    @Override
    public double getResistanceOhms() {
        return getImpedance(0); // Default to DC
    }

    @Override
    public double getImpedance(double frequency) {
        if (children == null || children.isEmpty()) return Double.POSITIVE_INFINITY;
        
        if (mode == Mode.SERIES) {
            double totalZ = 0.0;
            for (Components c : children) {
                totalZ += c.getImpedance(frequency);
            }
            return totalZ;
        } else { // PARALLEL
            double inverseZ = 0.0;
            boolean hasShort = false;
            for (Components c : children) {
                double z = c.getImpedance(frequency);
                if (z <= 1e-9) hasShort = true; // Handle effectively 0 impedance
                if (!Double.isInfinite(z) && z > 0) {
                    inverseZ += 1.0 / z;
                }
            }
            if (hasShort) return 0.0;
            if (inverseZ == 0.0) return Double.POSITIVE_INFINITY;
            return 1.0 / inverseZ;
        }
    }

    /**
     * Recursively distributes Voltage and Current to children based on circuit laws.
     */
    @Override
    public void setSimulationState(double voltage, double current) {
        // 1. Store state for this composite container
        super.setSimulationState(voltage, current);

        // 2. Distribute to children
        if (children.isEmpty()) return;

        // Get the frequency from context or passed down? 
        // For simplicity in this architecture, we recalculate impedances or 
        // assume impedance was cached. Here we re-fetch assuming freq is available 
        // or we calculate ratios based on R (DC) if freq is missing. 
        // Ideally, solve() should accept frequency.
        // Let's assume we use the cached values implied by the passed voltage/current.
        
        if (mode == Mode.SERIES) {
            // SERIES: Current is constant, Voltage splits.
            // V_child = V_total * (Z_child / Z_total)
            for (Components c : children) {
                // Approximate distribution based on DC Resistance for now, 
                // or you need to pass frequency into setSimulationState.
                // Assuming DC/Resistive logic for distribution to keep code simple:
                double childR = c.getResistanceOhms(); 
                double totalR = this.getResistanceOhms();
                
                double childV = (Double.isInfinite(totalR)) ? voltage : voltage * (childR / totalR);
                
                // If this is an open circuit (Infinite R), the open component takes ALL voltage
                if (Double.isInfinite(childR) && Double.isInfinite(totalR)) {
                     // Heuristic: Split voltage equally among open components or give to first
                     childV = voltage; 
                }

                c.setSimulationState(childV, current);
            }
        } else {
            // PARALLEL: Voltage is constant, Current splits.
            // I_child = I_total * (Z_total / Z_child) OR I_child = V / Z_child
            for (Components c : children) {
                double childR = c.getResistanceOhms();
                double childI = (childR <= 0) ? 0 : voltage / childR;
                
                if (Double.isInfinite(childR)) childI = 0.0;
                
                c.setSimulationState(voltage, childI);
            }
        }
    }

	@Override
	public Rectangle getBounds() {
		// TODO Auto-generated method stub
		return null;
	}
}