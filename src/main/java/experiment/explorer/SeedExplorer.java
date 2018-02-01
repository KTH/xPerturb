package experiment.explorer;

import experiment.Manager;
import experiment.Tuple;
import experiment.Util;
import experiment.exploration.IntegerExplorationPlusOne;
import perturbation.PerturbationEngine;
import perturbation.location.PerturbationLocation;
import perturbation.log.LoggerImpl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;


/**
 * Created by bdanglot on 02/05/16.
 */
public class SeedExplorer extends CallExplorer {

    public static int numberOfSeed = 15;

    public static int[] seeds;

    private static String nameOfSpecificExploration;

    private static String header;

    private int currentSeed;

    public SeedExplorer(Manager manager, boolean init, int currentSeed) {
        super(manager, new IntegerExplorationPlusOne());
        header = "Contains data to explore the impact of the seed\n";
        header += "Seed Exploration (SE)\n";
        header += "Seed used : ";
        for (int seed : seeds)
            header += seed + " ";
        header += "\n" + super.manager.getLocations().size() + " perturbation point\n";
        header += "N Execution Enactor\n";
        header += "PONE : Numerical Perturbator\n";
        header += "Size of each task : " + super.manager.getSizeOfTask() + "\n";
        header += "Number of task : " + super.manager.getIndexTask().size() + "\n";
        nameOfSpecificExploration = "SeedExploration";
        PerturbationEngine.loggers.put(nameOfSpecificExploration, new LoggerImpl());
        this.currentSeed = currentSeed;
        if (init)
            this.init();
    }

    private void init() {
        List<PerturbationLocation> locations = super.manager.getLocations();
        for (PerturbationLocation location : locations) {
            try {
                FileWriter writer = new FileWriter("results/" + super.manager.getName() + "/" + nameOfSpecificExploration + "_" + (location.getLocationIndex()) + ".txt", false);
                String format = "%-10s %-10s %-10s %-10s %-10s %-18s %-18s %-14s %-24s %-10s %-10s %-27s";
                writer.write(header);
                writer.write(String.format(format, "Seed", "IndexLoc",
                        "#Success", "#Failure", "#Exception",
                        "#CallAllExecs", "AvgCallPerExec",
                        "#Perturbations", "AvgPerturbationPerExec",
                        "#Execs", "#CallRef", "%Success") + "\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void log() {
        List<PerturbationLocation> locations = super.manager.getLocations();
        for (PerturbationLocation location : locations) {
            Tuple[][][][] results = super.logger.getResults();
            try {
                FileWriter writer = new FileWriter("results/" + super.manager.getName() + "/" + nameOfSpecificExploration + "_" + (location.getLocationIndex()) + ".txt", true);
                String format = "%-15s %-10s %-10s %-10s %-10s %-18s %-18s %-14s %-24s %-10s %-10s %-27s";
                Tuple result = new Tuple(6);
                int accNbOfTasks = 0;
                int accNbExecAllTask = 0;
                for (int indexTask = 0; indexTask < super.manager.getIndexTask().size(); indexTask++) {
                    result = result.add(results[locations.indexOf(location)][indexTask][0][0]);
                    accNbOfTasks += super.nbCallReferencePerLocationPerTask[locations.indexOf(location)][indexTask];
                }

                double avgCall = (double) result.get(3) / (double) accNbOfTasks;
                double avgPerturbation = (double) result.get(4) / (double) accNbOfTasks;

                writer.write(String.format(format, currentSeed, location.getLocationIndex(),
                        result.get(0), result.get(1), result.get(2),
                        result.get(3), String.format("%.2f", avgCall),
                        result.get(4), String.format("%.2f", avgPerturbation),
                        accNbExecAllTask, result.get(5),
                        result.get(4) == 0 ? "NaN" : Util.getStringPerc(result.get(0), result.total(3))) + "\n");

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void mergeFile(Manager manager) {
        try {
            FileWriter writer = new FileWriter("results/" + manager.getName() + "/" + nameOfSpecificExploration + ".txt", false);
            String format = "%-15s %-10s %-10s %-10s %-10s %-18s %-18s %-14s %-24s %-10s %-10s %-27s";
            writer.write(header);
            writer.write(String.format(format, "#Seed", "IndexLoc",
                    "#Success", "#Failure", "#Exception",
                    "#CallAllExecs", "AvgCallPerExec",
                    "#Perturbations", "AvgPerturbationPerExec",
                    "#Execs", "#CallRef", "%Success") + "\n");

            List<PerturbationLocation> locations = manager.getLocations();

            for (PerturbationLocation location : locations) {
                BufferedReader buffer = new BufferedReader(new FileReader("results/" + manager.getName() + "/" + nameOfSpecificExploration + "_" + (location.getLocationIndex()) + ".txt"));
                for (int i = 0; i < 9; i++) buffer.readLine();
                String line;
                while ((line = buffer.readLine()) != null)
                    writer.write(line + "\n");
                buffer.close();
                Files.delete(Paths.get("results/" + manager.getName() + "/" + nameOfSpecificExploration + "_" + (location.getLocationIndex()) + ".txt"));
            }
            writer.close();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }


    public static void run(Class<?> classManager) {
        System.out.println("Explore the impact of seed(" + numberOfSeed + ") ...");

        seeds = new int[numberOfSeed];
        Random rnd = new Random(23);
        Manager manager = null;

        for (int i = 0; i < seeds.length; i++)
            seeds[i] = rnd.nextInt();

        for (int i = 0; i < seeds.length; i++) {
            int seed = seeds[i];
            System.out.println("currentSeed : \t" + seed + "\t" + Util.getStringPerc(i, seeds.length));
            try {
                manager = (Manager) classManager.getConstructor(int.class).newInstance(seed);
                manager.initialize(20,100);
                new SeedExplorer(manager ,i == 0, seed).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mergeFile(manager);
    }

}
