package experiment.exploration;

import perturbation.perturbator.Perturbator;

import java.util.List;

/**
 * Created by spirals on 01/05/16.
 */

/**
 * Interface to drive an explorer
 */
public interface Exploration {

    String getName();

    /**
     * Used for the logging
     * @return
     */
    String getColumnName();

    /**
     * Used for the logging
     * @return
     */
    String getHeader();

    /**
     * Used for the logging
     * @return the name of the all perturbators used during the exploration
     */
    String[] getPerturbatorsName();

    /**
     * @return list of Perturbator used during the exploration
     */
    List<Perturbator> getPerturbators();

    String getType();

}
