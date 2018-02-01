package experiment;

/**
 * Created by spirals on 05/04/16.
 */

/**
 * Interface Oracle to assert the result of perturbed execution of subjects.
 * @param <T> input type of the subject
 * @param <P> output type of the subject
 */
public interface Oracle<T,P> {

    /**
     * Method to assert the result of execution
     * @param input the input of the execution
     * @param output the output of the execution
     * @return true if output is the result expected, false otherwise
     */
    boolean assertPerturbation(T input,P output);

}
