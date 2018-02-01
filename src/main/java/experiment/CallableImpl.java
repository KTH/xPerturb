package experiment;

import java.util.concurrent.Callable;

/**
 * Created by spirals on 08/04/16.
 */
public abstract class CallableImpl<T,P> implements Callable<P> {

    /**
     * Input of the execution under perturbation, usually provided by the manager
     */
    protected T input;

    public CallableImpl(T input) {
        this.input = input;
    }

    /**
     * @return the output expected by the oracle
     * @throws Exception
     */
    @Override
    public abstract P call() throws Exception;
}
