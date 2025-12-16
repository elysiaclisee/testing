package components;

public class SeriesConnection {
    public static double equivalent(Components a, Components b) {
        return Connections.series(a.getResistance(), b.getResistance());
    }
}