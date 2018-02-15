package experiment.explorer;

import experiment.Tuple;

public class AggregatedResult {
	public long nbSuccess = 0;
	public long nbFailure = 0;
	public long nbException = 0;
	public long nbCalls = 0;
	public long nbEnactions = 0;
	public long nbExecutions = 0;

	public void add(RunResult res) {
		nbSuccess += res.isSuccess ? 1 : 0;
		nbFailure += res.isFailure ? 1 : 0;
		nbException += res.isException ? 1 : 0;
		nbCalls += res.nbCalls;
		nbEnactions += res.nbEnactions;
		nbExecutions += 1;
	}
	public Tuple toTuple() {
		Tuple result = new Tuple(6);
		result.set(0, nbSuccess);
		result.set(1, nbFailure);
		result.set(2, nbException);
		result.set(3, nbCalls);
		result.set(4, nbEnactions);
		result.set(5, nbExecutions);// used for summing over tuples, hence counting the number of executions
		return result;
	}
}