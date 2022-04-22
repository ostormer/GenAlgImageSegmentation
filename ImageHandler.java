import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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

    public void save(String directory, Individual solution, int segmentationType) {
        if (segmentationType != 1 && segmentationType != 2 && segmentationType != 3) {
            throw new IllegalArgumentException("segmentationType must be either 1 (green on image) or 2 (black on white).");
        }
        String filename = name + "_t" + segmentationType + "_" + solution.getNumSegments() + ".jpg"; // TODO: Add more info to filename
        File output = new File(directory + "/" + filename);
        BufferedImage outImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int outlineColor;
        if (segmentationType == 1) {
            outlineColor = new Color(0, 255, 0).getRGB();
            for (Segment segment : solution.getSegments()) {
                for (Pixel pixel : segment.getPixels()) {
                    if (segment.isPixelAtEdge(pixel)) {
                        image.setRGB(pixel.x, pixel.y, outlineColor);
                    }
                }
            }
        } else if (segmentationType == 2) {
            outlineColor = new Color(0, 0, 0).getRGB();
            int fillColor = new Color(255, 255, 255).getRGB();
            for (Segment segment : solution.getSegments()) {
                for (Pixel pixel : segment.getPixels()) {
                    if (segment.isPixelAtEdge(pixel)) {
                        image.setRGB(pixel.x, pixel.y, outlineColor);
                    } else {
                        image.setRGB(pixel.x, pixel.y, fillColor);
                    }
                }
            }
        } else {
            outlineColor = new Color(0, 0, 0).getRGB();
            Random rand = new Random();
            for (Segment segment : solution.getSegments()) {
                // Select random color for segment
                final float hue = rand.nextFloat();
                // Saturation between 0.1 and 0.3
                final float saturation = (rand.nextInt(2000) + 1000) / 10000f;
                final float luminance = 0.9f;
                int fillColor = Color.getHSBColor(hue, saturation, luminance).getRGB();
                for (Pixel pixel : segment.getPixels()) {
                    image.setRGB(pixel.x, pixel.y, fillColor);
//                    if (segment.isPixelAtEdge(pixel)) {
//                        image.setRGB(pixel.x, pixel.y, outlineColor);
//                    } else {
//                        image.setRGB(pixel.x, pixel.y, fillColor);
//                    }
                }
            }
        }
        // Finished drawing all pixels, save image
        try {
            ImageIO.write(image, "jpg", output);
        } catch (
                IOException e) {
            e.printStackTrace();
        }
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

    public BufferedImage getBufferedImage() {
        return image;
    }

    public Pixel[][] getPixels() {
        return pixels;
    }
}
