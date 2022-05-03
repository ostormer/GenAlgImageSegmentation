package src;

import java.util.*;
import java.util.concurrent.*;

public class GenAlg {
    private final ImageHandler image;
    private List<Individual> pop;
    private List<List<Individual>> rankedPopulation;
    private final int genotypeLength;

    private final Executor executor = Executors.newFixedThreadPool(Params.threadPoolSize);
    private final Random rand = ThreadLocalRandom.current();

    public GenAlg(ImageHandler image) {
        this.image = image;
        this.genotypeLength = image.getHeight() * image.getWidth();
    }

    public void runGA() {
        int currentGen = 0;
        generatePop();
        while (currentGen < Params.numGenerations) { // Run GA
            System.out.printf("Generation: %d%n", currentGen); // TODO: modify and improve print
            List<Individual> parents = parentSelection(this.pop);
            List<Individual> newPopulation = Collections.synchronizedList(new ArrayList<>());
            for (int i = 0; i < Params.popSize / 2; i++) { // Crossover pairs of parents from parentSelection
                executor.execute(() -> {
                    Random threadLocalRand = ThreadLocalRandom.current();
                    Individual parent1 = parents.get(threadLocalRand.nextInt(parents.size()));
                    Individual parent2 = parents.get(threadLocalRand.nextInt(parents.size()));
                    Pair<Individual, Individual> offspring = crossover(parent1, parent2, threadLocalRand);
                    newPopulation.add(offspring.x);
                    newPopulation.add(offspring.y);
                    if (rand.nextDouble() < Params.mutationMergeProb) {
                        offspring.x.mutationMergeSegments(threadLocalRand);
                    }
                    if (rand.nextDouble() < Params.mutationMergeProb) {
                        offspring.y.mutationMergeSegments(threadLocalRand);
                    }
                });
            }
            while (newPopulation.size() != Params.popSize) {
                ; // Synchronize threads
            }
            for (Individual individual : newPopulation) {
                if (rand.nextDouble() < Params.mutationProb) {
                    individual.mutationMergeSegments(rand);
                }
            }
            this.pop = newPopulation;
            currentGen++;
        }
    }

    public void runGA2() { // Genetic alg where parents may survive until next generation if they perform well
        int currentGen = 0;
        generatePop();
        rankPopulation(this.pop);
        while (currentGen < Params.numGenerations) { // Run GA
            System.out.printf("Generation: %d%n", currentGen); // TODO: modify and improve print
            List<Individual> parents = parentSelection(this.pop);
            List<Individual> newPopulation = Collections.synchronizedList(new ArrayList<>());
            for (int i = 0; i < Params.popSize / 2; i++) {
                executor.execute(() -> {
                    Random threadLocalRand = ThreadLocalRandom.current();
                    Individual parent1 = parents.get(threadLocalRand.nextInt(parents.size()));
                    Individual parent2 = parents.get(threadLocalRand.nextInt(parents.size()));
                    Pair<Individual, Individual> offspring = crossover(parent1, parent2, threadLocalRand);
                    if (rand.nextDouble() < Params.mutationMergeProb) {
                        offspring.x.mutationMergeSegments(threadLocalRand);
                    }
                    if (rand.nextDouble() < Params.mutationMergeProb) {
                        offspring.y.mutationMergeSegments(threadLocalRand);
                    }
                    newPopulation.add(offspring.x);
                    newPopulation.add(offspring.y);
                });
            }
            while (newPopulation.size() != Params.popSize) {
                ; // Synchronize threads
            }
            for (Individual individual : newPopulation) {
                if (rand.nextDouble() < Params.mutationProb) {
                    individual.mutationMergeSegments(rand);
                }
            }
            this.pop.addAll(newPopulation);
            this.rankedPopulation = rankPopulation(this.pop);
            newPopulationFromRank();
            currentGen++;
        }
    }


    private void generatePop() {
        System.out.println("Generating initial population...");
        // pop is synchronized for multithreading
        List<Individual> newPopulation = Collections.synchronizedList(new ArrayList<>());
        ThreadPoolExecutor tempExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Params.threadPoolSize);

