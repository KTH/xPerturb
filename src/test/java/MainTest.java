
import experiment.Logger;
import experiment.Main2;
import main.Main;
import org.junit.After;
import org.junit.Test;
import processor.UtilPerturbation;
import quicksort.QuickSort;
import spoon.Launcher;
import util.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MainTest {

    @After
    public void tearDown() {
        // required for upcoming tests
        UtilPerturbation.reinitPerturbableTypes();
    }

    @Test
    public void testTransformationWithAPI() throws Exception {
        // this is an example of using the API main.Main for transforming code
        // contract: the main transforms the quicksort
        Main main = new Main();
        main.addInputResource("src/main/java/quicksort/QuickSort.java");
        main.run();
        // main perturbation points have been added
        assertEquals(41, main.spoon.getFactory().Class().get(QuickSort.class).getFields().size());
        assertEquals("__L40", main.spoon.getFactory().Class().get(QuickSort.class).getFields().get(0).getSimpleName());

    }

    @Test
    public void testMain() throws Exception {
        // this is an example of performing a complete exploration of the perturbation space with PONE
        // contract: the main does not throw an exception
        experiment.Main2.main(new String[] {"-v", "-s", "quicksort.QuickSortManager", "-nb", "10", "-size", "10", "-exp", "call", "pone"});

        Logger result = Main2.lastResultOfMainCall;

		assertEquals("Numerical", Main2.exploration.getType());
		assertEquals(10, result.getNumberOfTasks());

		// there are 47 pertubations points, see QuickSortInstr
		assertEquals(47, result.getNumberOfLocations());

        assertEquals(1, result.searchSpaceSizePerMagnitude.length);
		assertEquals(4331, result.searchSpaceSizePerMagnitude[0]);
		assertEquals(1, result.numberOfSuccessPerMagnitude.length);
		assertEquals(3323, result.numberOfSuccessPerMagnitude[0]);

    }
}
