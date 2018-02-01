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
public class TaskNumberExplorer extends CallExplorer {

    public static int[] numberOfTask = new int[]{1, 2, 5, 10, 25};//, 50, 100};

    private String header;

    private String nameOfSpecificExploration;

    public TaskNumberExplorer(Manager manager, boolean init, int[] arrayOfTask) {
        super(manager, new IntegerExplorationPlusOne());
        this.header = "Contains data to explore the number of task required for saturation\n";
        this.header += "Task Number Exploration (TNE)\n";
        this.header += "Task Number :";
        for (int nTask : arrayOfTask)
            this.header += nTask + " ";
        this.header += "\n" + super.manager.getLocations().size() + " perturbation point\n";
        this.header += "N Execution Enactor\n";
        this.header += "PONE : Numerical Perturbator\n";
        this.header += "Size of each task : " + super.manager.getSizeOfTask() + "\n";
        this.header += "Seed used for generate task is " + ((ManagerImpl) manager).seedForGenTask + "\n";

        this.nameOfSpecificExploration = "NumberTaskExplorer";
        PerturbationEngine.loggers.put(this.nameOfSpecificExploration, new LoggerImpl());

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
                writer.write(String.format(format, "#Tasks", "IndexLoc",
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
                    accNbOfTasks += nbCallReferencePerLocationPerTask[locations.indexOf(location)][indexTask];
                }

                double avgCall = (double) result.get(3) / (double) accNbOfTasks;
                double avgPerturbation = (double) result.get(4) / (double) accNbOfTasks;

                writer.write(String.format(format, super.manager.getIndexTask().size(), location.getLocationIndex(),
                        result.get(0), result.get(1), result.get(2),
                        result.get(3), String.format("%.2f", avgCall),
                        result.get(4), String.format("%.2f", avgPerturbation),
                        result.get(5), accNbOfTasks,
                        result.get(4) == 0 ? "NaN" : Util.getStringPerc(result.get(0), result.total(3))) + "\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static void mergeFile(Manager manager, int[] arrayOfTask) {
        String header = "Contains data to explore the number of task required for saturation\n";
        header += "Task Number Exploration (TNE)\n";
        header += "Number Task : ";
        for (int nTask : arrayOfTask)
            header += nTask + " ";
        header += "\n" + manager.getLocations().size() + " perturbation point\n";
        header += "N Execution Enactor\n";
        header += "PONE : Numerical Perturbator\n";
        header += "Each task is an arrays of " + manager.getSizeOfTask() + "\n";
        header += "Seed used for generate task is " + ((ManagerImpl)manager).seedForGenTask + "\n";

        try {
            FileWriter writer = new FileWriter("results/" + manager.getName() + "/NumberTaskExplorer.txt", false);
            String format = "%-10s %-10s %-10s %-10s %-10s %-18s %-18s %-14s %-24s %-10s %-10s %-27s";
            writer.write(header);
            writer.write(String.format(format, "#Tasks", "IndexLoc",
                    "#Success", "#Failure", "#Exception",
                    "#CallAllExecs", "AvgCallPerExec",
                    "#Perturbations", "AvgPerturbationPerExec",
                    "#Execs", "#CallRef", "%Success") + "\n");

            List<PerturbationLocation> locations = manager.getLocations();

            for (PerturbationLocation location : locations) {
                BufferedReader buffer = new BufferedReader(new FileReader("results/" + manager.getName() + "/NumberTaskExplorer_" + (location.getLocationIndex()) + ".txt"));
                for (int i = 0 ; i < 9 ; i++) buffer.readLine();
                String line;
                while ((line = buffer.readLine()) != null)
                    writer.write(line + "\n");
                buffer.close();
                Files.delete(Paths.get("results/" + manager.getName() + "/NumberTaskExplorer_" + (location.getLocationIndex()) + ".txt"));
            }
            writer.close();
        } catch (IOException e){}
    }

    public static void run(Class<?> classManager) {
        System.out.println("Explore the saturation by the number of tasks...");
        Manager manager = null;
        for (int i = 0 ; i < numberOfTask.length ; i++) {
            int nbTask = numberOfTask[i];
            System.out.println("Number of task : \t" + nbTask + "\t" +Util.getStringPerc(i, numberOfTask.length));
            try {
                manager = (Manager) classManager.getConstructor(int.class, int.class).newInstance(nbTask, 50);
                new TaskNumberExplorer(manager, i == 0, numberOfTask).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mergeFile(manager, numberOfTask);
    }

}
