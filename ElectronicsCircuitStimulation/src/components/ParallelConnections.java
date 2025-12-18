package components;

public class ParallelConnections {
    public static Complex equivalent(Components a, Components b) {
    	return Connections.parallel(a.getImpedance(0), b.getImpedance(0));
    }
}