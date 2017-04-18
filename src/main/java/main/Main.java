package main;

import processor.AssignmentProcessor;
import processor.PerturbationProcessor;
import processor.RenameProcessor;
import processor.UtilPerturbation;
import processor.VariableCaster;
import spoon.Launcher;

import java.util.logging.Level;

/**
 * Created by bdanglot on 08/06/16.
 */
public class Main {

	private static int getIndexOfOption(String opt, String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(opt))
				return i;
		}
		return -1;
	}

	private static void usage() {
		System.out.println("usage : ");
		System.out.println("\tjava -jar target/jPerturb-0.0.1-SNAPSHOT-jar-with-dependencies.jar (-type <types>) (-r) -i <i> (-o <o>) (-x)");
		System.out.println("options : ");
		System.out.println("\t-type <types>: every primitive type, separated by \":\" (all primitive type will be processed by default)");
		System.out.println("\tspecial token: IntNum it will process all integer expression (from byte to long, with java.util.BigInteger)");
		System.out.println("\tflag -r: will rename classes by adding \"Instr\" as suffix");
		System.out.println("\t-i <i>: path to classes to be perturbed");
		System.out.println("\t-o <o>: path to output (default is the same as <i>)");
		System.out.println("\tflag -x: no classpath mode of spoon");
		System.out.println("Example:");
		System.out.println("\tjava -jar target/jPerturb-0.0.1-SNAPSHOT-jar-with-dependencies.jar -type IntNum:boolean -i src/test/resources/ -o target/trash/");
		System.exit(0);
	}

	public static void main(String[] args) {

		if (getIndexOfOption("-help", args) != -1 || getIndexOfOption("-h", args) != -1)
			usage();

		int index;
		if ((index = getIndexOfOption("-type", args)) != -1) {
			UtilPerturbation.perturbableTypes.clear();
			String[] types = args[index + 1].split(":");
			for (int i = 0; i < types.length; i++) {
				if (types[i].equals("IntNum")) {
					UtilPerturbation.perturbableTypes.add("byte");
					UtilPerturbation.perturbableTypes.add("short");
					UtilPerturbation.perturbableTypes.add("int");
					UtilPerturbation.perturbableTypes.add("Integer");
					UtilPerturbation.perturbableTypes.add("BigInteger");
					UtilPerturbation.perturbableTypes.add("long");
				} else {
					UtilPerturbation.perturbableTypes.add(types[i]);
				}
			}
		}

		System.out.println(UtilPerturbation.perturbableTypes);

		String inputPath = "";
		if ((index = getIndexOfOption("-i", args)) == -1) {
			System.err.println("Error: no input provided");
			System.exit(-1);
		} else
			inputPath = args[index+1];

		Launcher spoon = new Launcher();

		spoon.addProcessor(new AssignmentProcessor());
		spoon.addProcessor(new VariableCaster());
		spoon.addProcessor(new PerturbationProcessor());
		if (getIndexOfOption("-r", args) != -1)
			spoon.addProcessor(new RenameProcessor());

		spoon.getEnvironment().setAutoImports(true);
		if (getIndexOfOption("-x", args) != -1)
			spoon.getEnvironment().setNoClasspath(true);

		final String separator = System.getProperty("path.separator");
		final String[] inputs = inputPath.split(separator);
		for (String input : inputs) {
			spoon.addInputResource(input);
		}
		if ((index = getIndexOfOption("-o", args)) == -1)
			spoon.setSourceOutputDirectory(inputPath);
		else
			spoon.setSourceOutputDirectory(args[index+1]);

		spoon.getEnvironment().setLevel(Level.ALL.toString());

		spoon.run();
	}

}