        for (int i = 0; i < Params.popSize; i++) {
            tempExecutor.execute(() -> {
                Individual ind = new Individual(this.image, ThreadLocalRandom.current().nextInt(5, 35)); // TODO: Test values
                System.out.printf("src.Individual created. segments: %d, genotype length: %d%n", ind.getNumSegments(), ind.getGenotype().size());
                newPopulation.add(ind);
            });
        }
        tempExecutor.shutdown();
        try {
            if (!tempExecutor.awaitTermination(1, TimeUnit.MINUTES)){
                System.out.println("Timeout while generating pop");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Done creating initial population!");
        this.pop = newPopulation;
    }

    private List<Individual> parentSelection(List<Individual> population) {
        List<Individual> selected = new ArrayList<>();
        Random rand = ThreadLocalRandom.current();
        while (selected.size() < Params.parentSelectionSize) {
            Individual parent1 = population.get(rand.nextInt(population.size()));
            Individual parent2 = population.get(rand.nextInt(population.size()));

            if (rand.nextDouble() < Params.tournamentProb) {
                if (parent1.isStrictlyFitterThan(parent2)) {
                    selected.add(parent1);
                } else if (parent2.isStrictlyFitterThan(parent1)) {
                    selected.add(parent2);
                } else if (!Params.useSimpleGA) {
                    if (parent1.getCrowdingDistance() > parent2.getCrowdingDistance()) {
                        selected.add(parent1);
                    } else if (parent1.getCrowdingDistance() < parent2.getCrowdingDistance()) {
                        selected.add(parent2);
                    }
                } else { // Using simple GA with weighted fitness
                    if (parent1.computeCombinedFitness() > parent2.computeCombinedFitness()) {
                        selected.add(parent1);
                    } else {
                        selected.add(parent2);
                    }
                }
            } else { // There is no competition, random is chosen
                selected.add(rand.nextInt(2) == 0 ? parent1 : parent2);
            }
        }
        return selected;
    }

    private Pair<Individual, Individual> crossover(Individual parentA, Individual parentB, Random threadLocalRand) {
        // Copy genotypes so separate threads can't modify them concurrently
        // Or so it does not crash when both parents are the same
        List<Gene> genotypeA = new ArrayList<>(parentA.getGenotype());
        List<Gene> genotypeB = new ArrayList<>(parentB.getGenotype());

        // Crossover by slicing and swapping. Very simple, very dumb.
        if (threadLocalRand.nextDouble() < Params.crossoverProb) {
            int sliceIndex = threadLocalRand.nextInt(genotypeLength);
            List<Gene> temp = new ArrayList<>(genotypeA.subList(sliceIndex, genotypeLength));
            genotypeA.subList(sliceIndex, genotypeLength).clear();
            genotypeA.addAll(genotypeB.subList(sliceIndex, genotypeLength));
            genotypeB.subList(sliceIndex, genotypeLength).clear();
            genotypeB.addAll(temp);
        }
        // Mutate
        genotypeA = mutateRandomGene(genotypeA, threadLocalRand);
        genotypeB = mutateRandomGene(genotypeB, threadLocalRand);
        return new Pair<>(new Individual(this.image, genotypeA), new Individual(this.image, genotypeB));
    }

    /**
     * Mutates a random gene with probability src.Params.mutationProb
     *
     * @param genotype Genotype to mutate
     * @param threadLocalRand Random object to use within thread
     * @return mutated genotype
     */
    public List<Gene> mutateRandomGene(List<Gene> genotype, Random threadLocalRand) {
        if (threadLocalRand.nextDouble() < Params.mutationProb) {
            int randomGeneIndex = threadLocalRand.nextInt(genotype.size()); // Select single random gene
            Pair<Integer, Integer> pixelCoords = genotypeIndexToCoords(randomGeneIndex, this.image.getHeight());
            List<Gene> legalGenes = this.image.getPixels()[pixelCoords.x][pixelCoords.y].getValidGenes();
            genotype.set(randomGeneIndex, legalGenes.get(threadLocalRand.nextInt(legalGenes.size())));
        }
        return genotype;
    }

    public List<List<Individual>> rankPopulation(List<Individual> population) {
        List<List<Individual>> rankedPopulation = new ArrayList<>();
        int currentRank = 1;
        while (population.size() > 0) {
            List<Individual> dominatingSet = findDominatingSet(population);
            for (Individual i : dominatingSet) { // all individuals in dominating have same rank
                i.setRank(currentRank);
            }
            rankedPopulation.add(dominatingSet);
            population.removeAll(dominatingSet);
            currentRank++;
        }
        for (List<Individual> front : rankedPopulation) {
            population.addAll(front);
        }
        return rankedPopulation;
    }

    private List<Individual> findDominatingSet(List<Individual> population) {
        List<Individual> nonDominatedList = new ArrayList<>();
        // Begin with first member of population
//        nonDominatedList.add(population.get(0));
        Set<Individual> dominatedSet = new HashSet<>();

        for (Individual individual : population) {
            if (dominatedSet.contains(individual)) {
                continue;
            }
            // Add to nonDominated before comparison
            nonDominatedList.add(individual);
            // Compare individual to other individuals currently not dominated
            for (Individual nonDominatedInd : nonDominatedList) {
                if (dominatedSet.contains(individual) || nonDominatedInd == individual) {
                    continue;
                }
                // If individual dominates a member of nonDominated, then remove it
                else if (individual.dominates(nonDominatedInd)) {
                    dominatedSet.add(nonDominatedInd);
                }
                // If individual is dominated by any member in nonDominated, it should not be included
                else if (nonDominatedInd.dominates(individual)) {
                    dominatedSet.add(individual);
                    // No need to compare with the rest of the list as domination has a transitive property
                    break;
                }
            }
        }
        nonDominatedList.removeAll(dominatedSet);
        return nonDominatedList;
    }

    private void newPopulationFromRank() {
        this.pop.clear();
        for (List<Individual> paretoFront : this.rankedPopulation) {
            assignCrowdingDistance(paretoFront);
            if (paretoFront.size() <= Params.popSize - this.pop.size()) {
                this.pop.addAll(paretoFront);
            } else {
                List<Individual> copy = new ArrayList<>(paretoFront);
                copy.sort((a, b) -> Double.compare(b.getCrowdingDistance(), a.getCrowdingDistance()));
                this.pop.addAll(copy.subList(0, Params.popSize - this.pop.size()));
            }
        }
    }

    private void assignCrowdingDistance(List<Individual> paretoFront) {
        for (Individual individual : paretoFront) {
            individual.setCrowdingDistance(0);
        }
        for (Objective objective : Objective.values()) {
            assignCrowdingDistanceToIndividuals(paretoFront, objective);
        }
    }

    private void assignCrowdingDistanceToIndividuals(List<Individual> paretoFront, Objective objective) {

        // Reverse sort by lowest to highest for objectives
        paretoFront.sort(Objective.getObjectiveComparator(objective));

        Individual minIndividual = paretoFront.get(0);
        Individual maxIndividual = paretoFront.get(paretoFront.size() - 1);

        maxIndividual.setCrowdingDistance(Integer.MAX_VALUE);
        minIndividual.setCrowdingDistance(Integer.MAX_VALUE);

        double maxMinObjectiveDiff = maxIndividual.getObjectiveValue(objective);
        maxMinObjectiveDiff -= minIndividual.getObjectiveValue(objective);

        double objectiveDiff;

        for (int i = 1; i < paretoFront.size() - 1; i++) {
            objectiveDiff = paretoFront.get(i + 1).getObjectiveValue(objective);
            objectiveDiff -= paretoFront.get(i - 1).getObjectiveValue(objective);
            objectiveDiff /= maxMinObjectiveDiff;
            paretoFront.get(i).setCrowdingDistance(
                    paretoFront.get(i).getCrowdingDistance() + objectiveDiff
            );
        }
    }

    public static int coordsToGenotypeIndex(int x, int y, int height) {
        return height * x + y;
    }

    public static Pair<Integer, Integer> genotypeIndexToCoords(int i, int height) {
        int y = i % height;
        int x = Math.floorDiv(i, height);
        return new Pair<>(x, y);
    }

    public ImageHandler getImage() {
        return image;
    }

    public List<Individual> getPop() {
        return pop;
    }

    public List<List<Individual>> getRankedPopulation() {
        return rankedPopulation;
    }

}
