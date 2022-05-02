package src;

public class Params {
    public static final String imageName = "118035"; // Use folder name in training_images
    // Create popsize/2 initial population, is doubled on first generation
    public static final int numGenerations = 100;
    public static final int popSize = 20;
    public static final int parentSelectionSize = 10;
    public static final double tournamentProb = 0.8;
    public static final double crossoverProb = 0.8;
    public static final double mutationProb = 0.2;
    // Probability of merging best edge, 1-p for random edge
    public static final double mergeMutationEpsilon = 0.7;
    public static final int threadPoolSize = 10;

    // Whether or not to use a simple GA with weighted loss
    public static final boolean useSimpleGA = false;

    public static final boolean mergeSmallSegments = true;
    // Used in mergemutations to select candidates and merging in final step
    public static final int mergeableSegmentLimit = 200;
    public static final int mergeTries = 10;

    // Weighted fitness params
    public static final double weightEdgeValue = 3; // 2;
    public static final double weightConnectivity = 100; // 100;
    public static final double weightDeviation = 10; // 10;

    // Misc
    public static final boolean deleteOldFiles = true;
    public static final String outputDirectory = "segmented_images";

}
