package experiment.exploration;

import perturbation.perturbator.AddNPerturbatorImpl;

import java.util.ArrayList;

/**
 * Created by beyni on 01/05/16.
 */
public class IntegerExplorationPlusOne extends ExplorationImpl {

    public IntegerExplorationPlusOne() {
        super.names = new String[1];
        super.names[0] = "1";
        super.perturbators = new ArrayList<>();
        super.perturbators.add(new AddNPerturbatorImpl(1));

        super.type = "Numerical";

        super.columnName = "Perturbator";

        super.name = "IntegerAddOne";
        super.header = "Exploration Plus One\n";

        super.header += "PONE : plus one perturbator\n";
    }

}
