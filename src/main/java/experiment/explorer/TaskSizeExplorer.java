package experiment.explorer;

import experiment.*;
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

/**
 * Created by bdanglot on 02/05/16.
 */
public class TaskSizeExplorer extends CallExplorer {

    public static int[] sizeOfTask = new int[]{5, 10, 20, 50, 100, 225};

    private String header;

    private String nameOfSpecificExploration;

    public TaskSizeExplorer(Manager manager, boolean init, int[] arrayOfTask) {
        super(manager, new IntegerExplorationPlusOne());
        //Logger contains : Success Failure Exception Call Perturbation NumberOfExecution
        super.logger = new Logger(super.manager, super.manager.getLocations().size(), super.manager.getIndexTask().size(), super.perturbators.size());//@TODO Check this
        PerturbationEngine.loggers.put(super.name, new LoggerImpl());

        header = "Contains data to explore the size of task required for saturation\n";
        header += "Task Size Exploration (TSE)\n";
        header += "Task Size : ";
        for (int size : arrayOfTask)
            header += size + " ";
        header += "\n" + super.manager.getLocations().size() + " perturbation point\n";
        header += "N Execution Enactor\n";
        header += "PONE : Numerical Perturbator\n";
        header += "One task of different size\n";
        header += "Seed used for generate task is " + ((ManagerImpl) manager).seedForGenTask + "\n";

        PerturbationEngine.loggers.remove(super.name);
        nameOfSpecificExploration = "SizeTaskExploration";
        PerturbationEngine.loggers.put(super.name, new LoggerImpl());

        if (init)
            init();
    }

    private void init() {
        List<PerturbationLocation> locations = super.manager.getLocations();
        for (PerturbationLocation location : locations) {
            try {
                FileWriter writer = new FileWriter("results/" + super.manager.getName() + "/" + nameOfSpecificExploration + "_" + (location.getLocationIndex()) + ".txt", false);
                String format = "%-10s %-10s %-10s %-10s %-10s %-18s %-18s %-14s %-24s %-10s %-10s %-27s";
                writer.write(header);
                writer.write(String.format(format, "Size", "IndexLoc",
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
        Tuple[][][][] results = super.logger.getResults();
        List<PerturbationLocation> locations = super.manager.getLocations();
        for (PerturbationLocation location : locations) {
            try {
                FileWriter writer = new FileWriter("results/" + super.manager.getName() + "/" + nameOfSpecificExploration + "_" + (location.getLocationIndex()) + ".txt", true);
                String format = "%-10s %-10s %-10s %-10s %-10s %-18s %-18s %-14s %-24s %-10s %-10s %-27s";
                Tuple result = new Tuple(6);
                int accNbOfTasks = 0;
                for (int indexTask = 0; indexTask < super.manager.getIndexTask().size(); indexTask++) {
                    result = result.add(results[locations.indexOf(location)][indexTask][0][0]);
                    accNbOfTasks += super.nbCallReferencePerLocationPerTask[locations.indexOf(location)][indexTask];
                }

                double avgCall = (double) result.get(3) / (double) accNbOfTasks;
                double avgPerturbation = (double) result.get(4) / (double) accNbOfTasks;

                writer.write(String.format(format, super.manager.getSizeOfTask(), location.getLocationIndex(),
                        result.get(0), result.get(1), result.get(2),
                        result.get(3), String.format("%.2f", avgCall),
                        result.get(4), String.format("%.2f", avgPerturbation),
                        accNbOfTasks, result.get(5),
                        result.get(4) == 0 ? "NaN" : Util.getStringPerc(result.get(0), result.total(3))) + "\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void mergeFile(Manager manager, int[] sizeOfTask) {
        List<PerturbationLocation> locations = manager.getLocations();
        String header = "Contains data to explore the size of task required for saturation\n";
        header += "Size Task Exploration (STE)\n";
        header += "Size Task : ";
        for (int size : sizeOfTask)
            header += size + " ";
        header += "\n" + locations.size() + " perturbation point\n";
        header += "N Execution Enactor\n";
        header += "PONE : Numerical Perturbator\n";
        header += "One task of different size\n";
        header += "Seed used for generate task is " + ((ManagerImpl) manager).seedForGenTask + "\n";

        String name = "SizeTaskExploration";

        try {
            FileWriter writer = new FileWriter("results/" + manager.getName() + "/" + name + ".txt", false);
            String format = "%-10s %-10s %-10s %-10s %-10s %-18s %-18s %-14s %-24s %-10s %-10s %-27s";
            writer.write(header);
            writer.write(String.format(format, "#Tasks", "IndexLoc",
                    "#Success", "#Failure", "#Exception",
                    "#CallAllExecs", "AvgCallPerExec",
                    "#Perturbations", "AvgPerturbationPerExec",
                    "#Execs", "#CallRef", "%Success") + "\n");

            for (PerturbationLocation location : locations) {
                BufferedReader buffer = new BufferedReader(new FileReader("results/" + manager.getName() + "/" + name + "_" + (location.getLocationIndex()) + ".txt"));
                for (int i = 0; i < 9; i++) buffer.readLine();
                String line;
                while ((line = buffer.readLine()) != null)
                    writer.write(line + "\n");
                buffer.close();
                Files.delete(Paths.get("results/" + manager.getName() + "/" + name + "_" + (location.getLocationIndex()) + ".txt"));
            }
            writer.close();
        } catch (IOException e) {
        }
    }

    public static void run(Class<?> classManager) {
        Manager manager = null;
        System.out.println("Explore the saturation by the size of tasks...");
        for (int i = 0; i < sizeOfTask.length; i++) {
            int size = sizeOfTask[i];
            System.out.println("Size of task : \t" + size + "\t" + Util.getStringPerc(i, sizeOfTask.length));
            try {
                manager = (Manager) classManager.getConstructor(int.class, int.class).newInstance(20, size);
                new TaskSizeExplorer(manager, i == 0, sizeOfTask).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mergeFile(manager, sizeOfTask);
    }
}
