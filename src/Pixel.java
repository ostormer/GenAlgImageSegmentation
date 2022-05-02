package src;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Pixel {
    public final CieLab color;
    public final int x, y; // Coordinates of pixel
    // Map store neighboring pixels. Filled after all pixels are created
    private Map<Integer, Pixel> neighbors = new HashMap<>();

    public Pixel(CieLab color, int x, int y) {
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public Map<Integer, Pixel> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(Map<Integer, Pixel> neighbors) {
        this.neighbors = neighbors;
    }

    public Map<Integer, Pixel> getCardinalNeighbors() {
        return neighbors.entrySet()
                .stream()
                .filter(entry -> entry.getKey() < 5 && entry.getValue() != null) // Not diagonal neighbors
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); // Make it a map
    }

    public List<Gene> getValidGenes() {
        return getCardinalNeighbors().entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null)
                .map(Map.Entry::getKey)
                .map(Gene::fromNeighborNumber)
                .collect(Collectors.toList());
    }

    public Pixel getNeighborByGene(Gene gene) {
        return switch (gene) {
            case RIGHT -> neighbors.get(1);
            case LEFT -> neighbors.get(2);
            case UP -> neighbors.get(3);
            case DOWN -> neighbors.get(4);
            case NONE -> this;
        };
    }
}
