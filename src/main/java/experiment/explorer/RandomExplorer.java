package experiment.explorer;

import experiment.Logger;
import experiment.Manager;
import experiment.Tuple;
import experiment.Util;
import experiment.exploration.Exploration;
import perturbation.PerturbationEngine;
import perturbation.enactor.Enactor;
import perturbation.enactor.RandomEnactorImpl;
import perturbation.location.PerturbationLocation;
import perturbation.log.LoggerImpl;
import perturbation.perturbator.Perturbator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by beyni on 30/04/16.
 */
public class RandomExplorer extends ExplorerImpl {

    protected float[] randomRates;

    private int seedOfRandomEnactor = 32;

    private Enactor[][] enactorsOfLocationPerRandomRates;

    private int numberOfRepeat;

    public RandomExplorer(Manager manager, Exploration exploration) {
        this(manager,exploration, 5, 0.001f, 0.005f, 0.01f, 0.05f, 0.1f, 0.15f, 0.2f, 0.25f, 0.3f);
    }

    public RandomExplorer(Manager manager,  Exploration exploration, float... randomRates) {
        this(manager, exploration, 5, randomRates);
    }

    public RandomExplorer(Manager manager, Exploration exploration, int numberOfRepeat) {
        this(manager, exploration, numberOfRepeat,   0.001f, 0.005f, 0.01f, 0.05f, 0.1f, 0.15f, 0.2f, 0.25f, 0.3f);
    }

    public RandomExplorer(Manager manager, Exploration exploration, int repeat, float... randomRates) {
        super(manager, exploration, "RandomExplorer");
        if (randomRates.length >= 1)
            this.randomRates = randomRates;
        else
            this.randomRates = new float[]{ 0.001f, 0.005f, 0.01f, 0.05f, 0.1f, 0.15f, 0.2f, 0.25f, 0.3f};

        this.numberOfRepeat = repeat;

        this.enactorsOfLocationPerRandomRates = new Enactor[super.manager.getLocations().size()][this.randomRates.length];

        for (int indexLocation = 0; indexLocation < super.manager.getLocations().size(); indexLocation++) {
            for (int indexRandom = 0; indexRandom < this.randomRates.length; indexRandom++) {
                this.enactorsOfLocationPerRandomRates[indexLocation][indexRandom] =
                        new RandomEnactorImpl(seedOfRandomEnactor, this.randomRates[indexRandom]);
            }
        }

        PerturbationEngine.loggers.put(super.name, new LoggerImpl());
    }

    @Override
    public int runReference(int indexOfTask, PerturbationLocation location) {
        PerturbationEngine.loggers.get(super.name).logOn(location);
        Tuple result = super.run(indexOfTask);
        super.logger.log(super.manager.getLocations().indexOf(location), super.manager.getIndexTask().indexOf(indexOfTask), 0, 0, result, super.name);
        PerturbationEngine.loggers.get(super.name).reset();
        return PerturbationEngine.loggers.get(name).getCalls(location);
    }

    @Override
    public void initLogger() {
        //Logger contains : Success Failure Exception Call Perturbation NumberOfExecution
        super.logger = new Logger(super.manager, super.manager.getLocations().size(), super.manager.getIndexTask().size(), perturbators.size(), this.randomRates.length + 1);
        PerturbationEngine.loggers.put(name, new LoggerImpl());
    }

    @Override
    public void runOnePerturbator(int indexOfTask, PerturbationLocation location, Perturbator perturbator) {
        location.setPerturbator(perturbator);
        for (int indexRandomRate = 0; indexRandomRate < this.randomRates.length; indexRandomRate++)
            runOneRandomRate(indexOfTask, location, indexRandomRate, super.perturbators.indexOf(perturbator));
    }

