import java.util.HashMap;
import java.util.Map;

public class Pixel {
    public final CieLab color;
    public final int x, y; // Coordinates of pixel
    // Map store neighboring pixels. Filled after all pixels are created
    private Map<Gene, Pixel> neighbors = new HashMap<>();

    public Pixel(CieLab color, int x, int y) {
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public Map<Gene, Pixel> getNeighbors() {
        return neighbors;
    }
}
