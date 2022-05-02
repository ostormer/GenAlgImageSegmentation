package src;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ImageHandler {
    private final int width, height;
    private final String name;
    private BufferedImage image;
    private Pixel[][] pixels; // To hold the actual image

    /**
     * @param imageName name of directory in train folder containing image file called 'Test image.jpg'
     */
    public ImageHandler(String imageName) throws IOException {
        File file = new File("train/" + imageName + "/Test image.jpg");
        InputStream input = new FileInputStream(file);
        this.image = ImageIO.read(input);

        this.name = imageName;
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

    /**
     * Copy all pixels of a bufferedImage, so original instance remains unedited
     * @param bi original image
     * @return copy of bufferedImage
     */
    private static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    public void save(Individual solution, int segmentationType, int individualID) {
        if (segmentationType != 1 && segmentationType != 2 && segmentationType != 3) {
            throw new IllegalArgumentException("segmentationType must be either 1 (green on image), 2 (black on white) or 3 (colors)");
        }
        Path directory = Path.of(Params.outputDirectory, name, "type" + Integer.toString(segmentationType));
        String filename = "t" + segmentationType
                + "_s%02d".formatted(solution.getNumSegments())
//                + "_id%02d".formatted(individualID)
                + "_ev%07.0f".formatted(solution.getEdgeValue())
                + "_c%04.0f".formatted(solution.getConnectivity())
                + "_d%05.0f".formatted(solution.getDeviation())
                + ".png";
        File output = new File( directory + "/" + filename);
        BufferedImage outImage = deepCopy(image);
        // Copy contents of image into outImage


        int outlineColor;
        if (segmentationType == 1) {
            outlineColor = new Color(0, 255, 0).getRGB();
            for (Segment segment : solution.getSegments()) {
                for (Pixel pixel : segment.getPixels()) {
                    if (segment.isPixelAtEdge(pixel)) {
                        outImage.setRGB(pixel.x, pixel.y, outlineColor);
                    }
                }
            }
        } else if (segmentationType == 2) {
            outlineColor = new Color(0, 0, 0).getRGB();
            int fillColor = new Color(255, 255, 255).getRGB();
            for (Segment segment : solution.getSegments()) {
                for (Pixel pixel : segment.getPixels()) {
                    if (segment.isPixelAtEdge(pixel)) {
                        outImage.setRGB(pixel.x, pixel.y, outlineColor);
                    } else {
                        outImage.setRGB(pixel.x, pixel.y, fillColor);
                    }
                }
            }
        } else {
            Random rand = new Random();
            for (Segment segment : solution.getSegments()) {
                // Select random color for segment
                final float hue = rand.nextFloat();
                // Saturation between 0.1 and 0.3
                final float saturation = (rand.nextInt(2000) + 1000) / 10000f;
                final float luminance = 0.9f;
                int fillColor = Color.getHSBColor(hue, saturation, luminance).getRGB();
                for (Pixel pixel : segment.getPixels()) {
                    outImage.setRGB(pixel.x, pixel.y, fillColor);
                }
            }
        }
        // Finished drawing all pixels, save image
        try {
            ImageIO.write(outImage, "png", output);
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllFilesInDir(Path path){
        for (File file : path.toFile().listFiles())
            if (!file.isDirectory())
                file.delete();
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
