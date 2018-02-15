package experiment;

import perturbation.location.PerturbationLocation;

import java.util.List;

/**
 * Created by spirals on 21/05/16.
 */
public interface Manager<T, P> {

    void initialize(int numberOfTask, int sizeOfTask);

    Class<?> getCUP();

    /**
     * @return a proper type of callable
     */
    CallableImpl<T, P> getCallable(T input);

    /**
     * @return a proper type of oracle
     */
    Oracle<T, P> getOracle();

    /**
     *
     */
    List<PerturbationLocation> getLocations();

    /**
     * Used this getter with filter on type of perturbation points.
     */
    List<PerturbationLocation> getLocations(String filter);

    List<Integer> getIndexTask();

    void setIndexTask(List<Integer> tasks);

    /**
     * @return the name of the subject
     */
    String getName();

    /**
     * @return a brief description of the subject
     */
    String getHeader();

    /**
     * @return the size of task
     */
    int getSizeOfTask();

    /**
     * This method should a return a clone of the task, in case of side effect
     * @param indexOfTask
     * @return
     */
    T getTask(int indexOfTask);

    /**
     * method used to recover a good state in case of errors/failure for stateful subject
     */
    void recover();

    /**
     * Method used to stop the subject properly
     */
    void stop();

}
