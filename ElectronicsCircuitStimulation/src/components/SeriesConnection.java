package components;

/**
 * Convenience wrapper for series connection calculations between two components.
 */
public class SeriesConnection {
    public static double equivalent(Components a, Components b) {
        return Connections.series(a.getResistance(), b.getResistance());
    }
}