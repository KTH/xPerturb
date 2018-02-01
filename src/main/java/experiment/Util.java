package experiment;

import perturbation.location.PerturbationLocation;
import perturbation.location.PerturbationLocationImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by spirals on 12/04/16.
 */
public class Util {

    private static ClassLoader loader = ClassLoader.getSystemClassLoader();

    public static List<PerturbationLocation> getAllLocations(String project, String packagePath, String type) {
        final List<PerturbationLocation> locations = new ArrayList<>();
        List<Class> classes = iterateFolders(new ArrayList<>(), project, packagePath);
		classes.stream().forEach(clazz -> {
			PerturbationLocationImpl.getLocationFromClass(clazz).forEach(location -> {
                        if (!locations.contains(location) && location.getType().equals(type))
                            locations.add(location);
                    });
                }
        );
		return locations;
    }

    private static List<Class> iterateFolders(List<Class> classes, String path, String currentPackage) {
        File root = new File(path);
        assert root.listFiles() != null;
        for (File subFile : root.listFiles()) {
            if (subFile.isDirectory())
                iterateFolders(classes, path + subFile.getName() + "/", currentPackage + "." + subFile.getName());
            else if (isJava(subFile.getName())) {
                try {
//                    Class<?> clazz = Class.forName(currentPackage + "." + removeExt(subFile.getName()));
                    Class<?> clazz = loader.loadClass(currentPackage + "." + removeExt(subFile.getName()));
//                    System.out.println(currentPackage + "." + removeExt(subFile.getName()));
                    classes.add(clazz);
                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
                    continue;
                }
            }
        }
        return classes;
    }

    private static String removeExt(String name) {
        return name.substring(0, name.length() - ".java".length());
    }

    private static boolean isJava(String name) {
        return name.endsWith(".java");
    }

    public static String getStringPerc(long nb, long total) {
        double perc = perc(nb, total);
        String ret = dash(perc);
        return ret + " " + String.format("%.2f", perc);
    }

    public static double perc(long nb, long total) {
        return (double) nb / (double) total * 100;
    }

    public static String dash(double perc) {
        String dash = "";
        for (int d = 0; d < perc / 5; d++) dash += "-";
        return dash;
    }

}
