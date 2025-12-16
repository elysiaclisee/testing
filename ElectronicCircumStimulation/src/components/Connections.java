package components;

/**
 * Utility methods for computing equivalent resistance and current for simple two-component
 * series (continuous) and parallel connections.
 */
public class Connections {
    // series (continuous) equivalent resistance for two resistances
    public static double series(double r1, double r2) {
        if (Double.isInfinite(r1) || Double.isInfinite(r2)) {
            return Double.POSITIVE_INFINITY;
        }
        return r1 + r2;
    }

    // parallel equivalent resistance for two resistances
    public static double parallel(double r1, double r2) {
        // handle infinities: 1/(1/r1 + 1/r2)
        if (Double.isInfinite(r1) && Double.isInfinite(r2)) return Double.POSITIVE_INFINITY;
        if (Double.isInfinite(r1)) return r2; // open circuit in one branch -> other branch only
        if (Double.isInfinite(r2)) return r1;
        if (r1 <= 0 || r2 <= 0) return Double.POSITIVE_INFINITY;
        return 1.0 / (1.0 / r1 + 1.0 / r2);
    }

    // compute current for a given voltage and equivalent resistance
    public static double current(double voltage, double req) {
        if (req <= 0 || Double.isInfinite(req)) return 0.0;
        return voltage / req;
    }
}