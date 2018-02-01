package experiment.exploration;

import perturbation.perturbator.Perturbator;

import java.util.List;

/**
 * Created by spirals on 01/05/16.
 */
public abstract class ExplorationImpl implements Exploration {

    protected String[] names;

    protected List<Perturbator> perturbators;

    protected String header;

    protected String name;

    protected String columnName;

    protected String type;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getColumnName() {
        return this.columnName;
    }

    @Override
    public String getHeader() {
        return this.header;
    }

    @Override
    public String[] getPerturbatorsName() {
        return this.names;
    }

    @Override
    public List<Perturbator> getPerturbators() {
        return this.perturbators;
    }

    @Override
    public String getType() {
        return this.type;
    }

}
