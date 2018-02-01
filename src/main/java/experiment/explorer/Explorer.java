package experiment.explorer;

import perturbation.location.PerturbationLocation;

/**
 * Created by spirals on 05/04/16.
 */
public interface Explorer {


    /**
     * Run the explorer
     */
    void run();

    /**
     * Method to run one time the subject on a task without any perturbation.
     */
    int runReference(int indexOfTask, PerturbationLocation location);

    /**
     * Initialize the PerturbationEngine.loggers and experiment.Logger with the setup of the exp.
     */
    void initLogger();

    /**
     * Output the logging after that the Explorer rand
     */
    void log();


}
