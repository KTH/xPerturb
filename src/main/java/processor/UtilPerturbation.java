package processor;

import perturbation.PerturbationEngine;
import perturbation.location.PerturbationLocation;
import perturbation.location.PerturbationLocationImpl;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtAnnotationType;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UtilPerturbation {

	/**
	 * Singleton
	 */
	private static UtilPerturbation instance = null;

	/**
	 * private construction
	 */
	private UtilPerturbation() {
		this.methodsByClass = new HashMap<String, List<CtMethod>>();
		this.listOfFieldByClass = new HashMap<String, List<CtField>>();
		this.staticBlockByClass = new HashMap<String, CtAnonymousExecutable>();
		this.currentLocation = 0;
	}

	/**
	 * internal getter singleton
	 * @return this method will build a new instance if the current is null, else the current.
	 */
	private static UtilPerturbation getInstance() {
		if (instance == null)
			instance = new UtilPerturbation();
		return instance;
	}

	/**
	 * List of type to be processed by perturbation processors
	 */
	public static final List<String> perturbableTypes = new ArrayList<>();

	//TODO remove this
	public static final String PACKAGE_NAME_PERTURBATION = "perturbation";


	/**
	 * name of methods of initialization of PerturbationLocation injected into the code.
	 * This methods are private static, and will create all injected perturbation point.
	 */
	private static final String INIT_METHOD_NAME = "initPerturbationLocation";

	/**
	 * Supported type by default
	 */
	static {
		reinitPerturbableTypes();
	}

	public static void reinitPerturbableTypes() {
		perturbableTypes.add("char");

		perturbableTypes.add("byte");
		perturbableTypes.add("short");
		perturbableTypes.add("int");
		perturbableTypes.add("Integer");
		perturbableTypes.add("long");
		perturbableTypes.add("BigInteger");

		perturbableTypes.add("boolean");

		perturbableTypes.add("float");
		perturbableTypes.add("double");
	}

	/**
	 * Contains the list of methods of initialization of Perturbation Location.
	 * Because java allow only 65536 instructions in one methods, by Qualified name of Class
	 */
	private Map<String, List<CtMethod>> methodsByClass;

	/**
	 * Contains all anonymous blocks to call all methods
	 */
	private Map<String, CtAnonymousExecutable> staticBlockByClass;

	/**
	 * Contains all injected perturbation point per class, by Qualified name of Class
	 */
	private Map<String, List<CtField>> listOfFieldByClass;

	/**
	 * Number of location injected, ensure the uniqueness of index.
	 */
	private int currentLocation;

	public static boolean isInPerturbatationPackage(CtElement candidate) {
		CtPackage parent = candidate.getParent(CtPackage.class);
		return parent.getQualifiedName().startsWith(PACKAGE_NAME_PERTURBATION);
	}

	/**
	 * Create the right method invocation of perturbation of PerturbationEngine at the given perturbation point.
	 * @return the invocation of the method, surrounding the original value.
	 */
	public static CtInvocation createStaticCallOfPerturbationFunction(Factory factory, String perturbedType, CtTypeReference originalType, CtExpression argument) {

		CtTypeReference<?> classReference = factory.Type().createReference(PerturbationEngine.class);
		String name = "";
		if (originalType.getSimpleName().equals("Integer"))
			name = "pint";
		else
			name = "p" + originalType.getSimpleName();
		CtExecutableReference execRef = classReference.getTypeDeclaration().getMethodsByName(name).get(0).getReference();

		CtTypeAccess typeAccess = factory.Core().createTypeAccess();
		typeAccess.setType(classReference);
		typeAccess.setAccessedType(classReference);

		CtExpression[] args = new CtExpression[2];
		args[0] = addFieldLocationToClass(factory, argument, perturbedType);
		args[1] = argument;

		getInstance().currentLocation++;

		return factory.Code().createInvocation(typeAccess, execRef, args);

	}

	/**
	 * Method to add the field of the perturbation to the class
	 */
	private static CtFieldRead addFieldLocationToClass(Factory factory, CtExpression argument, String typeOfPerturbation) {

		// find the top level class or the static class
		CtType clazz = argument.getParent(new TypeFilter<CtType>(CtType.class) {
			@Override
			public boolean matches(CtType element) {
				if (element.isTopLevel()) {
					return true;
				}
				if (element.getModifiers().contains(ModifierKind.STATIC)) {
					return true;
				}
				CtElement parent = element.getParent();
				if (parent instanceof CtInterface || parent instanceof CtAnnotationType) {
					return true;
				}
				return false;
			}
		});

		String currentKey = clazz.getQualifiedName();

		if (!getInstance().listOfFieldByClass.containsKey(currentKey)) {
			getInstance().listOfFieldByClass.put(currentKey, new ArrayList<>());
			getInstance().methodsByClass.put(currentKey, new ArrayList<>());
		}

		String fieldName = "__L" + getInstance().currentLocation;

		CtField fieldLocation = factory.Core().createField();
		fieldLocation.setSimpleName(fieldName);
		fieldLocation.setType(factory.Type().createReference(PerturbationLocation.class));
		fieldLocation.addModifier(ModifierKind.PUBLIC);
		fieldLocation.addModifier(ModifierKind.STATIC);
		fieldLocation.setParent(clazz);

		getInstance().listOfFieldByClass.get(currentKey).add(fieldLocation);

		String position = argument.getPosition().toString();
		CtTypeReference<PerturbationLocationImpl> refToLocationImpl = factory.Code().createCtTypeReference(PerturbationLocationImpl.class);
		CtConstructorCall constructorCall = factory.Code().createConstructorCall(refToLocationImpl,
				factory.Code().createLiteral(position),
				factory.Code().createLiteral(getInstance().currentLocation),
				factory.Code().createLiteral(typeOfPerturbation));

		CtFieldWrite writeField = factory.Core().createFieldWrite();
		writeField.setVariable(fieldLocation.getReference());

		CtAssignment assignmentField = factory.Core().createAssignment();
		assignmentField.setAssigned(writeField);
		assignmentField.setAssignment(constructorCall);

		//Methods
		if (!getInstance().methodsByClass.get(currentKey).isEmpty()) {
			if (getInstance().methodsByClass.get(currentKey).get(getInstance().methodsByClass.get(currentKey).size() - 1).getBody().getStatements().size() < 1000) {
				getInstance().methodsByClass.get(currentKey).get(getInstance().methodsByClass.get(currentKey).size() - 1).getBody().insertEnd(assignmentField);
			} else
				addInitMethodTo(clazz, factory, assignmentField);
		} else
			addInitMethodTo(clazz, factory, assignmentField);

		//Build the read of the field for the parameter of the perturbation function
		CtFieldRead readFieldLocation = factory.Core().createFieldRead();
		readFieldLocation.setVariable(fieldLocation.getReference());

		return readFieldLocation;
	}

	private static void addInitMethodTo(CtType clazz, Factory factory, CtStatement firstStatement) {

		CtTypeReference typeReference = clazz.getReference();

		String currentKey = clazz.getQualifiedName();

		CtMethod initMethod = factory.Core().createMethod();
		initMethod.setSimpleName(INIT_METHOD_NAME + (getInstance().methodsByClass.get(currentKey).size()));

		Set<ModifierKind> modifierKinds = new HashSet<ModifierKind>();
		modifierKinds.add(ModifierKind.PRIVATE);
		modifierKinds.add(ModifierKind.STATIC);
		initMethod.setModifiers(modifierKinds);
		initMethod.setType(factory.Type().VOID_PRIMITIVE);
		initMethod.setBody(factory.Code().createCtBlock(firstStatement));

		getInstance().methodsByClass.get(currentKey).add(initMethod);

		CtTypeAccess typeAccess = factory.Core().createTypeAccess();
		typeAccess.setType(typeReference);
		typeAccess.setAccessedType(typeReference);

		CtExecutableReference execRef = factory.Core().createExecutableReference();
		execRef.setDeclaringType(clazz.getReference());
		execRef.setSimpleName(INIT_METHOD_NAME + (getInstance().methodsByClass.get(currentKey).size() - 1));
		execRef.setStatic(true);

		if (getInstance().staticBlockByClass.containsKey(currentKey)) {
			getInstance().staticBlockByClass.get(currentKey).getBody().insertEnd(factory.Code().createInvocation(typeAccess, execRef));
		} else {
			CtBlock initBlock = factory.Code().createCtBlock(factory.Code().createInvocation(typeAccess, execRef));
			CtAnonymousExecutable staticBlock = factory.Core().createAnonymousExecutable();
			staticBlock.addModifier(ModifierKind.STATIC);
			staticBlock.setBody(initBlock);
			getInstance().staticBlockByClass.put(currentKey, staticBlock);
		}

	}

	public static void addAllFieldsAndMethods(Factory factory) {
		//maps have the sames key
		for (String currentKey : getInstance().listOfFieldByClass.keySet()) {
			//Add all fields at top of the current class
			CtType clazz = factory.Type().get(currentKey);
			if (!(clazz instanceof CtClass)) {
				return;
			}
			if (clazz == null)
				System.out.println(clazz);

			for (CtField field : getInstance().listOfFieldByClass.get(currentKey)) {
				clazz.addFieldAtTop(field);
			}

			//Add Methods
			List<CtMethod> methods = getInstance().methodsByClass.get(currentKey);
			for (CtMethod method : methods) {
				clazz.addMethod(method);
			}

			if (!getInstance().staticBlockByClass.isEmpty()) {
				//Add static block init
				List<CtAnonymousExecutable> anonymousExecutables = new ArrayList<CtAnonymousExecutable>();
				List<CtAnonymousExecutable> existingExecutables = ((CtClass) clazz).getAnonymousExecutables();
				for (CtAnonymousExecutable existing : existingExecutables) {
					anonymousExecutables.add(existing);
				}

				//Put the static block in first statement
				SourcePosition position = factory.Core().createSourcePosition(clazz.getPosition().getCompilationUnit(), -1, -1,new int[0]);

				getInstance().staticBlockByClass.get(currentKey).setPosition(position);
				anonymousExecutables.add(getInstance().staticBlockByClass.get(currentKey));
				((CtClass) clazz).setAnonymousExecutables(anonymousExecutables);
			}
		}
		instance = null;
	}

}
