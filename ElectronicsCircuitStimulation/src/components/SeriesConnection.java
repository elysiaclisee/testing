package components;

public class SeriesConnection {
    public static Complex equivalent(Components a, Components b) {
    	return Connections.series(a.getImpedance(0), b.getImpedance(0));
    }
}