package src;

import java.util.*;

/**
 * A single segmentation
 */
public class Individual {
    private final Random rand = new Random();
    private List<Gene> genotype;
    private final ImageHandler image;

    private int rank;
    private int numSegments; // Number of segments
    private int prevMergeableSegments = 0; // number of segments that should be merged, before previous merge
    private List<Segment> segments = new ArrayList<>();
    private double deviation, edgeValue, connectivity; // The three objectives to optimize
    private double crowdingDistance;

    public Individual(ImageHandler image, int numSegments) {
        this.numSegments = numSegments;
        this.image = image;
        generateMinSpanTree(); // Generate random genotype
        createSegments();
    }

    public Individual(ImageHandler image, List<Gene> genotype) {
        this.image = image;
        this.genotype = genotype;
        createSegments();
    }

    /**
     * Uses Prims algorithm to construct a minimal span tree
     */
    public void generateMinSpanTree() {
        int randX = rand.nextInt(image.getWidth());
        int randY = rand.nextInt(image.getHeight());
        int totalNodes = image.getWidth() * image.getHeight();

        // Initialize genotype to only point at itself
        this.genotype = new ArrayList<>();
        for (int i = 0; i < totalNodes; i++) {
            this.genotype.add(Gene.NONE);
        }
        // Initialize priorityQueue of Edges and list of visitedNodes
        PriorityQueue<Edge> priorityQueue = new PriorityQueue<>();
        List<Edge> createdEdges = new ArrayList<>();
        Set<Pixel> visitedNodes = new HashSet<>(); // Use hashSet because we want to test whether it contains
        // Initial choice of pixel is randomized
        Pixel current = image.getPixels()[randX][randY];
        // Make sure that all nodes are visited once
        while (visitedNodes.size() < totalNodes) {
            if (!visitedNodes.contains(current)) {
                // Add to priorityQueue if not already there
                visitedNodes.add(current);
                Pixel finalCurrent = current;
                List<Edge> edges = Arrays.stream(new Gene[]{Gene.RIGHT, Gene.LEFT, Gene.UP, Gene.DOWN})
                        .map(current::getNeighborByGene)
                        .filter(Objects::nonNull)
                        .map(neighbor -> new Edge(finalCurrent, neighbor)).toList();
                priorityQueue.addAll(edges);
            }
            // Get current best edge (measured in RGB-distance between pixels)
            Edge e = priorityQueue.poll();
            if (!visitedNodes.contains(e.to)) {
                // Set genotype to the corresponding gene to get from pixel to pixel
                updateGenotype(e.from, e.to);
                createdEdges.add(e);
            }
            current = e.to;
        }
        Collections.sort(createdEdges);
        // Remove the worst edges, as to create noOfSegments initial segments
        for (int i = 0; i < this.numSegments - 1; i++) {
            Edge removedEdge = createdEdges.get(i);
            updateGenotype(removedEdge.from, removedEdge.from);
        }
    }

