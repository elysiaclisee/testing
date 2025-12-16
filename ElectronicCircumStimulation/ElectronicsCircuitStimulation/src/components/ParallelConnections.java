package components;

/**
 * Convenience wrapper for parallel connection calculations between two components.
 */
public class ParallelConnections {
    public static double equivalent(Components a, Components b) {
        return Connections.parallel(a.getResistance(), b.getResistance());
    }
}