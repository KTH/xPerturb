package experiment.explorer;

import experiment.*;
import experiment.exploration.Exploration;
import perturbation.enactor.NeverEnactorImpl;
import perturbation.location.PerturbationLocation;
import perturbation.perturbator.NothingPerturbatorImpl;
import perturbation.perturbator.Perturbator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by beyni on 30/04/16.
 */
public abstract class ExplorerImpl implements Explorer {

    protected List<Perturbator> perturbators;

    protected Exploration exploration;

    protected Manager manager;

    protected Logger logger;

    public String name;

    private List<Object> outputs;

    public ExplorerImpl(Manager manager, Exploration exploration, String name) {
        this.exploration = exploration;
        this.perturbators = exploration.getPerturbators();
        this.name = name;
        this.manager = manager;
        this.outputs = new ArrayList<>();
    }

    protected Tuple run(int indexOfTask) {
        Tuple result = new Tuple(3);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Callable instanceRunner = this.manager.getCallable(this.manager.getTask(indexOfTask));
            Future future = executor.submit(instanceRunner);
            try {
                Object output = (future.get(Main2.numberOfSecondsToWait, TimeUnit.SECONDS));
                this.outputs.add(output);
                boolean assertion = this.manager.getOracle().assertPerturbation(this.manager.getTask(indexOfTask), output);
                executor.shutdownNow();
                if (assertion)
                    result.set(0, 1); // success
                else {
                    result.set(1, 1); // failures
                    this.manager.recover();
                }
                return result;
            } catch (TimeoutException e) {
                future.cancel(true);
                result.set(2, 1); // error computation time
                System.err.println("Time out!");
                executor.shutdownNow();
                this.manager.recover();
                return result;
            }
        } catch (Exception | Error e) {
            result.set(2, 1);
            executor.shutdownNow();
//            e.printStackTrace();
            this.manager.recover();
            return result;
        }
    }

    public void runLocation(int indexOfTask, PerturbationLocation location) {
        for (Perturbator perturbator : this.perturbators)
            runOnePerturbator(indexOfTask, location, perturbator);

        location.setPerturbator(new NothingPerturbatorImpl());
        location.setEnactor(new NeverEnactorImpl());
    }

    private void runTask(int indexTask) {
        @SuppressWarnings("unchecked")
        List<PerturbationLocation> locations = this.manager.getLocations(this.exploration.getType());
        for (PerturbationLocation location : locations) {
            if (Main2.verbose)
                System.out.println(indexTask + " " + location.getLocationIndex() + " " + Util.getStringPerc(locations.indexOf(location), locations.size()));
            this.runReference(indexTask, location);
            this.runLocation(indexTask, location);
        }
    }

    @Override
    public void run() {
        System.out.println("Run " + this.toString() + " on " + this.manager.getCUP().getSimpleName() + " ...");
        this.initLogger();
        @SuppressWarnings("unchecked")
        List<Integer> indices = this.manager.getIndexTask();
        for (int i = 0 ; i < indices.size() ; i++) {
            this.runTask(i);
            log();
        }
    }

    @Override
    public String toString() {
        return this.exploration.getName() + "_" + this.name;
    }

    public abstract void runOnePerturbator(int indexOfTask, PerturbationLocation location, Perturbator perturbator);

}
