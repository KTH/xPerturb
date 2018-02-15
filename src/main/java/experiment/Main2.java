package experiment;

import experiment.exploration.*;
import experiment.explorer.CallExplorer;
import experiment.explorer.Explorer;
import experiment.explorer.HeatMapExplorer;
import experiment.explorer.RandomExplorer;
import experiment.explorer.SeedExplorer;
import experiment.explorer.TaskNumberExplorer;
import experiment.explorer.TaskSizeExplorer;

/**
 * Main, forked from experiment.Main in https://travis-ci.org/Spirals-Team/correctness-attraction-experiments/
 */
public class Main2 {

    public static Exploration exploration;

    //TODO remove all parameters manager in experiments
    public static Manager manager;

    public static int numberOfTask = 20;

    public static int sizeOfTask = 100;

    public static Integer seed = 23;

    public static String typePerturbed = "Numerical";

    public static int numberOfSecondsToWait = 20;

    public static boolean verbose = false;

    public static void main(String[] args) throws Exception {

        if (getIndexOfOption("-help", args) != -1 || getIndexOfOption("-h", args) != -1)
            usage();

        if (getIndexOfOption("-v", args) != -1)
            verbose = true;

        int currentIndex = -1;
        if ((currentIndex = getIndexOfOption("-time", args)) != -1) {
            try {
                numberOfSecondsToWait = Integer.parseInt(args[currentIndex + 1]);
            } catch (NumberFormatException e) {
                System.err.println("Time specified must be an integer.");
                usage();
            }
        }

        if ((currentIndex = getIndexOfOption("-size", args)) != -1) {
            try {
                sizeOfTask = Integer.parseInt(args[currentIndex + 1]);
            } catch (NumberFormatException e) {
                System.err.println("Size specified must be an integer.");
                usage();
            }
        }

        if ((currentIndex = getIndexOfOption("-nb", args)) != -1) {
            try {
                numberOfTask = Integer.parseInt(args[currentIndex + 1]);
            } catch (NumberFormatException e) {
                System.err.println("Number of Task specified must be an integer.");
                usage();
            }
        }

        if ((currentIndex = getIndexOfOption("-type", args)) != -1) {
            if (args[currentIndex + 1].equals("Numerical") || args[currentIndex + 1].equals("Boolean"))
                typePerturbed = args[currentIndex + 1];
            else
                usage();
        }

        if ((currentIndex = getIndexOfOption("-s", args)) != -1) {
            buildSubject(currentIndex + 1, args);
        }

        if ((currentIndex = getIndexOfOption("-exp", args)) != -1) {
            Explorer explorer = buildExp(currentIndex + 1, args);
            long time = System.currentTimeMillis();
            // run the exploration of the perturbation space
            explorer.run();
            System.out.println(System.currentTimeMillis() - time + " ms");
            explorer.log();
        } else if ((currentIndex = getIndexOfOption("-run", args)) != -1) {
            run(currentIndex + 1, args);
        }

    }

    private static void run(int index, String[] args) {

        runNumberTask();
    }

    private static Explorer buildExp(int index, String[] args) {
        int currentIndex = index;
        switch (args[currentIndex]) {
            case "rnd":
                return buildRnd(currentIndex + 1, args);
            case "call":
                return buildCall(currentIndex + 1, args);
            case "heatmap":
            case "hm":
                return buildHeatMap();
            default:
                usage();
        }
        //Make compiler happy
        return null;
    }

    private static void runNumberTask() {
        TaskNumberExplorer.run(manager.getClass());
    }

    private static void runSizeTask() {
        TaskSizeExplorer.run(manager.getClass());
    }

    private static void runSeed() {
        SeedExplorer.run(manager.getClass());
    }

    private static Explorer buildHeatMap() {
        return new HeatMapExplorer(manager, new IntegerExplorationPlusMagnitude());
    }

    private static Explorer buildCall(int i, String[] args) {
        exploration = getExploration(i, args);
        if (magnitude)
            i++;
        i++;
        return new CallExplorer(manager, exploration);
    }

