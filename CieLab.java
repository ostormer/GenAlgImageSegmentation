import java.util.Objects;

public class CieLab {

    public final float l, a, b;
//    private int red, green, blue;

    public CieLab(float l, float a, float b) {
        this.l = l;
        this.a = a;
        this.b = b;
    }

    public static CieLab fromRGB(int R, int G, int B) {
        // Stolen from http://www.brucelindbloom.com
        float eps = 216.f / 24389.f;
        float k = 24389.f / 27.f;

        float Xr = 0.964221f;  // reference white D50
        float Yr = 1.0f;
        float Zr = 0.825211f;

        // RGB to XYZ
        float r = R / 255.f; //R 0..1
        float g = G / 255.f; //G 0..1
        float b = B / 255.f; //B 0..1

        // assuming sRGB (D65)
        if (r <= 0.04045)
            r = r / 12;
        else
            r = (float) Math.pow((r + 0.055) / 1.055, 2.4);

        if (g <= 0.04045)
            g = g / 12;
        else
            g = (float) Math.pow((g + 0.055) / 1.055, 2.4);

        if (b <= 0.04045)
            b = b / 12;
        else
            b = (float) Math.pow((b + 0.055) / 1.055, 2.4);

        float X = 0.436052025f * r + 0.385081593f * g + 0.143087414f * b;
        float Y = 0.222491598f * r + 0.71688606f * g + 0.060621486f * b;
        float Z = 0.013929122f * r + 0.097097002f * g + 0.71418547f * b;

        // XYZ to Lab
        float xr = X / Xr;
        float yr = Y / Yr;
        float zr = Z / Zr;

        float fx, fy, fz;
        if (xr > eps)
            fx = (float) Math.pow(xr, 1 / 3.);
        else
            fx = (float) ((k * xr + 16.) / 116.);

        if (yr > eps)
            fy = (float) Math.pow(yr, 1 / 3.);
        else
            fy = (float) ((k * yr + 16.) / 116.);

        if (zr > eps)
            fz = (float) Math.pow(zr, 1 / 3.);
        else
            fz = (float) ((k * zr + 16.) / 116);

        float Ls = (116 * fy) - 16;
        float as = 500 * (fx - fy);
        float bs = 200 * (fy - fz);

        float lab_L = (int) (2.55 * Ls + .5);
        float lab_A = (int) (as + .5);
        float lab_B = (int) (bs + .5);
        return new CieLab(lab_L, lab_A, lab_B);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof CieLab)) {
            return false;
        }
        CieLab rgb = (CieLab) o;
        return l == rgb.l && a == rgb.a && b == rgb.b;
    }

    @Override
    public int hashCode() {
        return Objects.hash(l, a, b);
    }

    /**
     * Calculate Euclidean distance between two CieLab colors
     * Euclidean distance is the main advantage of using CieLab
     *
     * @param one first color
     * @param two second color
     * @return distance
     */
    public static double computeDistance(CieLab one, CieLab two) {
        return Math.sqrt(
                Math.pow(one.l - two.l, 2)
                        + Math.pow(one.a - two.a, 2)
                        + Math.pow(one.b - two.b, 2)
        );
    }
}
