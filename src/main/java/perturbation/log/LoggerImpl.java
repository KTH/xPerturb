package perturbation.log;

import perturbation.location.PerturbationLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by spirals on 31/03/16.
 */
public class LoggerImpl implements Logger {

    private final Map<PerturbationLocation, Integer> numberOfCallsPerLocation = new HashMap<PerturbationLocation, Integer>();
    private final Map<PerturbationLocation, Integer> numberOfEnactionsPerLocation = new HashMap<PerturbationLocation, Integer>();

    @Override
    public void logOn(PerturbationLocation location) {
        this.numberOfCallsPerLocation.put(location, 0);
        this.numberOfEnactionsPerLocation.put(location, 0);
    }

    @Override
    public void remove(PerturbationLocation location) {
        this.numberOfCallsPerLocation.remove(location);
        this.numberOfEnactionsPerLocation.remove(location);
    }

    @Override
    public void logCall(PerturbationLocation location) {
        if (this.numberOfCallsPerLocation.containsKey(location))
            this.numberOfCallsPerLocation.put(location, this.numberOfCallsPerLocation.get(location) + 1);
    }

    @Override
    public void logEnaction(PerturbationLocation location) {
        if (numberOfEnactionsPerLocation.containsKey(location))
            this.numberOfEnactionsPerLocation.put(location, this.numberOfEnactionsPerLocation.get(location) + 1);
    }

    @Override
    public int getCalls(PerturbationLocation location) {
        return this.numberOfCallsPerLocation.get(location);
    }

    @Override
    public int getEnactions(PerturbationLocation location) {
        return this.numberOfEnactionsPerLocation.get(location);
    }

    @Override
    public boolean isLogging(PerturbationLocation location) {
        return this.numberOfCallsPerLocation.containsKey(location) && this.numberOfEnactionsPerLocation.containsKey(location);
    }

    @Override
    public void reset() {
        this.numberOfCallsPerLocation.clear();
        this.numberOfEnactionsPerLocation.clear();
    }

}
