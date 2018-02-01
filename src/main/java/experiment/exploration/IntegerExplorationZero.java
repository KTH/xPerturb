package experiment.exploration;

import perturbation.perturbator.AddNPerturbatorImpl;
import perturbation.perturbator.NothingPerturbatorImpl;
import perturbation.perturbator.ZeroPerturbatorImpl;

import java.util.ArrayList;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/05/17
 */
public class IntegerExplorationZero extends ExplorationImpl {

    public IntegerExplorationZero() {
        super.names = new String[1];
        super.names[0] = "0";
        super.perturbators = new ArrayList<>();
        super.perturbators.add(new ZeroPerturbatorImpl(new NothingPerturbatorImpl()));

        super.type = "Numerical";

        super.columnName = "Perturbator";

        super.name = "IntegerZero";
        super.header = "Exploration Zero\n";

        super.header += "ZERO : zero perturbator\n";
    }
}
