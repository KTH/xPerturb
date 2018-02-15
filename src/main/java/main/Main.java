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

	final public Launcher spoon = new Launcher();

	public Main() {
		UtilPerturbation.perturbableTypes.clear();
		spoon.addProcessor(new AssignmentProcessor());
		spoon.addProcessor(new VariableCaster());
		spoon.addProcessor(new PerturbationProcessor());
		// by default only int types are transformed
		addPerturbedType("int");
		spoon.getEnvironment().setAutoImports(true);
		spoon.getEnvironment().setLevel(Level.ALL.toString());
	}

	public void addPerturbedType(String type) {
		UtilPerturbation.perturbableTypes.add(type);
	}

	public void addInputResource(String input) {
		spoon.addInputResource(input);
	}

	public static void main(String[] args) {
		Main main = new Main();
		if (getIndexOfOption("-help", args) != -1 || getIndexOfOption("-h", args) != -1)
			usage();

		int index;
		if ((index = getIndexOfOption("-type", args)) != -1) {
			String[] types = args[index + 1].split(":");
			for (int i = 0; i < types.length; i++) {
				if (types[i].equals("IntNum")) {
					main.addPerturbedType("byte");
					main.addPerturbedType("short");
					main.addPerturbedType("int");
					main.addPerturbedType("Integer");
					main.addPerturbedType("BigInteger");
					main.addPerturbedType("long");
				} else {
					main.addPerturbedType(types[i]);
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

		if (getIndexOfOption("-r", args) != -1)
			main.spoon.addProcessor(new RenameProcessor());

		if (getIndexOfOption("-x", args) != -1)
			main.spoon.getEnvironment().setNoClasspath(true);

		final String separator = System.getProperty("path.separator");
		final String[] inputs = inputPath.split(separator);
		for (String input : inputs) {
			main.addInputResource(input);
		}

		if ((index = getIndexOfOption("-o", args)) == -1)
			main.spoon.setSourceOutputDirectory(inputPath);
		else
			main.spoon.setSourceOutputDirectory(args[index+1]);

		main.spoon.run();
	}

	// run the transformation
	public void run() {
		spoon.run();
	}
}
