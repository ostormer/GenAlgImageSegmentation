import java.util.*;

public class Segment {
    private CieLab centroid; // Average color in segment

    private final Individual individual;
    private Set<Pixel> pixels; // TODO: why Set and not List?
    private int size;
    public final double connectivity, edgeValue, deviation; // The objectives

    public Segment(Individual individual, Set<Pixel> pixels) {
        this.individual = individual;
        this.pixels = pixels;
        this.size = pixels.size();
        updateCentroid();
        this.connectivity = computeConnectivity();
        this.edgeValue = computeEdgeValue();
        this.deviation = computeDeviation();
    }

    /**
     * Returns whether this segment contains given pixel, if pixel is not null
     *
     * @param pixel pixel to look for in segment
     * @return boolean, true if pixel is in segment
     */
    public boolean containsPixel(Pixel pixel) {
        if (pixel == null) {
            return false;
        } else {
            return pixels.contains(pixel);
        }
    }

    public void addPixels(Set<Pixel> pixels) {
        this.pixels.addAll(pixels);
    }

    public void updateCentroid() {
        float l = 0;
        float a = 0;
        float b = 0;
        for (Pixel p : this.pixels) {
            l += p.color.l;
            a += p.color.a;
            b += p.color.b;
        }
        centroid = new CieLab(l / size, a / size, b / size);
    }

    public double computeEdgeValue() {
        // Segment edge "contrast". Difference between neighboring colors on opposing sides of the edge
        // This objective should be maximized. Negative is returned so it should be minimized instead
        int edgeValue = 0;
        for (Pixel pixel : this.pixels) {
            Collection<Pixel> neighbours = pixel.getNeighbors().values();
            for (Pixel neighbour : neighbours) {
                if (!this.containsPixel(neighbour)) { // If pixels are not in the same Segment
                    edgeValue += CieLab.computeDistance(pixel.color, neighbour.color);
                }
            }
        }
        return -edgeValue;
    }

    public double computeConnectivity() {
        // Penalize segments with weirdly shaped edges.
        // This objective should be minimized
        double connectivity = 0;
        for (Pixel pixel : this.pixels) {
            for (Pixel neighbor : pixel.getNeighbors().values()) {
                if (!this.containsPixel(neighbor)) {
                    connectivity += 0.125;  // TODO: test 1/F where F is neighbor-number of pixel
                }
            }
        }
        return connectivity;
    }

    public double computeDeviation() {
        // Segment color deviation from centroid
        // This objective should be minimized
        return pixels.stream()
                .map(pixel -> CieLab.computeDistance(pixel.color, this.centroid))
                .reduce(0.0, Double::sum);
    }

    /**
     * Checks whether pixel is at the edge of segment
     *
     * @param pixel to check if is at edge (MUST BE IN THIS SEGMENT)
     * @return true if pixel is in segment, false if not.
     */
    public boolean isPixelAtEdge(Pixel pixel) {
        if (pixel.x == 0 ||
                pixel.x == getIndividual().getImage().getWidth() - 1 ||
                pixel.y == 0 ||
                pixel.y == getIndividual().getImage().getHeight() - 1) {
            return true;
        }
        return this.containsPixel(pixel.getNeighborByGene(Gene.DOWN))
                && this.containsPixel(pixel.getNeighborByGene(Gene.RIGHT));
    }

    public Individual getIndividual() {
        return individual;
    }

    public Set<Pixel> getPixels() {
        return pixels;
    }
}

