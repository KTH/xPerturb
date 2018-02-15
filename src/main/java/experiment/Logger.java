package experiment;

import experiment.explorer.AggregatedResult;
import experiment.explorer.RunResult;
import perturbation.PerturbationEngine;
import perturbation.location.PerturbationLocation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by spirals on 13/04/16.
 */
public class Logger {

    /**
     * Tuple results with 4 dimensions Location Task Perturbator Enactor
     */
    private AggregatedResult[][][][] results;

    final public int[] searchSpaceSizePerMagnitude;
    final public int[] numberOfSuccessPerMagnitude;


    private Manager<?, ?> manager;

    /**
     * Init logger with Tuple 6 with the given numbers and 1 enactor.
     * @param numberOfLocations
     * @param numberOfTask
     * @param numberOfPerturbator
     */
    public Logger(Manager manager, int numberOfLocations, int numberOfTask, int numberOfPerturbator) {
        this(manager, numberOfLocations, numberOfTask, numberOfPerturbator, 1);
    }

    /**
     * Init logger with Tuple 6 with the given numbers.
     * @param numberOfLocations
     * @param numberOfTask
     * @param numberOfPerturbator
     * @param numberOfEnactor
     */
    public Logger(Manager manager, int numberOfLocations, int numberOfTask, int numberOfPerturbator, int numberOfEnactor) {
        this.manager = manager;
        this.results = new AggregatedResult[numberOfLocations][numberOfTask][numberOfPerturbator][numberOfEnactor];
        for (int indexLocation = 0 ; indexLocation < numberOfLocations ; indexLocation ++) {
            for (int indexTask = 0 ; indexTask < numberOfTask ; indexTask++) {
                for (int indexPerturbator = 0 ; indexPerturbator < numberOfPerturbator ; indexPerturbator++) {
                    for (int indexEnactor = 0 ; indexEnactor < numberOfEnactor ; indexEnactor++)
                        this.results[indexLocation][indexTask][indexPerturbator][indexEnactor] = new AggregatedResult();
                }
            }
        }
        searchSpaceSizePerMagnitude = new int[numberOfPerturbator];
        numberOfSuccessPerMagnitude = new int[numberOfPerturbator];
    }

    public int getNumberOfTasks() {
        return results[0].length;
    }

    public int getNumberOfLocations() {
        return results.length;
    }

    public int getNumberOfPerturbators() {
        return results[0][0].length;
    }

    public Tuple[][][][] getResults() {
        int numberOfLocations = getNumberOfLocations();
        int numberOfTask = getNumberOfTasks();
        int numberOfPerturbator = getNumberOfPerturbators();
        int numberOfEnactor = results[0][0][0].length;
        Tuple[][][][] tupleResults = new Tuple[results.length][numberOfLocations][numberOfTask][results[0][0][0].length];
        for (int indexLocation = 0 ; indexLocation < numberOfLocations ; indexLocation ++) {
            for (int indexTask = 0 ; indexTask < numberOfTask ; indexTask++) {
                for (int indexPerturbator = 0 ; indexPerturbator < numberOfPerturbator ; indexPerturbator++) {
                    for (int indexEnactor = 0 ; indexEnactor < numberOfEnactor ; indexEnactor++)
                        tupleResults[indexLocation][indexTask][indexPerturbator][indexEnactor] = results[indexLocation][indexTask][indexPerturbator][indexEnactor].toTuple();
                }
            }
        }
        return tupleResults;
    }

    /**
     * Method log for explorer : it has side effect ie it will add calls and enactions of the given location and
     * increment by one the 6th integer to count the number of time it has been called.
     * This Method assume Tuple of 6 integers.
     * @param indexLocation
     * @param indexTask
     * @param indexPerturbartor
     * @param indexEnactor
     * @param result
     * @param name
     */
    public void log(int indexLocation, int indexTask, int indexPerturbartor, int indexEnactor, RunResult result, String name) {

        result.nbCalls  = PerturbationEngine.loggers.get(name).getCalls(this.manager.getLocations().get(indexLocation));
        result.nbEnactions = PerturbationEngine.loggers.get(name).getEnactions(this.manager.getLocations().get(indexLocation));

        this.results[indexLocation][indexTask][indexPerturbartor][indexEnactor].add(result);
    }

    public static double TOLERANCE = 70.0f;

    public List<PerturbationLocation> getAntifragilePoints() {
        List<PerturbationLocation> l = new ArrayList<>();
        for (int indexLocation = 0 ; indexLocation < getNumberOfLocations(); indexLocation ++) {
            Tuple resultForLocation = new Tuple(6);
            for (int indexPerturbator = 0; indexPerturbator < getNumberOfPerturbators(); indexPerturbator++) {
                Tuple result = new Tuple(6);
                for (int indexTask = 0; indexTask < getNumberOfTasks(); indexTask++) {
                    result = result.add(results[indexLocation][indexTask][indexPerturbator][0].toTuple());
                }
                resultForLocation = resultForLocation.add(result);
            }
            if (resultForLocation.get(0) == resultForLocation.get(5) && resultForLocation.get(0) != 0)//Super - Antifragile
                l.add(this.manager.getLocations().get(indexLocation));
        }
        return l;
    }

    public static void addToFragilityList(Tuple result, long total, PerturbationLocation location,
                                   List<PerturbationLocation> locationExceptionFragile, List<PerturbationLocation> locationSuperAntiFragile,
                                   List<PerturbationLocation> locationAntiFragile , List<PerturbationLocation> locationOracleFragile) {
        if (result.get(0) == total && result.get(0) != 0)//Super - Antifragile
            locationSuperAntiFragile.add(location);
        else if (Util.perc(result.get(0), total) >= TOLERANCE)//AntiFragile
            locationAntiFragile.add(location);
        else if (Util.perc(result.get(1), total) >= TOLERANCE)//OracleFragile
            locationOracleFragile.add(location);
        else if (Util.perc(result.get(2), total) >= TOLERANCE)//ExceptionFragile
            locationExceptionFragile.add(location);
    }

    public static void writeListOnGivenFile(String pathToFile, String header, List<PerturbationLocation> locations) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(pathToFile , false);
            writer.write(header + "\n");
            for (PerturbationLocation location : locations)
                writer.write(location.getLocationIndex() + " ");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