    private static Explorer buildRnd(int i, String[] args) {
        exploration = getExploration(i, args);
        if (magnitude)
            i++;
        i++;
        int repeat;
        if (i < args.length) {
            try {
                repeat = Integer.parseInt(args[i]);
                i++;
            } catch (NumberFormatException e) {
                repeat = 5;
            }
        } else
            repeat = 5;

        float[] randomRate = null;
        if (i < args.length) {
            try {
                String[] rndRateStr;
                if ((rndRateStr = args[i].split(":")).length > 1) {
                    randomRate = new float[rndRateStr.length];
                    for (int index = 0; index < rndRateStr.length; index++)
                        randomRate[index] = Float.parseFloat(rndRateStr[index]);
                    i++;
                }
            } catch (NumberFormatException e) {
                randomRate = new float[]{0.001f, 0.005f, 0.01f, 0.05f, 0.1f, 0.15f, 0.2f, 0.25f, 0.3f};
            }
        }

        if (randomRate == null)
            randomRate = new float[]{0.001f, 0.005f, 0.01f, 0.05f, 0.1f, 0.15f, 0.2f, 0.25f, 0.3f};

        return new RandomExplorer(manager, exploration, repeat, randomRate);
    }

    private static boolean magnitude = false;

    private static Exploration getExploration(int i, String[] args) {
        switch (args[i]) {
            case "magnitude":
                typePerturbed = "Numerical";
                String[] mag = null;
                if (i + 1 < args.length && (mag = args[i + 1].split(":")).length > 1) {
                    int[] magint = new int[mag.length];
                    for (int index = 0; index < mag.length; index++)
                        magint[index] = Integer.parseInt(mag[index]);
                    magnitude = true;
                    return new IntegerExplorationPlusMagnitude(magint);
                } else
                    return new IntegerExplorationPlusMagnitude();
            case "pone":
                typePerturbed = "Numerical";
                return new IntegerExplorationPlusOne();
            case "mone":
                typePerturbed = "Numerical";
                return new IntegerExplorationMinueOne();
            case "zero":
                typePerturbed = "Numerical";
                return new IntegerExplorationZero();
            case "boolean":
                typePerturbed = "Boolean";
                return new BooleanExplorationNegation();
            case "one":
                return new IntegerExplorationPlusOne();
            default: throw new IllegalArgumentException("not recognized experiment");
        }
    }

    public static void buildSubject(int index, String[] args) throws Exception {
	manager = (Manager) Class.forName(args[index]).getDeclaredConstructor(int.class, int.class, int.class).newInstance(numberOfTask, sizeOfTask, seed);

    }

    public static int getIndexOfOption(String opt, String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(opt))
                return i;
        }
        return -1;
    }

    public static void usage() {
        System.out.println("options available : ");
        System.out.println("\t-size <integer> specify the size of each task");
        System.out.println("\t-nb <integer> specify the number of task");
        System.out.println("\t-time <integer> specify the number of seconds to wait until timeout");
        System.out.println("\t-v or -verbose to active Runner verbose mode");
        System.out.println("\t-s <subject> to specify the subject");
        System.out.println("\tvalue for <subject> : qs zip rc4 laguerre tea torrent rsa sudoku bayes simplex mt md5 bc");
        System.out.println("\t-exp <exp> specify the exp");
        System.out.println("\tvalue for <exp> call rnd heatmap");
        System.out.println("\tfor call and rnd exp you can specify which <exploration> just after it.");
        System.out.println("\tvalue for <exploration> : one magnitude boolean");
        System.out.println("\tyou can specify an array of magnitude to be used just after the key-word magnitude");
        System.out.println("\ta list of integer separated with \":\" (1:2:3 for example)");
        System.out.println("\tafter the exploration, you can specify the random rates list used by rnd explorer just as for the magnitude (but with float)");
        System.out.println("\tif no exp is specified, <call one> <call magnitude> <rnd one> <call boolean> <rnd boolean> will be run");
        System.out.println("\t-run tasksize tasknumber to run the exploration of the impact of the size or the number of task");
        System.out.println("\truns will be executed before everything else, you must specify a subject (with -s) before");
        System.out.println("\tyou can type -run gui to run the demo gui with success meter");
        System.out.println("\t-help display this help");
        System.exit(-1);
    }


}
