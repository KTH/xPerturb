package util;

import processor.AssignmentProcessor;
import processor.PerturbationProcessor;
import processor.VariableCaster;
import spoon.Launcher;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by spirals on 07/03/16.
 */
public class Util {

    public static Launcher createSpoonWithPerturbationProcessors() {
        Launcher launcher = new Launcher();

//        launcher.getEnvironment().setLevel(Level.ALL.toString());

        launcher.addProcessor(new AssignmentProcessor());
        launcher.addProcessor(new VariableCaster());
        launcher.addProcessor(new PerturbationProcessor());

        launcher.addInputResource("src/main/java/perturbation/");

        launcher.getEnvironment().setShouldCompile(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.setBinaryOutputDirectory("spooned/bin");

        return launcher;
    }

    public static URLClassLoader removeOldFileFromClassPath(URLClassLoader sysloader) {
        URL[] urls = new URL[sysloader.getURLs().length - 1];
        for (int i = 0, j = 0; i < sysloader.getURLs().length; i++, j++) {
            if (sysloader.getURLs()[i].toString().endsWith(System.getProperty("user.dir")+"/target/classes/"))
                j--;
            else
                urls[j] = sysloader.getURLs()[i];
        }
        URLClassLoader classLoaderWithoutOldFild = URLClassLoader.newInstance(urls, null);
        return classLoaderWithoutOldFild;
    }

    public static void addPathToClassPath(URL u) {

        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{u});
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Error, could not add URL to system classloader");
        }
    }

}
