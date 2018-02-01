package experiment.exploration;

import perturbation.perturbator.AddNPerturbatorImpl;

import java.util.ArrayList;

/**
 * Created by bdanglot on 14/06/16.
 */
public class IntegerExplorationMinueOne extends ExplorationImpl {

    public IntegerExplorationMinueOne() {
        super.names = new String[1];
        super.names[0] = "-1";
        super.perturbators = new ArrayList<>();
        super.perturbators.add(new AddNPerturbatorImpl(-1));

        super.type = "Numerical";

        super.columnName = "Perturbator";

        super.name = "IntegerMinusOne";
        super.header = "Exploration Minus One\n";

        super.header += "MONE : minus one perturbator\n";
    }


}