    /**
     * Creates segments according to this individual's genotype
     */
    private void createSegments() {
        List<Segment> tempSegments = new ArrayList<>();
        Pixel currentPixel;
        int currentIndex;
        boolean[] visitedNodes = new boolean[genotype.size()];
        Arrays.fill(visitedNodes, false);
        Set<Pixel> segmentPixels;
        for (int i = 0; i < genotype.size(); i++) {
            // If already visited, skip
            if (visitedNodes[i]) {
                continue;
            }
            // Select pixel at index, add to segment and visitedNodes
            segmentPixels = new HashSet<>();
            Pair<Integer, Integer> pixelIndex = GenAlg.genotypeIndexToCoords(i, image.getHeight());
            currentPixel = this.image.getPixels()[pixelIndex.x][pixelIndex.y];
            segmentPixels.add(currentPixel);
            visitedNodes[i] = true;
            // Move on to neighbor as defined by genotype
            currentPixel = currentPixel.getNeighborByGene(genotype.get(i));
            currentIndex = GenAlg.coordsToGenotypeIndex(currentPixel.x, currentPixel.y, image.getHeight());
            // While the neighbor has not been visited previously, keep moving
            while (!visitedNodes[currentIndex]) {
                segmentPixels.add(currentPixel);
                visitedNodes[currentIndex] = true;
                currentPixel = currentPixel.getNeighborByGene(genotype.get(currentIndex));
                currentIndex = GenAlg.coordsToGenotypeIndex(currentPixel.x, currentPixel.y, image.getHeight());
            }
            // If last visited node has been visited before and does not point to itself, merge segments
            if (this.image.getPixels()[pixelIndex.x][pixelIndex.y] != currentPixel) {
                boolean flag = false;
                for (Segment s : tempSegments) {
                    if (s.containsPixel(currentPixel)) {
                        s.addPixels(segmentPixels);
                        flag = true;
                        break;
                    }
                }
                // If we reach a node with NONE as src.Gene, which does not belong to another segment
                // it should create a new segment
                if (!flag) {
                    tempSegments.add(new Segment(this, segmentPixels));
                }
                // Else create new segment
            } else {
                tempSegments.add(new Segment(this, segmentPixels));
            }
        }
        // Update number of segments
        this.numSegments = tempSegments.size();
        this.segments = tempSegments;
        this.updateObjectiveValues();
    }

    /**
     * An src.Individual dominates another if it beats it on all three objectives
     *
     * @param other src.Individual to compare to.
     * @return whether this dominates other.
     */
    public boolean dominates(Individual other) {
        return this.connectivity < other.connectivity
                && this.deviation < other.deviation
                && this.edgeValue < other.edgeValue;
    }

    public boolean isStrictlyFitterThan(Individual other) {
        if (Params.useSimpleGA) {
            return this.computeCombinedFitness() < other.computeCombinedFitness();
        } else {
            return this.getRank() < other.getRank();
        }
    }

    public double computeCombinedFitness() {
        return Params.weightConnectivity * this.connectivity
                + Params.weightDeviation * this.deviation
                + Params.weightEdgeValue * this.edgeValue;
    }

    // Supports multithreading, but needs to be parallelized from GA method
    public void mutationMergeSegments(Random threadLocalRandom) {
        // Find segments with fewer pixels than minimumSegmentSize
        List<Segment> candidates = segments.stream()
                .filter(segment -> segment.getPixels().size() < Params.mergeableSegmentLimit).toList();
        if (candidates.size() == 0) {
            return;
        }
        Segment pick1 = candidates.get(threadLocalRandom.nextInt(candidates.size()));
        Edge merge = threadLocalRandom.nextDouble() > Params.mergeMutationEpsilon
                ? getRandomSegmentEdge(pick1, threadLocalRandom)
                : getBestSegmentEdge(pick1);

        if (merge != null) {
            updateGenotype(merge.to, merge.from);
            createSegments();
        }
    }

    public void mergeSmallSegments() {
        this.mergeSmallSegments(0);
    }

    /**
     * Find segments under the merge limit and merge them to their best neighbor
     * Runs recursively until it is done merging
     * @param merge_number set this to 0 when calling it, recursive calls increment it
     */
    public void mergeSmallSegments(int merge_number){
        List<Segment> mergeableSegments = new ArrayList<>();
        // Find segments with fewer pixels than threshold
        for (Segment s: this.segments){
            if (s.getPixels().size() < Params.mergeableSegmentLimit){
                mergeableSegments.add(s);
            }
        }
        // If no merge was made previous run, increment tries counter
        if (mergeableSegments.size() == this.prevMergeableSegments) merge_number++;
        // If no merge is needed or tries exceeded; exit condition
        if (mergeableSegments.size() == 0 || merge_number > Params.mergeTries) return;
        // Find the best edge from each segment to merge
        for (Segment s: mergeableSegments){
            Edge merge = getBestSegmentEdge(s);
            if (merge != null){
                updateGenotype(merge.to, merge.from);
            }
        }
        // Update prevMergeableSegments and create segments based on new genotype
        this.prevMergeableSegments = mergeableSegments.size();
        this.createSegments();
        // Recursively run until exit condition is reached
        this.mergeSmallSegments(merge_number);
    }

