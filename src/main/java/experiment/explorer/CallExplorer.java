package experiment.explorer;

import experiment.*;
import experiment.exploration.Exploration;
import perturbation.PerturbationEngine;
import perturbation.enactor.NCallEnactorImpl;
import perturbation.location.PerturbationLocation;
import perturbation.log.LoggerImpl;
import perturbation.perturbator.Perturbator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by spirals on 30/04/16.
 */
public class CallExplorer extends ExplorerImpl {

    protected long[][] nbCallReferencePerLocationPerTask;

    public CallExplorer(Manager manager, Exploration exploration) {
        super(manager, exploration, "CallExplorer");
        nbCallReferencePerLocationPerTask = new long[super.manager.getLocations().size()][super.manager.getIndexTask().size()];
        this.initLogger();
    }

    @Override
    public void initLogger() {
        //Logger contains : Success Failure Exception Call Perturbation NumberOfExecution
        super.logger = new Logger(super.manager, super.manager.getLocations().size(), super.manager.getIndexTask().size(), perturbators.size());
        PerturbationEngine.loggers.put(name, new LoggerImpl());
    }

    @Override
    public int runReference(int indexOfTask, PerturbationLocation location) {
        //reference run : no perturbation
        PerturbationEngine.loggers.get(super.name).logOn(location);
        this.run(indexOfTask);
        int currentNbCall = PerturbationEngine.loggers.get(super.name).getCalls(location);
        this.nbCallReferencePerLocationPerTask[super.manager.getLocations().indexOf(location)][indexOfTask] = currentNbCall;
        PerturbationEngine.loggers.get(super.name).reset();
        return currentNbCall;
    }

    @Override
    public void runOnePerturbator(int indexOfTask, PerturbationLocation location, Perturbator perturbator) {
        long currentNbCall = nbCallReferencePerLocationPerTask[super.manager.getLocations().indexOf(location)][indexOfTask];
        location.setPerturbator(perturbator);
        for (int indexOfCall = 0; indexOfCall < currentNbCall; indexOfCall++) {
            PerturbationEngine.loggers.get(super.name).logOn(location);
            RunResult result = runAtTheIndexOfCall(indexOfCall, indexOfTask, location);
            super.logger.log(super.manager.getLocations().indexOf(location), indexOfTask, super.perturbators.indexOf(perturbator), 0, result, super.name);
            PerturbationEngine.loggers.get(super.name).reset();
        }
    }

    private RunResult runAtTheIndexOfCall(int indexOfCall, int indexOfTask, PerturbationLocation location) {
        location.setEnactor(new NCallEnactorImpl(indexOfCall));
        return this.run(indexOfTask);
    }

    public long[][] getNbCallReferencePerLocationPerTask() {
        return this.nbCallReferencePerLocationPerTask;
    }

