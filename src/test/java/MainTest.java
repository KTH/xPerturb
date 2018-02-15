
import org.junit.Test;
import spoon.Launcher;
import util.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MainTest {

    @Test
    public void testMain() throws Exception {
        // contract: the main does not throw an exception
        experiment.Main2.main(new String[] {"-v", "-s", "quicksort.QuickSortManager", "-nb", "10", "-size", "10", "-exp", "call", "one"});
    }
}
