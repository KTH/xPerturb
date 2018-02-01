package experiment.exploration;

import perturbation.perturbator.InvPerturbatorImpl;

import java.util.ArrayList;

/**
 * Created by bdanglot on 02/05/16.
 */
public class BooleanExplorationNegation extends ExplorationImpl {

    public BooleanExplorationNegation() {
        super.names = new String[1];
        super.names[0] = "NEGB";
        super.perturbators = new ArrayList<>();
        super.perturbators.add(new InvPerturbatorImpl());

        super.type = "Boolean";

        super.columnName = "Perturbator";

        super.name = "BooleanNegation";

        super.header = "Exploration Boolean Negation\n";
        super.header += "NEGB : negation of boolean value.\n";
    }

}