    @Override
    public void log() {
        Tuple[][][][] results = super.logger.getResults();

        List<PerturbationLocation> locationExceptionFragile = new ArrayList<>();
        List<PerturbationLocation> locationOracleFragile = new ArrayList<>();
        List<PerturbationLocation> locationAntiFragile = new ArrayList<>();
        List<PerturbationLocation> locationSuperAntiFragile = new ArrayList<>();

        List<PerturbationLocation> locations = super.manager.getLocations();

        int numberOfPerturbor = exploration.getPerturbators().size();
        String[] perturbatorsName = exploration.getPerturbatorsName();
        String campaignName = exploration.getName();
        String dir = "results/" + super.manager.getName() + "/" ;
        new File(dir).mkdirs();
        String pathToOutPutFile = dir +
                campaignName + "_" + this.name;

        try {
            FileWriter writer = new FileWriter(pathToOutPutFile + "_detail.txt", false);
            String format = "%-4s %-" + (exploration.getColumnName().length() + 1) + "s %-10s %-10s %-10s %-10s %-10s %-14s %-27s";
            writer.write("detail per task and per " + exploration.getColumnName() + ".\n" + exploration.getHeader() + super.manager.getHeader());
            writer.write(String.format(format,
                    "Task", exploration.getColumnName(), "IndexLoc",
                    "#Success", "#Failure", "#Exception",
                    "#Call", "#Perturbations",
                    "%Success") + "\n");

            for (int indexTask = 0; indexTask < super.manager.getIndexTask().size(); indexTask++) {
                for (PerturbationLocation location : locations) {
                    for (int indexPerturbator = 0; indexPerturbator < numberOfPerturbor; indexPerturbator++) {
                        Tuple result = results[super.manager.getLocations().indexOf(location)][indexTask][indexPerturbator][0];
                        logger.searchSpaceSizePerMagnitude[indexPerturbator] += result.get(5);
                        logger.numberOfSuccessPerMagnitude[indexPerturbator] += result.get(0);
                        writer.write(String.format(format,
                                indexTask, perturbatorsName[indexPerturbator], location.getLocationIndex(),
                                result.get(0), result.get(1), result.get(2),
                                result.get(3), result.get(4),
                                result.get(4) == 0 ? "NaN" : Util.getStringPerc(result.get(0), result.total(3))) + "\n");
                    }

                }
            }
            writer.close();

            /* Sum Arrays */
            writer = new FileWriter(pathToOutPutFile + "_analysis_graph_data.txt", false);
            writer.write("contains the data for build a graph for analysis.\n" +
                    exploration.getHeader() + super.manager.getHeader());
            format = "%-11s %-10s %-14s %-10s %-10s %-10s %-14s %-27s";
            writer.write(String.format(format, exploration.getColumnName(), "IndexLoc", "#Perturbations",
                    "#Success", "#Failure", "#Exception",
                    "#Call",
                    "%Success") + "\n");
            for (PerturbationLocation location : locations) {
                Tuple resultForLocation = new Tuple(6);
                for (int indexPerturbator = 0; indexPerturbator < numberOfPerturbor; indexPerturbator++) {
                    Tuple result = new Tuple(6);
                    for (int indexTask = 0; indexTask < super.manager.getIndexTask().size(); indexTask++) {
                        result = result.add(results[super.manager.getLocations().indexOf(location)][indexTask][indexPerturbator][0]);
                    }
                    writer.write(String.format(format, perturbatorsName[indexPerturbator], location.getLocationIndex(), result.get(4),
                            result.get(0), result.get(1), result.get(2),
                            result.get(3),
                            result.get(4) == 0 ? "NaN" : Util.getStringPerc(result.get(0), result.total(3))) + "\n");

                    resultForLocation = resultForLocation.add(result);
                }
                Logger.addToFragilityList(resultForLocation, resultForLocation.total(), location, locationExceptionFragile, locationSuperAntiFragile,
                        locationAntiFragile, locationOracleFragile);
            }
            writer.close();

            format = "%-30s %-30s";
            for (int indexPerturbator = 0; indexPerturbator < numberOfPerturbor; indexPerturbator++) {
                writer = new FileWriter(pathToOutPutFile + "_search_space_size_" + perturbatorsName[indexPerturbator] + ".txt", false);
                StringWriter sb = new StringWriter();
                sb.write("detail of space for " + this.name + " with " + exploration.getColumnName() + " = " + perturbatorsName[indexPerturbator] + "\n");
                sb.write(String.format(format, "number of Task : ", super.manager.getIndexTask().size()) + "\n");
                sb.write(String.format(format, "number of Locations : ", super.manager.getLocations().size()) + "\n");
                sb.write(String.format(format, "number of executions done : ", logger.searchSpaceSizePerMagnitude[indexPerturbator]) + "\n");
                sb.write(String.format(format, "number of successful executions done : ", logger.numberOfSuccessPerMagnitude[indexPerturbator]) + "\n");
                sb.write(String.format(format, "% Success : ", Util.getStringPerc(logger.numberOfSuccessPerMagnitude[indexPerturbator], logger.searchSpaceSizePerMagnitude[indexPerturbator])) + "\n");
                // writing to console and to file
                System.out.println(sb.toString());
                writer.write(sb.toString());
                System.out.println("see also files in "+dir);
                writer.close();
            }

            /* Sum PerturbationPoint */
            format = "%-10s %-10s %-10s %-10s %-18s %-18s %-14s %-24s %-10s %-10s %-17s %-10s %-27s";
            for (int indexPerturbator = 0; indexPerturbator < numberOfPerturbor; indexPerturbator++) {
                writer = new FileWriter(pathToOutPutFile + "_per_location_" + perturbatorsName[indexPerturbator] + ".txt", false);
                writer.write("aggregate data per location for " + exploration.getColumnName() + " = " + perturbatorsName[indexPerturbator] + "\n");
                writer.write(exploration.getHeader() + super.manager.getHeader());

                writer.write(String.format(format, "IndexLoc",
                        "#Success", "#Failure", "#Exception",
                        "#CallAllExecs", "AvgCallPerExec",
                        "#Perturbations", "AvgPerturbationPerExec",
                        "#Execs", "#ExecsRef", "AvgCallRefPerTask",
                        "#Tasks", "%Success") + "\n");

                for (PerturbationLocation location : locations) {
                    Tuple result = new Tuple(6);
                    int accExecsRef = 0;
                    for (int indexTask = 0; indexTask < super.manager.getIndexTask().size(); indexTask++) {
                        result = result.add(results[super.manager.getLocations().indexOf(location)][indexTask][indexPerturbator][0]);
                        accExecsRef += nbCallReferencePerLocationPerTask[super.manager.getLocations().indexOf(location)][indexTask];
                    }
                    double avgCall = (double) result.get(3) / (double) result.get(5);
                    double avgPerturbation = (double) result.get(4) / (double) result.get(5);
                    double avgCallRefPerTask = (double) accExecsRef / (super.manager.getIndexTask().size());

                    writer.write(String.format(format, location.getLocationIndex(),
                            result.get(0), result.get(1), result.get(2),
                            result.get(3), String.format("%.2f", avgCall),
                            result.get(4), String.format("%.2f", avgPerturbation),
                            result.get(5), accExecsRef, avgCallRefPerTask,
                            super.manager.getIndexTask().size(),
                            result.get(4) == 0 ? "NaN" : Util.getStringPerc(result.get(0), result.total(3))) + "\n");
                }
                writer.close();
            }


            //Log Reference
            writer = new FileWriter(pathToOutPutFile + "_nbCallRef.txt", false);
            format = "%-10s %-10s %-10s";
            writer.write("Number of Execution of the reference run per location and per task \n" +
                    "With the aggregation by task for each location, and the total of the execution ref\n");
            writer.write(String.format(format, "IndexLoc",
                    "IndexTask",
                    "#ExecRef") + "\n");
            int accTotal = 0;
            for (PerturbationLocation location : locations) {
                int acc = 0;
                for (int indexTask = 0; indexTask < super.manager.getIndexTask().size(); indexTask++) {
                    acc += nbCallReferencePerLocationPerTask[super.manager.getLocations().indexOf(location)][indexTask];
                    writer.write(String.format(format, location.getLocationIndex(),
                            indexTask,
                            nbCallReferencePerLocationPerTask[super.manager.getLocations().indexOf(location)][indexTask]) + "\n");
                }
                writer.write(String.format(format, location.getLocationIndex(),
                        "TotalExcRef",
                        acc) + "\n");
                accTotal += acc;
            }
            writer.write("Total execution ref: " + accTotal + "\n");
            writer.close();

            Logger.writeListOnGivenFile(pathToOutPutFile + "_anti_fragile.txt",
                    "List of ids antifragile points.", locationAntiFragile);
            Logger.writeListOnGivenFile(pathToOutPutFile + "_super_anti_fragile.txt",
                    "List of ids antifragile points.", locationSuperAntiFragile);
            Logger.writeListOnGivenFile(pathToOutPutFile + "_oracle_fragile.txt",
                    "list ids of oracle fragile code : >" + Logger.TOLERANCE + "% of oracle failures", locationOracleFragile);
            Logger.writeListOnGivenFile(pathToOutPutFile + "_exception_fragile.txt",
                    "list ids of exception fragile code : >" + Logger.TOLERANCE + "% of exceptions.", locationExceptionFragile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
