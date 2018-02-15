package experiment.explorer;

import experiment.Tuple;

public class RunResult {
	public boolean isSuccess = false;
	public boolean isFailure = false;
	public boolean isException = false;
	public int nbCalls = 0;
	public int nbEnactions = 0;

	/*
	public Tuple toTuple() {
		Tuple result = new Tuple(6);
		result.set(0, isSuccess ? 1 : 0);
		result.set(1, isFailure ? 1 : 0);
		result.set(2, isException ? 1 : 0);
		result.set(3, nbCalls);
		result.set(4, nbEnactions);
		result.set(5, 1);// used for summing over tuples, hence counting the number of executions
		return result;
	}
	*/
}