    private void updateGenotype(Pixel from, Pixel to) {
        if (Objects.equals(from, to)) {
            this.genotype.set(GenAlg.coordsToGenotypeIndex(from.x, from.y, image.getHeight()), Gene.NONE);
            return;
        }
        // Sets the gene at e.to to point towards e.from as an MST can only have one parent but multiple
        // children
        this.genotype.set(
                GenAlg.coordsToGenotypeIndex(to.x, to.y, image.getHeight()),
                Gene.fromUnitVector(from.x - to.x, from.y - to.y));
    }

    private Edge getBestSegmentEdge(Segment segment) {
        Edge bestEdge = null;
        double bestDistance = Integer.MAX_VALUE;
        // Iterate through pixels in segment, find neighbours
        for (Pixel p : segment.getPixels()) {
            for (Pixel n : p.getCardinalNeighbors().values()) {
                // Assign neighbours who are not in the same segment to a new src.Edge candidate
                if (!segment.containsPixel(n)) {
                    Edge temp = new Edge(p, n);
                    if (temp.distance < bestDistance) {
                        // Update bestEdge to keep the edge with the lowest distance in RGB-space
                        bestDistance = temp.distance;
                        bestEdge = temp;
                    }
                }
            }
        }
        return bestEdge;
    }

    private Edge getRandomSegmentEdge(Segment segment, Random threadLocalRandom) {
        List<Edge> candidates = new ArrayList<>();
        // Iterate through pixels in segment, find neighbours
        for (Pixel p : segment.getPixels()) {
            for (Pixel n : p.getCardinalNeighbors().values()) {
                // Assign neighbours who are not in the same segment to a new src.Edge candidate
                if (!segment.containsPixel(n)) {
                    candidates.add(new Edge(p, n));
                }
            }
        }
        return candidates.size() > 0
                ? candidates.get(threadLocalRandom.nextInt(candidates.size()))
                : null;
    }


    public double getObjectiveValue(Objective objective) {
        return switch (objective) {
            case CONNECTIVITY -> connectivity;
            case EDGE_VALUE -> edgeValue;
            case DEVIATION -> deviation;
        };
    }

    private void updateObjectiveValues() {
        this.deviation = segments.stream()
                .map(segment -> segment.deviation)
                .reduce(0.0, Double::sum);
        this.edgeValue = segments.stream()
                .map(segment -> segment.edgeValue)
                .reduce(0.0, Double::sum);
        this.connectivity = segments.stream()
                .map(segment -> segment.connectivity)
                .reduce(0.0, Double::sum);
    }

    public List<Gene> getGenotype() {
        return genotype;
    }

    public void setGenotype(List<Gene> genotype) {
        this.genotype = genotype;
    }

    public int getNumSegments() {
        return numSegments;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getDeviation() {
        return deviation;
    }

    public void setDeviation(double deviation) {
        this.deviation = deviation;
    }

    public double getEdgeValue() {
        return edgeValue;
    }

    public void setEdgeValue(double edgeValue) {
        this.edgeValue = edgeValue;
    }

    public double getConnectivity() {
        return connectivity;
    }

    public void setConnectivity(double connectivity) {
        this.connectivity = connectivity;
    }

    public double getCrowdingDistance() {
        return crowdingDistance;
    }

    public void setCrowdingDistance(double crowdingDistance) {
        this.crowdingDistance = crowdingDistance;
    }

    public ImageHandler getImage() {
        return image;
    }
}
