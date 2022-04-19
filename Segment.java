import java.util.Collection;
import java.util.List;

public class Segment {
    private CieLab centroid; // Average color in segment
    private List<Pixel> pixels; // TODO: why Set and not List?
    private int size;
    public final double connectivity, edgeValue, deviation; // The objectives

    public Segment() {
        this.connectivity = computeConnectivity();
        this.edgeValue = computeEdgeValue();
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
        // This objective should be maximized. Negative is returned so it should be minimized instead
        int edgeValue = 0;
        for (Pixel pixel : this.pixels) {
            Collection<Pixel> neighbours = pixel.getNeighbors().values();
            for (Pixel neighbour : neighbours) {
                if (!this.containsPixel(neighbour)) { // If pixels are not in the same Segment
                    edgeValue += CieLab.distance(pixel.color, neighbour.color);
                }
            }
        }
        return -edgeValue;
    }

    public double computeConnectivity() {
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
        double deviation = 0;

        deviation = pixels.stream()
                .map(pixel -> CieLab.distance(pixel.color, this.centroid))
                .reduce(0.0, (total, element) -> total + element);
        return deviation;
    }

}

