import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ImageHandler {
    private final int width, height;
    private final String name;
    private BufferedImage image;
    private Pixel[][] pixels; // To hold the actual image

    /**
     * @param directory path of directory containing image file called 'Test image.jpg'
     */
    public ImageHandler(String directory) throws IOException {
        File file = new File(directory + "/Test image.jpg");
        InputStream input = new FileInputStream(file);
        this.image = ImageIO.read(input);

        this.name = directory;
        this.width = image.getWidth();
        this.height = image.getHeight();

        // Add pixels
        this.pixels = new Pixel[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = new Color(image.getRGB(x, y));
                CieLab cieLab = CieLab.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
                Pixel pixel = new Pixel(cieLab, x, y);
                this.pixels[x][y] = pixel;
            }
        }
        // Update neighbors
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[x][y].setNeighbors(findPixelNeighbors(x, y));
            }
        }
    }

    public void save(String directory, String solution) {
        String filename = name + ".jpg"; // TODO: Add more info to filename
        File output = new File(directory + filename);
        BufferedImage outImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    }

    private Map<Integer, Pixel> findPixelNeighbors(int x, int y) {
        Map<Integer, Pixel> neighbors = new HashMap<>();
        // Check whether the pixel is at the edge, add neighbors accordingly.
        if (x < width - 1) {
            neighbors.put(1, pixels[x + 1][y]);
        }
        if (x > 0) {
            neighbors.put(2, pixels[x - 1][y]);
        }
        if (y > 0) {
            neighbors.put(3, pixels[x][y - 1]);
        }
        if (y < height - 1) {
            neighbors.put(4, pixels[x][y + 1]);
        }
        if (x < width - 1 && y > 0) {
            neighbors.put(5, pixels[x + 1][y - 1]);
        }
        if (x < width - 1 && y < height - 1) {
            neighbors.put(6, pixels[x + 1][y + 1]);
        }
        if (x > 0 && y > 0) {
            neighbors.put(7, pixels[x - 1][y - 1]);
        }
        if (x > 0 && y < height - 1) {
            neighbors.put(8, pixels[x - 1][y + 1]);
        }
        return neighbors;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getName() {
        return name;
    }

    public BufferedImage getImage() {
        return image;
    }

    public Pixel[][] getPixels() {
        return pixels;
    }
}
