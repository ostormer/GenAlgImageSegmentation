import java.util.ArrayList;
import java.util.List;

/**
 * A single segmentation
 */
public class Individual {
    private List<Gene> genotype;
    private Pixel[][] pixels;
    private int width, height;

    private int nSegments; // Number of segments
    private List<Segment> segments;
    private double deviation, edgeValue, connectivity; // The three objectives to optimize
    private double crowdingDistance;

    public Individual(Pixel[][] pixels) {
        this.genotype = new ArrayList<>();
        this.segments = new ArrayList<>();
        this.pixels = pixels;
    }
}
