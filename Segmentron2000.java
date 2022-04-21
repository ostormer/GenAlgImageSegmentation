import java.io.IOException;

public class Segmentron2000 {
    public static void main(String[] args) throws IOException {
        ImageHandler img = new ImageHandler("train/86016");
        GenAlg ga = new GenAlg(img);
        ga.runGA();
    }
}
