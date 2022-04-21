import java.util.*;

public class Segment {
    private CieLab centroid; // Average color in segment
    private Set<Pixel> pixels = new HashSet<>(); // TODO: why Set and not List?
    private int size;
    public final double connectivity, edgeValue, deviation; // The objectives

    public Segment() {
        this.connectivity = computeConnectivity();
        this.edgeValue = computeEdgeValue();
        this.deviation = computeDeviation();
        this.size = pixels.size();
    }

    public Segment(Set<Pixel> pixels) {
        this();
        this.pixels = pixels;
        this.size = pixels.size();
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

    public void addPixels(Set<Pixel> pixels){
        this.pixels.addAll(pixels);
    }


    public CieLab computeCentroid() {
        float l = 0;
        float a = 0;
        float b = 0;
        for (Pixel p : this.pixels) {
            l += p.color.l;
            a += p.color.a;
            b += p.color.b;
        }
        return new CieLab(l / size, a / size, b / size);
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
        double deviation = pixels.stream()
                .map(pixel -> CieLab.computeDistance(pixel.color, this.centroid))
                .reduce(0.0, (total, element) -> total + element);
        return deviation;
    }

    public Set<Pixel> getPixels() {
        return pixels;
    }
}