    private void runOneRandomRate(int indexOfTask, PerturbationLocation location, int indexRandomRate, int perturbator) {
        int indexLocation = super.manager.getLocations().indexOf(location);
        location.setEnactor(enactorsOfLocationPerRandomRates[indexLocation][indexRandomRate]);
        for (int i = 0; i < numberOfRepeat; i++) {
            PerturbationEngine.loggers.get(super.name).logOn(location);
            Tuple result = super.run(indexOfTask);
            super.logger.log(super.manager.getLocations().indexOf(location),
                    super.manager.getIndexTask().indexOf(indexOfTask), perturbator, indexRandomRate + 1, result, super.name);
            PerturbationEngine.loggers.get(super.name).reset();
        }
    }

    @Override
    public void log() {

        List<PerturbationLocation> locationExceptionFragile = new ArrayList<>();
        List<PerturbationLocation> locationOracleFragile = new ArrayList<>();
        List<PerturbationLocation> locationAntiFragile = new ArrayList<>();
        List<PerturbationLocation> locationSuperAntiFragile = new ArrayList<>();

        Tuple[][][][] results = super.logger.getResults();

        int numberOfPerturbor = exploration.getPerturbators().size();
        String[] perturbatorsName = exploration.getPerturbatorsName();
        String campaignName = exploration.getName();

        String pathToOutPutFile = "results/" + super.manager.getName() + "/" +
                campaignName + "_" + this.name;

        String header = "random rate : ";
        for (float rate : this.randomRates)
            header += rate + " ";
        header += "\n";

        header += numberOfRepeat + " repetition\n";

        List<PerturbationLocation> locations = super.manager.getLocations();

        try {
            /* All Log */
            FileWriter writer = new FileWriter(pathToOutPutFile + "_detail.txt", false);
            String format = "%-4s %-10s %-"+ exploration.getColumnName().length() +"s %-10s %-10s %-10s %-10s %-10s %-14s %-22s %-27s";
            writer.write("detail per task and per random rate and per " + exploration.getColumnName() + ".\n" + exploration.getHeader() + header + super.manager.getHeader());
            writer.write(String.format(format,
                    "Task", "RandomRate",
                    exploration.getColumnName(), "IndexLoc",
                    "#Success", "#Failure", "#Exception",
                    "#Call", "#Perturbation", "AvgPerturbationPerCall",
                    "%Success") + "\n");
            for (int indexTask = 0; indexTask <super.manager.getIndexTask().size(); indexTask++) {
                for (PerturbationLocation location : locations) {
                    Tuple result = results[super.manager.getLocations().indexOf(location)][indexTask][0][0];
                    double avg = (double) result.get(4) / (double) result.get(3);
                    writer.write(String.format(format,
                            indexTask, 0.0,
                            "None", location.getLocationIndex(),
                            result.get(0), result.get(1), result.get(2),
                            result.get(3), result.get(4), avg,
                            Util.getStringPerc(result.get(0), result.total(3))) + "\n");
                    for (int indexRandomRates = 0; indexRandomRates < randomRates.length; indexRandomRates++) {
                        for (int indexPerturbator = 0; indexPerturbator < numberOfPerturbor; indexPerturbator++) {
                            result = results[super.manager.getLocations().indexOf(location)][indexTask][indexPerturbator][indexRandomRates + 1];
                            avg = (double) result.get(4) / (double) result.get(3);
                            writer.write(String.format(format,
                                    indexTask, randomRates[indexRandomRates],
                                    perturbatorsName[indexPerturbator], location.getLocationIndex(),
                                    result.get(0), result.get(1), result.get(2),
                                    result.get(3), result.get(4), avg,
                                    result.get(4) == 0 ? "NaN" : Util.getStringPerc(result.get(0), result.total(3))) + "\n");
                        }
                    }
                }
            }
            writer.close();

             /* Sum Arrays */
            writer = new FileWriter(pathToOutPutFile + "_analysis_graph_data.txt", false);
            writer.write("contains the data for the random rates analysis graph with " + exploration.getColumnName() + " as perturbator.\n");
            writer.write(exploration.getHeader() + header + super.manager.getHeader());
            format = "%-10s %-" + exploration.getColumnName().length() + "s %-10s %-10s %-10s %-10s %-10s %-14s %-27s";
            writer.write(String.format(format,
                    "RandomRate", exploration.getColumnName(), "IndexLoc",
                    "#Success", "#Failure", "#Exception",
                    "#Call", "#Perturbation",
                    "%Success") + "\n");

            for (PerturbationLocation location : locations) {
                Tuple resultForLocation = new Tuple(3);
                for (int indexRandomRates = 0; indexRandomRates < randomRates.length + 1; indexRandomRates++) {
                    Tuple result = new Tuple(5);
                    for (int indexPerturbator = 0; indexPerturbator < numberOfPerturbor; indexPerturbator++) {
                        for (int indexTask = 0; indexTask <super.manager.getIndexTask().size(); indexTask++)
                            result = result.add(results[super.manager.getLocations().indexOf(location)][indexTask][indexPerturbator][indexRandomRates]);

                        String random;
                        String perturbator;
                        String success;
                        if (indexRandomRates == 0) {
                            random = "0.0";
                            success = Util.getStringPerc(result.get(0), result.total(3));
                            perturbator = "None";
                        } else {
                            random =  "" +randomRates[indexRandomRates - 1];
                            success = result.get(4) == 0 ? "NaN" : Util.getStringPerc(result.get(0), result.total(3));
                            perturbator = perturbatorsName[indexPerturbator];
                        }

                        writer.write(String.format(format,
                                random, perturbator, location.getLocationIndex(),
                                result.get(0), result.get(1), result.get(2),
                                result.get(3), result.get(4), success
                                ) + "\n");

                        resultForLocation = resultForLocation.add(result);
                    }
                }
                Logger.addToFragilityList(resultForLocation, resultForLocation.total(), location, locationExceptionFragile, locationSuperAntiFragile,
                        locationAntiFragile, locationOracleFragile);
            }
            writer.close();

            format = "%-10s %-10s %-10s %-10s %-18s %-18s %-14s %-24s %-10s %-10s %-27s";
            for (int indexPerturbator = 0; indexPerturbator < numberOfPerturbor; indexPerturbator++) {
                for (int indexRandomRates = 0; indexRandomRates < randomRates.length; indexRandomRates++) {
                /* Sum PerturbationPoint */
                    writer = new FileWriter(pathToOutPutFile + "_per_location_" + perturbatorsName[indexPerturbator] + "_" + randomRates[indexRandomRates] + "_" + ".txt", false);
                    writer.write("aggregate data per location for random rate = " + randomRates[indexRandomRates] + " and with " + exploration.getColumnName() + " = " + perturbatorsName[indexPerturbator] + "\n");
                    writer.write(exploration.getHeader() + super.manager.getHeader());
                    writer.write(String.format(format,
                            "IndexLoc", "#Success", "#Failure", "#Exception",
                            "#CallAllExecs", "AvgCallPerExec",
                            "#Perturbations", "AvgPerturbationPerExec",
                            "#Execs", "#Tasks", "%Success") + "\n");

                    for (PerturbationLocation location : locations) {
                        Tuple result = new Tuple(5);
                        int accNbOfTasks = 0;
                        for (int indexTask = 0; indexTask <super.manager.getIndexTask().size(); indexTask++) {
                            result = result.add(results[super.manager.getLocations().indexOf(location)][indexTask][indexPerturbator][indexRandomRates + 1]);
                            accNbOfTasks += results[super.manager.getLocations().indexOf(location)][indexTask][indexPerturbator][indexRandomRates].get(5);
                        }

                        double avgCalls = ((double) result.get(3) / (double) accNbOfTasks);
                        double avgPerturbation = (double) result.get(4) / (double) accNbOfTasks;

                        writer.write(String.format(format,
                                location.getLocationIndex(), result.get(0), result.get(1), result.get(2), //results
                                result.get(3), String.format("%.2f", avgCalls), // Calls
                                result.get(4), String.format("%.2f", avgPerturbation), //Perturbations
                                accNbOfTasks,super.manager.getIndexTask().size(),//Execs Task
                                result.get(4) == 0 ? "NaN" : Util.getStringPerc(result.get(0), result.total(3))) + "\n");//Percentage success
                    }
                    writer.close();
                }
            }

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
