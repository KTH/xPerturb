package quicksort;

import experiment.Oracle;

/**
 * Created by spirals on 05/04/16.
 */
public class QuickSortOracle implements Oracle<int[], int[]> {

    @Override
    public boolean assertPerturbation(int[] input, int[] output) {
        int cpt = 1;

        if (!contains(input, output[0]) || input.length != output.length)
            return false;

        for (int i = 1; i < output.length; i++) {
            if (output[i - 1] > output[i])
                return false;
            if (contains(input, output[i]))
                cpt++;
            else {
                return false;
            }
        }

        return input.length == cpt;
    }

    private boolean contains(int[] array, int value) {
        for (int values : array) {
            if (values == value)
                return true;
        }
        return false;
    }

}
