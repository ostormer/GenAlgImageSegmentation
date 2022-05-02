package src;

import java.util.Comparator;

public enum Objective {
    EDGE_VALUE,
    CONNECTIVITY,
    DEVIATION;

    public static Comparator<Individual> getObjectiveComparator(Objective segmentationCriteria) {
        return switch (segmentationCriteria) {
            case EDGE_VALUE -> (a, b) -> Double.compare(a.getEdgeValue(), b.getEdgeValue());
            case CONNECTIVITY -> (a, b) -> Double.compare(a.getConnectivity(), b.getConnectivity());
            case DEVIATION -> (a, b) -> Double.compare(a.getDeviation(), b.getDeviation());
        };
    }

    public static double measure(Objective segmentationCriteria, Segment segment) {
        return switch (segmentationCriteria) {
            case EDGE_VALUE -> segment.computeEdgeValue();
            case CONNECTIVITY -> segment.computeConnectivity();
            case DEVIATION -> segment.computeDeviation();
        };
    }
}
