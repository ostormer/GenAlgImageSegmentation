package src;

/**
 * src.Edge between two neighboring pixels, used to compare color differences as described in the paper
 */
public class Edge implements Comparable<Edge>{
    public Pixel from, to;
    public double distance;

    public Edge(Pixel from, Pixel to){
        this.from = from;
        this.to = to;
        this.distance = CieLab.computeDistance(from.color, to.color);
    }

    @Override
    public int compareTo(Edge e) {
        return Double.compare(this.distance, e.distance);
//        if (this.distance > e.distance) return 1;
//        if (e.distance > this.distance) return -1;
//        return 0;
    }

}