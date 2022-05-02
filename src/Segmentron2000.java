package src;

import src.GenAlg;
import src.ImageHandler;
import src.Individual;
import src.Params;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Segmentron2000 {

    private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Params.threadPoolSize);
    public static void main(String[] args) {
        run();
    }

    private static void run(){
        // Set up paths and directories
        Path pathType1 = Path.of(Params.outputDirectory, Params.imageName, "type1");
        Path pathType2 = Path.of(Params.outputDirectory, Params.imageName, "type2");
        Path pathType3 = Path.of(Params.outputDirectory, Params.imageName, "type3");
        try {
            if (!pathType1.toFile().exists()) {
                Files.createDirectories(pathType1.getParent());
                Files.createDirectory(pathType1);
            }
            if (!pathType2.toFile().exists()) {
                Files.createDirectories(pathType2.getParent());
                Files.createDirectory(pathType2);
            }
            if (!pathType3.toFile().exists()) {
                Files.createDirectories(pathType3.getParent());
                Files.createDirectory(pathType3);
            }
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        if (Params.deleteOldFiles) {
            ImageHandler.deleteAllFilesInDir(pathType1);
            ImageHandler.deleteAllFilesInDir(pathType2);
            ImageHandler.deleteAllFilesInDir(pathType3);
        }

        final ImageHandler img;
        try {
            img = new ImageHandler(Params.imageName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GenAlg ga = new GenAlg(img);
        List<Individual> best;
        if (Params.useSimpleGA) { // Not used, other alternative does better
            ga.runGA();
            List<Individual> final_pop = ga.getPop();
            final_pop.sort(Comparator.comparingDouble(Individual::computeCombinedFitness));
            best = final_pop.subList(0, 5);
        } else {
            ga.runGA2();
            best = ga.rankPopulation(ga.getPop()).get(0); // best is the pareto-front. ie. the tied first place

        }
        System.out.println("Merging and saving images");
        for (int i=0; i<best.size(); i++) {
            Individual individual = best.get(i);
            int finalI = i;
            executor.execute(()-> {
                if (Params.mergeSmallSegments){
                    individual.mergeSmallSegments();
                }
                img.save(individual, 1, finalI);
                img.save(individual, 2, finalI);
                img.save(individual, 3, finalI);
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()){
            ;
        }
        System.out.println("FINISHED!");
        if (!Params.useSimpleGA) {
            List<List<Individual>> rankedPopulation = ga.getRankedPopulation();
            System.out.println("Size of pareto fronts:");
            for (List<Individual> individuals : rankedPopulation) {
                System.out.print(individuals.size() + " ");
            }
            System.out.println("");
        }
        System.exit(0);
    }
}
