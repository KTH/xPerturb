package experiment.explorer;

import experiment.Manager;
import experiment.Tuple;
import experiment.Util;
import experiment.exploration.Exploration;
import perturbation.location.PerturbationLocation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by bdanglot on 03/05/16.
 */
public class HeatMapExplorer extends RandomExplorer {

    private static final float[] randomRates = new float[]{0.001f, 0.005f, 0.01f, 0.05f, 0.1f, 0.15f, 0.2f, 0.025f, 0.3f};

    public HeatMapExplorer(Manager manager, Exploration exploration) {
        super(manager,exploration, randomRates);
    }

    @Override
    public void log() {

        Tuple[][][][] results = super.logger.getResults();

        int numberOfPerturbor = exploration.getPerturbators().size();
        String[] perturbatorsName = exploration.getPerturbatorsName();

        String pathToOutPutFile = "results/" + super.manager.getName() + "/HeatMap.txt";

        String randomRatesAsString = "random rates : ";
        for (float rate : super.randomRates)
            randomRatesAsString += rate + " ";
        randomRatesAsString += "\n";

        List<PerturbationLocation> locations = super.manager.getLocations();

        try {
         /* Sum Arrays */
            FileWriter writer = new FileWriter(pathToOutPutFile, false);
            writer.write("contains the data for the random rates analysis graph with " + exploration.getColumnName() + " as perturbator.\n");
            writer.write(exploration.getHeader() + randomRatesAsString +  super.manager.getHeader());
            String format = "%-10s %-" + exploration.getColumnName().length() + "s %-10s %-10s %-10s %-10s %-10s %-14s %-27s";
            writer.write(String.format(format,
                    "RandomRate", exploration.getColumnName(), "IndexLoc",
                    "#Success", "#Failure", "#Exception",
                    "#Call", "#Perturbation",
                    "%Success") + "\n");

            for (PerturbationLocation location : locations) {
                Tuple resultForLocation = new Tuple(3);
                for (int indexRandomRates = 0; indexRandomRates < randomRates.length; indexRandomRates++) {
                    Tuple result = new Tuple(5);
                    for (int indexPerturbator = 0; indexPerturbator < numberOfPerturbor; indexPerturbator++) {
                        for (int indexTask = 0; indexTask < super.manager.getIndexTask().size() ; indexTask++) {
                            result = result.add(results[locations.indexOf(location)][indexTask][indexPerturbator][indexRandomRates]);
                        }
                        writer.write(String.format(format,
                                randomRates[indexRandomRates], perturbatorsName[indexPerturbator], location.getLocationIndex(),
                                result.get(0), result.get(1), result.get(2),
                                result.get(3), result.get(4),
                                result.get(4) == 0 ? "NaN" : Util.getStringPerc(result.get(0), result.total(3))) + "\n");

                        resultForLocation = resultForLocation.add(result);
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
        }
    }
}
