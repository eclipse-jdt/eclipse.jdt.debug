/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.jdi.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;

import org.eclipse.jdi.Bootstrap;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.StepRequest;

/**
 * Tests for com.sun.jdi.* and JDWP commands.
 * These tests assume that the target program is
 * "org.eclipse.debug.jdi.tests.program.MainClass".
 *
 * Examples of arguments:
 *   -launcher SunVMLauncher -address c:\jdk1.2.2\ -cp d:\target
 *   -launcher J9VMLauncher -address d:\ive\ -cp d:\target
 */
public abstract class AbstractJDITest extends TestCase {
	static int TIMEOUT = 10000; //ms
	static protected int fBackEndPort = 9900;
	// We want subsequent connections to use different ports.
	protected static String fVMLauncherName;
	protected static String fTargetAddress;
	protected static String fClassPath;
	protected static String fBootPath;
	protected static String fVMType;
	protected com.sun.jdi.VirtualMachine fVM;
	protected Process fLaunchedProxy;
	protected Process fLaunchedVM;
	protected static int fVMTraceFlags = com.sun.jdi.VirtualMachine.TRACE_NONE;
	protected EventReader fEventReader;
	protected AbstractReader fConsoleReader;
	protected AbstractReader fConsoleErrorReader;
	protected AbstractReader fProxyReader;
	protected AbstractReader fProxyErrorReader;
	protected boolean fInControl = true;
	// Whether this test should control the VM (ie. starting it and shutting it down)
	protected static boolean fVerbose;
	protected static String fStdoutFile;
	protected static String fStderrFile;
	protected static String fProxyoutFile;
	protected static String fProxyerrFile;
	protected static String fVmCmd;
	protected static String fVmArgs;
	protected static String fProxyCmd;

	// Stack offset to the MainClass.run() method
	protected static final int RUN_FRAME_OFFSET = 1;

	/**
	 * Constructs a test case with a default name.
	 */
	public AbstractJDITest() {
		super("JDI Test");
	}
	/**
	 * Returns the names of the tests that are known to not work
	 * By default, none are excluded.
	 */
	protected String[] excludedTests() {
		return new String[] {
		};
	}
	/**
	 * Creates and returns an access watchpoint request
	 * for the field "fBool" in
	 * org.eclipse.debug.jdi.tests.program.MainClass
	 * NOTE: This assumes that the VM can watch field access.
	 */
	protected AccessWatchpointRequest getAccessWatchpointRequest() {
		// Get the field
		Field field = getField("fBool");

		// Create an access watchpoint for this field
		return fVM.eventRequestManager().createAccessWatchpointRequest(field);
	}
	/**
	 * Returns all tests that start with the given string.
	 * Returns a vector of String.
	 */
	protected Vector<String> getAllMatchingTests(String match) {
		Class<? extends AbstractJDITest> theClass = this.getClass();
		java.lang.reflect.Method[] methods = theClass.getDeclaredMethods();
		Vector<String> result = new Vector<>();
		for (int i = 0; i < methods.length; i++) {
			java.lang.reflect.Method m = methods[i];
			String name = m.getName();
			Class<?>[] parameters = m.getParameterTypes();
			if (parameters.length == 0 && name.startsWith(match)) {
				if (!isExcludedTest(name)) {
					result.add(name);
				} else
					System.out.println(name + " is excluded.");
			}
		}
		return result;
	}

	/**
	 * Returns if the current VM version is greater than or equal to 1.6
	 * @return <code>true</code> if a 1.6 or higher VM
	 * @since 3.8
	 */
	protected boolean is16OrGreater() {
		String ver = fVM.version();
		return ver.indexOf("1.6") > -1 || ver.indexOf("1.7") > -1;
	}

	/**
	 * Returns an array reference.
	 */
	protected ArrayReference getObjectArrayReference() {
		// Get static field "fArray"
		Field field = getField("fArray");

		// Get value of "fArray"
		return (ArrayReference) getMainClass().getValue(field);
	}

	/**
	 * Returns another array reference.
	 */
	protected ArrayReference getNonEmptyDoubleArrayReference() {
		// Get static field "fDoubleArray"
		Field field = getField("fDoubleArray");

		// Get value of "fDoubleArray"
		return (ArrayReference) getMainClass().getValue(field);
	}

	/**
	 * One-dimensional empty array reference getters
	 */
	protected ArrayReference getByteArrayReference() {
		Field field = getField("byteArray");
		return (ArrayReference) getMainClass().getValue(field);
	}
	protected ArrayReference getShortArrayReference() {
		Field field = getField("shortArray");
		return (ArrayReference) getMainClass().getValue(field);
	}
	protected ArrayReference getIntArrayReference() {
		Field field = getField("intArray");
		return (ArrayReference) getMainClass().getValue(field);
	}
	protected ArrayReference getLongArrayReference() {
		Field field = getField("longArray");
		return (ArrayReference) getMainClass().getValue(field);
	}
	protected ArrayReference getDoubleArrayReference() {
		Field field = getField("doubleArray");
		return (ArrayReference) getMainClass().getValue(field);
	}
	protected ArrayReference getFloatArrayReference() {
		Field field = getField("floatArray");
		return (ArrayReference) getMainClass().getValue(field);
	}
	protected ArrayReference getCharArrayReference() {
		Field field = getField("charArray");
		return (ArrayReference) getMainClass().getValue(field);
	}
	protected ArrayReference getBooleanArrayReference() {
		Field field = getField("booleanArray");
		return (ArrayReference) getMainClass().getValue(field);
	}
	/**
	 * Two-dimensional array reference getters
	 */
	protected ArrayReference getByteDoubleArrayReference() {
		Field field = getField("byteDoubleArray");
		return (ArrayReference) getMainClass().getValue(field);
	}
	protected ArrayReference getShortDoubleArrayReference() {
		Field field = getField("shortDoubleArray");
		return (ArrayReference) getMainClass().getValue(field);
	}
	protected ArrayReference getIntDoubleArrayReference() {
		Field field = getField("intDoubleArray");
		return (ArrayReference) getMainClass().getValue(field);
	}
	protected ArrayReference getLongDoubleArrayReference() {
		Field field = getField("longDoubleArray");
		return (ArrayReference) getMainClass().getValue(field);
	}
	protected ArrayReference getFloatDoubleArrayReference() {
		Field field = getField("floatDoubleArray");
		return (ArrayReference) getMainClass().getValue(field);
	}
	protected ArrayReference getDoubleDoubleArrayReference() {
		Field field = getField("doubleDoubleArray");
		return (ArrayReference) getMainClass().getValue(field);
	}
	protected ArrayReference getCharDoubleArrayReference() {
		Field field = getField("charDoubleArray");
		return (ArrayReference) getMainClass().getValue(field);
	}
	protected ArrayReference getBooleanDoubleArrayReference() {
		Field field = getField("booleanDoubleArray");
		return (ArrayReference) getMainClass().getValue(field);
	}

	/**
	 * Returns the array type.
	 */
	protected ArrayType getArrayType() {
		// Get array reference
		ArrayReference value = getObjectArrayReference();

		// Get reference type of "fArray"
		return (ArrayType) value.referenceType();
	}
	/**
	 * One-dimensional primitive array getters
	 */
	protected ArrayType getByteArrayType() {
		ArrayReference value = getByteArrayReference();
		return (ArrayType) value.referenceType();
	}
	protected ArrayType getShortArrayType() {
		ArrayReference value = getShortArrayReference();
		return (ArrayType) value.referenceType();
	}
	protected ArrayType getIntArrayType() {
		ArrayReference value = getIntArrayReference();
		return (ArrayType) value.referenceType();
	}
	protected ArrayType getLongArrayType() {
		ArrayReference value = getLongArrayReference();
		return (ArrayType) value.referenceType();
	}
	protected ArrayType getFloatArrayType() {
		ArrayReference value = getFloatArrayReference();
		return (ArrayType) value.referenceType();
	}
	protected ArrayType getDoubleArrayType() {
		ArrayReference value = getDoubleArrayReference();
		return (ArrayType) value.referenceType();
	}
	protected ArrayType getCharArrayType() {
		ArrayReference value = getCharArrayReference();
		return (ArrayType) value.referenceType();
	}
	protected ArrayType getBooleanArrayType() {
		ArrayReference value = getBooleanArrayReference();
		return (ArrayType) value.referenceType();
	}
	/**
	 * Two-dimensional primitive array getters
	 */
	protected ArrayType getByteDoubleArrayType() {
		ArrayReference value = getByteDoubleArrayReference();
		return (ArrayType) value.referenceType();
	}
	protected ArrayType getShortDoubleArrayType() {
		ArrayReference value = getShortDoubleArrayReference();
		return (ArrayType) value.referenceType();
	}
	protected ArrayType getIntDoubleArrayType() {
		ArrayReference value = getIntDoubleArrayReference();
		return (ArrayType) value.referenceType();
	}
	protected ArrayType getLongDoubleArrayType() {
		ArrayReference value = getLongDoubleArrayReference();
		return (ArrayType) value.referenceType();
	}
	protected ArrayType getFloatDoubleArrayType() {
		ArrayReference value = getFloatDoubleArrayReference();
		return (ArrayType) value.referenceType();
	}
	protected ArrayType getDoubleDoubleArrayType() {
		ArrayReference value = getDoubleDoubleArrayReference();
		return (ArrayType) value.referenceType();
	}
	protected ArrayType getCharDoubleArrayType() {
		ArrayReference value = getCharDoubleArrayReference();
		return (ArrayType) value.referenceType();
	}
	protected ArrayType getBooleanDoubleArrayType() {
		ArrayReference value = getBooleanDoubleArrayReference();
		return (ArrayType) value.referenceType();
	}

	/**
	 * Creates and returns a breakpoint request in the first
	 * instruction of the MainClass.triggerBreakpointEvent() method.
	 */
	protected BreakpointRequest getBreakpointRequest() {
		// Create a breakpoint request
		return fVM.eventRequestManager().createBreakpointRequest(getLocation());
	}

	/**
	 * Creates a new breakpoint request for a user specified position
	 * @param loc the location to set the breakpoint on
	 * @return a new breakpoint request or null if the location is invalid
	 * @since 3.3
	 */
	protected BreakpointRequest getBreakpointRequest(Location loc) {
		return fVM.eventRequestManager().createBreakpointRequest(loc);
	}
	/**
	 * Returns the class with the given name or null if not loaded.
	 */
	protected ClassType getClass(String name) {
		List<?> classes = fVM.classesByName(name);
		if (classes.size() == 0)
			return null;

		return (ClassType) classes.get(0);
	}
	/**
	 * Returns the class loader of
	 * org.eclipse.debug.jdi.tests.program.MainClass
	 */
	protected ClassLoaderReference getClassLoaderReference() {
		// Get main class
		ClassType type = getMainClass();

		// Get its class loader
		return type.classLoader();
	}
	/**
	 * Creates and returns an exception request for uncaught exceptions.
	 */
	protected ExceptionRequest getExceptionRequest() {
		return fVM.eventRequestManager().createExceptionRequest(null, false, true);
	}
	/**
	 * Returns the static field "fObject" in
	 * org.eclipse.debug.jdi.tests.program.MainClass
	 */
	protected Field getField() {
		return getField("fObject");
	}
	/**
	 * Returns the field with the given name in
	 * org.eclipse.debug.jdi.tests.program.MainClass.
	 */
	protected Field getField(String fieldName) {
		// Get main class
		ClassType type = getMainClass();

		// Get field
		Field result = type.fieldByName(fieldName);
		if (result == null)
			throw new Error("Unknown field: " + fieldName);

		return result;
	}
	/**
	 * Returns the n frame (starting at the top of the stack) of the thread
	 * contained in the static field "fThread" of org.eclipse.debug.jdi.tests.program.MainClass.
	 */
	protected StackFrame getFrame(int n) {
		// Make sure the thread is suspended
		ThreadReference thread = getThread();
		assertTrue(thread.isSuspended());

		// Get the frame
		StackFrame frame = null;
		try {
			List<?> frames = thread.frames();
			frame = (StackFrame) frames.get(n);
		} catch (IncompatibleThreadStateException e) {
			throw new Error("Thread was not suspended");
		}

		return frame;
	}
	/**
	 * Returns the interface type org.eclipse.debug.jdi.tests.program.Printable.
	 */
	protected InterfaceType getInterfaceType() {
		List<?> types = fVM.classesByName("org.eclipse.debug.jdi.tests.program.Printable");
		return (InterfaceType) types.get(0);
	}
	/**
	 * Returns the variable "t" in the frame running MainClass.run().
	 */
	protected LocalVariable getLocalVariable() {
		try {
			return getFrame(RUN_FRAME_OFFSET).visibleVariableByName("t");
		} catch (AbsentInformationException e) {
			return null;
		}
	}
	/**
	 * Returns the first location in MainClass.print(OutputStream).
	 */
	protected Location getLocation() {
		return getMethod().location();
	}
	/**
	 * Returns the class org.eclipse.debug.jdi.tests.program.MainClass.
	 */
	protected ClassType getMainClass() {
		return getClass( getMainClassName() );
	}
	/**
	 * Returns the method "print(Ljava/io/OutputStream;)V"
	 * in org.eclipse.debug.jdi.tests.program.MainClass
	 */
	protected Method getMethod() {
		return getMethod("print", "(Ljava/io/OutputStream;)V");
	}
	/**
	 * Returns the method with the given name and signature
	 * in org.eclipse.debug.jdi.tests.program.MainClass
	 */
	protected Method getMethod(String name, String signature) {
		return getMethod(
			getMainClassName(),
			name,
			signature);
	}
	/**
	 * Returns the method with the given name and signature
	 * in the given class.
	 */
	protected Method getMethod(String className, String name, String signature) {
		// Get main class
		ClassType type = getClass(className);

		// Get method print(OutputStream)
		Method method = null;
		List<Method> methods = type.methods();
		ListIterator<Method> iterator = methods.listIterator();
		while (iterator.hasNext()) {
			Method m = iterator.next();
			if ((m.name().equals(name)) && (m.signature().equals(signature))) {
				method = m;
				break;
			}
		}
		if (method == null)
			throw new Error("Unknown method: " + name + signature);

		return method;
	}
	/**
	 * Creates and returns a modification watchpoint request
	 * for the field "fBool" in
	 * org.eclipse.debug.jdi.tests.program.MainClass.
	 * NOTE: This assumes that the VM can watch field modification.
	 */
	protected ModificationWatchpointRequest getModificationWatchpointRequest() {
		// Get the field
		Field field = getField("fBool");

		// Create a modification watchpoint for this field
		return fVM.eventRequestManager().createModificationWatchpointRequest(field);
	}
	/**
	 * Returns the value of the static field "fObject" in
	 * org.eclipse.debug.jdi.tests.program.MainClass
	 */
	protected ObjectReference getObjectReference() {
		// Get main class
		ClassType type = getMainClass();

		// Get field "fObject"
		Field field = getField();

		// Get value of "fObject"
		return (ObjectReference) type.getValue(field);
	}
	/**
	 * Creates and returns an access watchpoint request
	 * for the static field "fString" in
	 * org.eclipse.debug.jdi.tests.program.MainClass
	 * NOTE: This assumes that the VM can watch field access.
	 */
	protected AccessWatchpointRequest getStaticAccessWatchpointRequest() {
		// Get the static field
		Field field = getField("fString");

		// Create an access watchpoint for this field
		return fVM.eventRequestManager().createAccessWatchpointRequest(field);
	}
	/**
	 * Creates and returns a modification watchpoint request
	 * for the static field "fString" in
	 * org.eclipse.debug.jdi.tests.program.MainClass.
	 * NOTE: This assumes that the VM can watch field modification.
	 */
	protected ModificationWatchpointRequest getStaticModificationWatchpointRequest() {
		// Get the field
		Field field = getField("fString");

		// Create a modification watchpoint for this field
		return fVM.eventRequestManager().createModificationWatchpointRequest(field);
	}
	/**
	 * Returns the value of the static field "fString" in
	 * org.eclipse.debug.jdi.tests.program.MainClass
	 */
	protected StringReference getStringReference() {
		// Get field "fString"
		Field field = getField("fString");

		// Get value of "fString"
		return (StringReference) getMainClass().getValue(field);
	}
	/**
	 * Returns the class java.lang.Object.
	 */
	protected ClassType getSystemType() {
		List<ReferenceType> classes = fVM.classesByName("java.lang.Object");
		if (classes.size() == 0)
			return null;

		return (ClassType) classes.get(0);
	}
	/**
	 * Returns the thread contained in the static field "fThread" in
	 * org.eclipse.debug.jdi.tests.program.MainClass
	 */
	protected ThreadReference getThread() {
		return getThread("fThread");
	}

	protected ThreadReference getMainThread() {
		return getThread("fMainThread");
	}

	private ThreadReference getThread(String fieldName) {
		ClassType type = getMainClass();
		if (type == null)
			return null;

		// Get static field "fThread"
		List<Field> fields = type.fields();
		ListIterator<Field> iterator = fields.listIterator();
		Field field = null;
		while (iterator.hasNext()) {
			field = iterator.next();
			if (field.name().equals(fieldName))
				break;
		}

		// Get value of "fThread"
		Value value = type.getValue(field);
		if (value == null)
			return null;

		return (ThreadReference) value;
	}
	/**
	 * Returns the VM info for this test.
	 */
	public VMInformation getVMInfo() {
		return new VMInformation(
			fVM,
			fVMType,
			fLaunchedVM,
			fEventReader,
			fConsoleReader);
	}
	/**
	 * Returns whether the given test is excluded for the VM we are testing.
	 */
	private boolean isExcludedTest(String testName) {
		String[] excludedTests = excludedTests();
		if (excludedTests == null)
			return false;
		for (int i = 0; i < excludedTests.length; i++)
			if (testName.equals(excludedTests[i]))
				return true;
		return false;
	}

	/**
	 * Launches the target VM and connects to VM.
	 */
	protected void launchTargetAndConnectToVM() {
		launchTarget();
		connectToVM();
	}

	protected boolean vmIsRunning() {
		boolean isRunning = false;
		try {
			if (fLaunchedVM != null)
				fLaunchedVM.exitValue();
		} catch (IllegalThreadStateException e) {
			isRunning = true;
		}
		return isRunning;
	}

	protected void launchTarget() {
		if (fVmCmd != null)
			launchCommandLineTarget();
		else if (fVMLauncherName.equals("SunVMLauncher"))
			launchSunTarget();
		else if (fVMLauncherName.equals("IBMVMLauncher"))
			launchIBMTarget();
		else
			launchJ9Target();
	}

	/**
	 * Launches the target VM specified on the command line.
	 */
	private void launchCommandLineTarget() {
		try {
			if (fProxyCmd != null) {
				fLaunchedProxy = Runtime.getRuntime().exec(parseCommand(fProxyCmd));
			}
			fLaunchedVM = Runtime.getRuntime().exec(parseCommand(fVmCmd));
		} catch (IOException e) {
			throw new Error("Could not launch the VM because " + e.getMessage());
		}
	}

	/**
	 * Parse the command {@link String} to make sure we use
	 * {@link Runtime#exec(String[])}.
	 *
	 * @param command
	 * @return the array of items from the command {@link String}
	 * @since 4.3
	 */
	String[] parseCommand(String command) {
		StringTokenizer tokenizer = new StringTokenizer(command);
		String[] commandArray = new String[tokenizer.countTokens()];
		//first token is the command
		if(tokenizer.hasMoreTokens()) {
			commandArray[0] = tokenizer.nextToken();
		}
		for (int i= 1; tokenizer.hasMoreTokens(); i++) {
			commandArray[i] = tokenizer.nextToken();
		}
		return commandArray;
	}

	/**
	 * Launches the target J9 VM.
	 */
	private void launchJ9Target() {
		try {
			// Launch proxy
			String proxyString[] = new String[3];
			int index = 0;
			String binDirectory =
				fTargetAddress
					+ System.getProperty("file.separator")
					+ "bin"
					+ System.getProperty("file.separator");

			proxyString[index++] = binDirectory + "j9proxy";
			proxyString[index++] = "localhost:" + (fBackEndPort - 1);
			proxyString[index++] = "" + fBackEndPort;
			fLaunchedProxy = Runtime.getRuntime().exec(proxyString);

			// Launch target VM
			Vector<String> commandLine = new Vector<>();

			String launcher = binDirectory + "j9w.exe";
			File vm= new File(launcher);
			if (!vm.exists()) {
				launcher = binDirectory + "j9";
			}
			commandLine.add(launcher);

			if (fBootPath.length() > 0)
				commandLine.add("-bp:" + fBootPath);
			commandLine.add("-cp:" + fClassPath);
			commandLine.add("-debug:" + (fBackEndPort - 1));
			injectVMArgs(commandLine);
			commandLine.add(getMainClassName());

			fLaunchedVM = exec(commandLine);

		} catch (IOException e) {
			throw new Error("Could not launch the VM because " + e.getMessage());
		}
	}

	/**
	 * Launches the target Sun VM.
	 */
	private void launchSunTarget() {
		try {
			// Launch target VM
			StringBuilder binDirectory= new StringBuilder();
			if (fTargetAddress.endsWith("jre")) {
				binDirectory.append(fTargetAddress.substring(0, fTargetAddress.length() - 4));
			} else {
				binDirectory.append(fTargetAddress);
			}
			binDirectory.append(System.getProperty("file.separator"));
			binDirectory.append("bin").append(System.getProperty("file.separator"));

			Vector<String> commandLine = new Vector<>();

			String launcher = binDirectory.toString() + "javaw.exe";
			File vm= new File(launcher);
			if (!vm.exists()) {
				launcher = binDirectory + "java";
			}
			commandLine.add(launcher);

			if (fBootPath.length() > 0) {
				commandLine.add("-bootpath");
				commandLine.add(fBootPath);
			}
			commandLine.add("-classpath");
			commandLine.add(fClassPath);
			commandLine.add("-Xdebug");
			commandLine.add("-Xnoagent");
			commandLine.add("-Djava.compiler=NONE");
			commandLine.add("-Xrunjdwp:transport=dt_socket,address=" + fBackEndPort + ",suspend=y,server=y");
			injectVMArgs(commandLine);
			commandLine.add(getMainClassName());

			fLaunchedVM = exec(commandLine);

		} catch (IOException e) {
			throw new Error("Could not launch the VM because " + e.getMessage());
		}
	}
	/**
	 * Launches the target IBM VM.
	 */
	private void launchIBMTarget() {
		try {
			// Launch target VM
			String binDirectory =
				fTargetAddress
					+ System.getProperty("file.separator")
					+ "bin"
					+ System.getProperty("file.separator");

			Vector<String> commandLine = new Vector<>();

			commandLine.add(binDirectory + "javaw");
			if (fBootPath.length() > 0) {
				commandLine.add("-bootpath");
				commandLine.add(fBootPath);
			}

			commandLine.add("-classpath");
			commandLine.add(fClassPath);
			commandLine.add("-Xdebug");
			commandLine.add("-Xnoagent");
			commandLine.add("-Djava.compiler=NONE");
			commandLine.add("-Xrunjdwp:transport=dt_socket,address=" + fBackEndPort + ",suspend=y,server=y");
			injectVMArgs(commandLine);
			commandLine.add(getMainClassName());

			fLaunchedVM = exec(commandLine);

		} catch (IOException e) {
			throw new Error("Could not launch the VM because " + e.getMessage());
		}
	}

	protected String getMainClassName() {
		return "org.eclipse.debug.jdi.tests.program.MainClass";
	}

	protected String getTestPrefix() {
		return "testJDI";
	}

	/**
	 * Injects arguments specified using -vmargs command line option into
	 * the provided commandLine.
	 * @param commandLine A vector of command line argument strings.
	 */
	private void injectVMArgs(Vector<String> commandLine) {
		if (fVmArgs != null) {
			String[] args = fVmArgs.split(",");
			for (int i=0; i < args.length; i++) {
				commandLine.add(args[i]);
			}
		}
	}

	/**
	 * Flattens the variable size command line and calls Runtime.exec().
	 * @param commandLine A vector of command line argument strings.
	 * @return The Process created by Runtime.exec()
	 * @throws IOException
	 */
	private Process exec(Vector<String> commandLine) throws IOException {
		String[] vmString = new String[commandLine.size()];
		commandLine.toArray(vmString);
		return Runtime.getRuntime().exec(vmString);
	}


	/**
	 * Connects to the target vm.
	 */
	protected void connectToVM() {
		// Start the console reader if possible so that the VM doesn't block when the stdout is full
		startConsoleReaders();


		// Contact the VM (try 10 times)
		for (int i = 0; i < 10; i++) {
			try {
				VirtualMachineManager manager = Bootstrap.virtualMachineManager();
				List<AttachingConnector> connectors = manager.attachingConnectors();
				if (connectors.size() == 0)
					break;
				AttachingConnector connector = connectors.get(0);
				Map<String, Argument> args = connector.defaultArguments();
				args.get("port").setValue(String.valueOf(fBackEndPort));
				args.get("hostname").setValue("localhost");

				fVM = connector.attach(args);
				if (fVMTraceFlags != com.sun.jdi.VirtualMachine.TRACE_NONE)
					fVM.setDebugTraceMode(fVMTraceFlags);
				break;
			} catch (IllegalConnectorArgumentsException e) {
			} catch (IOException e) {
//				System.out.println("Got exception: " + e.getMessage());
				try {
					if (i == 9) {
						System.out.println(
							"Could not contact the VM at localhost" + ":" + fBackEndPort + ".");
					}
					Thread.sleep(200);
				} catch (InterruptedException e2) {
				}
			}
		}
		if (fVM == null) {
			if (fLaunchedVM != null) {
				// If the VM is not running, output error stream
				try {
					if (!vmIsRunning()) {
						InputStream in = fLaunchedVM.getErrorStream();
						int read;
						do {
							read = in.read();
							if (read != -1)
								System.out.print((char) read);
						} while (read != -1);
					}
				} catch (IOException e) {
				}

				// Shut it down
				killVM();
			}
			throw new Error("Could not contact the VM");
		}
		startEventReader();
	}
	/**
	 * Initializes the fields that are used by this test only.
	 */
	public abstract void localSetUp();
	/**
	 * Makes sure the test leaves the VM in the same state it found it.
	 * Default is to do nothing.
	 */
	public void localTearDown() {
	}
	/**
	 * Parses the given arguments and store them in this tests
	 * fields.
	 * Returns whether the parsing was successful.
	 */
	protected static boolean parseArgs(String[] args) {
		// Default values
		String vmVendor = System.getProperty("java.vm.vendor");
		String vmVersion = System.getProperty("java.vm.version");
		String targetAddress = System.getProperty("java.home");
		String vmLauncherName;
		if (vmVendor != null
			&& (vmVendor.indexOf("Sun") > -1 || vmVendor.indexOf("Oracle") > -1 || vmVendor.indexOf("Apple") > -1)
			&& vmVersion != null) {
			vmLauncherName = "SunVMLauncher";
		} else if (
			vmVendor != null && vmVendor.indexOf("IBM") > -1 && vmVersion != null) {
			vmLauncherName = "IBMVMLauncher";
		} else {
			vmLauncherName = "J9VMLauncher";
		}
		String classPath = System.getProperty("java.class.path");
		String bootPath = "";
		String vmType = "?";
		boolean verbose = false;

		// Parse arguments
		for (int i = 0; i < args.length; ++i) {
			String arg = args[i];
			if (arg.startsWith("-")) {
				if (arg.equals("-verbose") || arg.equals("-v")) {
					verbose = true;
				} else {
					String next = (i < args.length - 1) ? args[++i] : null;
					// If specified, passed values override default values
					if (arg.equals("-launcher")) {
						vmLauncherName = next;
					} else if (arg.equals("-address")) {
						targetAddress = next;
					} else if (arg.equals("-port")) {
						fBackEndPort = Integer.parseInt(next);
					} else if (arg.equals("-cp")) {
						classPath = next;
					} else if (arg.equals("-bp")) {
						bootPath = next;
					} else if (arg.equals("-vmtype")) {
						vmType = next;
					} else if (arg.equals("-stdout")) {
						fStdoutFile = next;
					} else if (arg.equals("-stderr")) {
						fStderrFile = next;
					} else if (arg.equals("-proxyout")) {
						fProxyoutFile = next;
					} else if (arg.equals("-proxyerr")) {
						fProxyerrFile = next;
					} else if (arg.equals("-vmcmd")) {
						fVmCmd = next;
					} else if (arg.equals("-vmargs")) {
						fVmArgs = next;
					} else if (arg.equals("-proxycmd")) {
						fProxyCmd = next;
					} else if (arg.equals("-trace")) {
						if (next.equals("all")) {
							fVMTraceFlags = com.sun.jdi.VirtualMachine.TRACE_ALL;
						} else {
							fVMTraceFlags = Integer.decode(next).intValue();
						}
					} else {
						System.out.println("Invalid option: " + arg);
						printUsage();
						return false;
					}
				}
			}
		}
		fVMLauncherName = vmLauncherName;
		fTargetAddress = targetAddress;
		fClassPath = classPath;
		fBootPath = bootPath;
		fVMType = vmType;
		fVerbose = verbose;
		return true;
	}
	/**
	 * Prints the various options to pass to the constructor.
	 */
	protected static void printUsage() {
		System.out.println("Possible options:");
		System.out.println("-launcher <Name of the launcher class>");
		System.out.println("-address <Address of the target VM>");
		System.out.println("-port <Debug port number>");
		System.out.println("-cp <Path to the test program>");
		System.out.println("-bp <Boot classpath for the system class library>");
		System.out.println("-vmtype <The type of VM: JDK, J9, ...>");
		System.out.println("-verbose | -v");
		System.out.println("-stdout <file where VM output is written to>");
		System.out.println("-stderr <file where VM error output is written to>");
		System.out.println("-proxyout <file where proxy output is written to>");
		System.out.println("-proxyerr <file where proxy error output is written to>");
		System.out.println("-vmcmd <exec string to start VM>");
		System.out.println("-vmargs <comma-separated list of VM arguments>");
		System.out.println("-proxycmd <exec string to start proxy>");
	}
	/**
	 * Set the value of the "fBool" field back to its original value
	 */
	protected void resetField() {
		Field field = getField("fBool");
		Value value = null;
		value = fVM.mirrorOf(false);
		try {
			getObjectReference().setValue(field, value);
		} catch (ClassNotLoadedException e) {
			assertTrue("resetField.2", false);
		} catch (InvalidTypeException e) {
			assertTrue("resetField.3", false);
		}
	}
	/**
	 * Set the value of the "fString" field back to its original value
	 */
	protected void resetStaticField() {
		Field field = getField("fString");
		Value value = null;
		value = fVM.mirrorOf("Hello World");
		try {
			getMainClass().setValue(field, value);
		} catch (ClassNotLoadedException e) {
			assertTrue("resetField.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("resetField.2", false);
		}
	}
	/**
	 * Runs this test's suite with the given arguments.
	 */
	protected void runSuite(String[] args) {
		// Check args
		if (!parseArgs(args))
			return;

		// Run test
		System.out.println(new java.util.Date());
		System.out.println("Begin testing " + getName() + "...");
		junit.textui.TestRunner.run(suite());
		System.out.println("Done testing " + getName() + ".");
	}
	/**
	 * Sets the 'in control of the VM' flag for this test.
	 */
	public void setInControl(boolean inControl) {
		fInControl = inControl;
	}
	/**
	 * Launch target VM and start program in target VM.
	 */
	protected void launchTargetAndStartProgram() {
		launchTargetAndConnectToVM();
		startProgram();
	}
	/**
	 * Init tests
	 */
	@Override
	protected void setUp() {
		if (fVM == null || fInControl) {
			launchTargetAndStartProgram();
		}
		try {
			verbose("Setting up the test");
			localSetUp();
		} catch (RuntimeException e) {
			System.out.println("Runtime exception during set up:");
			e.printStackTrace();
		} catch (Error e) {
			System.out.println("Error during set up:");
			e.printStackTrace();
		}
	}
	/**
	 * Sets the VM info for this test.
	 */
	public void setVMInfo(VMInformation info) {
		if (info != null) {
			fVM = info.fVM;
			fLaunchedVM = info.fLaunchedVM;
			fEventReader = info.fEventReader;
			fConsoleReader = info.fConsoleReader;
		}
	}
	/**
	 * Stop console and event readers.
	 */
	protected void stopReaders() {
		stopEventReader();
		stopConsoleReaders();
	}
	/**
	 * Shut down the target.
	 */
	public void shutDownTarget() {
		stopReaders();
		if (fVM != null) {
			try {
				fVM.exit(0);
			} catch (VMDisconnectedException e) {
			}
		}

		fVM = null;
		fLaunchedVM = null;

		// We want subsequent connections to use different ports, unless a
		// VM exec sting is given.
		if (fVmCmd == null)
			fBackEndPort += 2;
	}
	/**
	 * Starts the threads that reads from the VM and proxy input and error streams
	 */
	private void startConsoleReaders() {
		if (fStdoutFile != null) {
			fConsoleReader =
				new FileConsoleReader(
					"JDI Tests Console Reader",
					fStdoutFile,
					fLaunchedVM.getInputStream());
		} else {
			fConsoleReader =
				new NullConsoleReader("JDI Tests Console Reader", fLaunchedVM.getInputStream());
		}
		fConsoleReader.start();

		if (fStderrFile != null) {
			fConsoleErrorReader =
				new FileConsoleReader(
					"JDI Tests Console Error Reader",
					fStderrFile,
					fLaunchedVM.getErrorStream());
		} else {
			fConsoleErrorReader =
				new NullConsoleReader(
					"JDI Tests Console Error Reader",
					fLaunchedVM.getErrorStream());
		}
		fConsoleErrorReader.start();

		if (fLaunchedProxy == null)
			return;

		if (fProxyoutFile != null) {
			fProxyReader =
				new FileConsoleReader(
					"JDI Tests Proxy Reader",
					fProxyoutFile,
					fLaunchedProxy.getInputStream());
		} else {
			fProxyReader =
				new NullConsoleReader(
					"JDI Tests Proxy Reader",
					fLaunchedProxy.getInputStream());
		}
		fProxyReader.start();

		if (fProxyerrFile != null) {
			fProxyErrorReader =
				new FileConsoleReader(
					"JDI Tests Proxy Error Reader",
					fProxyerrFile,
					fLaunchedProxy.getErrorStream());
		} else {
			fProxyErrorReader =
				new NullConsoleReader(
					"JDI Tests Proxy Error Reader",
					fLaunchedProxy.getErrorStream());
		}
		fProxyErrorReader.start();
	}
	/**
	 * Stops the console reader.
	 */
	private void stopConsoleReaders() {
		if (fConsoleReader != null)
			fConsoleReader.stop();
		if (fConsoleErrorReader != null)
			fConsoleErrorReader.stop();
		if (fProxyReader != null)
			fProxyReader.stop();
		if (fProxyErrorReader != null)
			fProxyErrorReader.stop();
	}
	/**
	 * Starts event reader.
	 */
	private void startEventReader() {
		// Create the VM event reader.
		fEventReader = new EventReader("JDI Tests Event Reader", fVM.eventQueue());
	}
	/**
	 * Stops the event reader.
	 */
	private void stopEventReader() {
		fEventReader.stop();
	}
	protected void killVM() {
		if (fLaunchedVM != null)
			fLaunchedVM.destroy();
		if (fLaunchedProxy != null)
			fLaunchedProxy.destroy();
	}
	/**
	 * Starts the target program.
	 */
	protected void startProgram() {
		verbose("Starting target program");

		// Request class prepare events
		EventRequest classPrepareRequest =
			fVM.eventRequestManager().createClassPrepareRequest();
		classPrepareRequest.enable();

		// Prepare to receive the token class prepare event
		ClassPrepareEventWaiter waiter =
			new ClassPrepareEventWaiter(
				classPrepareRequest,
				true,
				getMainClassName());
		fEventReader.addEventListener(waiter);

		// Start the event reader (this will start the VM when the VMStartEvent is picked up)
		fEventReader.start();

		// Wait until the program has started
		Event event = waitForEvent(waiter, 3 * TIMEOUT);
		fEventReader.removeEventListener(waiter);
		if (event == null) {
//			try {
				System.out.println(
					"\nThe program doesn't seem to have started after " + (3 * TIMEOUT) + "ms");
//				InputStream errorStream = fLaunchedVM.getErrorStream();
//				int read;
//				do {
//					read = errorStream.read();
//					if (read != -1)
//						System.out.print((char) read);
//				} while (read != -1);
//			} catch (IOException e) {
//			}
		}

		// Stop class prepare events
		fVM.eventRequestManager().deleteEventRequest(classPrepareRequest);

		// Wait for the program to be ready to be tested
		waitUntilReady();
	}
	/**
	 * Returns all tests
	 */
	protected Test suite() {
		JDITestSuite suite = new JDITestSuite(this);
		Vector<String> testNames = getAllMatchingTests( getTestPrefix() );
		Iterator<String> iterator = testNames.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			suite.addTest(new JDITestCase(this, name));
		}
		return suite;
	}
	/**
	 * Undo the initialization of the test.
	 */
	@Override
	protected void tearDown() {
		try {
			super.tearDown();
		} catch (Exception e) {
			System.out.println("Exception during tear down:");
			e.printStackTrace();
		}
		try {
			verbose("Tearing down the test");
			localTearDown();

			// Ensure that the test didn't leave a modification watchpoint that could change the expected state of the program
			if (fVM != null) {
				assertTrue(fVM.eventRequestManager().modificationWatchpointRequests().size() == 0);
				if (fInControl) {
					shutDownTarget();
				}
			}

		} catch (RuntimeException e) {
			System.out.println("Runtime exception during tear down:");
			e.printStackTrace();
		} catch (Error e) {
			System.out.println("Error during tear down:");
			e.printStackTrace();
		}

	}
	/**
	 * Triggers and waits for the given event to come in.
	 * Let the thread go if asked.
	 * Throws an Error if the event didn't come in after TIMEOUT ms
	 */
	protected Event triggerAndWait(
		EventRequest request,
		String eventType,
		boolean shouldGo) {
		Event event = triggerAndWait(request, eventType, shouldGo, TIMEOUT);
		if (event == null)
			throw new Error(
				"Event for " + request + " didn't come in after " + TIMEOUT + "ms");

		return event;
	}
	/**
	 * Triggers and waits for the given event to come in.
	 * Let the thread go if asked.
	 * Returns null if the event didn't come in after the given amount of time (in ms)
	 * @param time the time to wait
	 */
	protected Event triggerAndWait(
		EventRequest request,
		String eventType,
		boolean shouldGo,
		long time) {
		// Suspend only if asked
		if (shouldGo)
			request.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		else
			request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);

		// Enable request
		request.enable();

		// Prepare to receive the event
		EventWaiter waiter = new EventWaiter(request, shouldGo);
		fEventReader.addEventListener(waiter);

		// Trigger the event
		triggerEvent(eventType);

		// Wait for the event to come in
		Event event = waitForEvent(waiter, TIMEOUT);
		fEventReader.removeEventListener(waiter);

		if (shouldGo) {
			// Wait for the program to be ready
			waitUntilReady();
		}

		// Clear request
		fVM.eventRequestManager().deleteEventRequest(request);

		return event;
	}
	/**
	 * Triggers the given type of event. See the MainClass for details on types of event.
	 */
	protected void triggerEvent(String eventType) {
		// Set the "fEventType" field to the given eventType
		ClassType type = getMainClass();
		Field field = type.fieldByName("fEventType");
		assertTrue("1", field != null);

		Value value = null;
		value = fVM.mirrorOf(eventType);
		try {
			type.setValue(field, value);
		} catch (ClassNotLoadedException e) {
			assertTrue("2", false);
		} catch (InvalidTypeException e) {
			assertTrue("3", false);
		}

		// Resume the test thread
		ThreadReference thread = getThread();
		int suspendCount = thread.suspendCount();
		for (int i = 0; i < suspendCount; i++)
			thread.resume();
	}
	/**
	 * Triggers a step event and waits for it to come in.
	 */
	protected StepEvent triggerStepAndWait() {
		return triggerStepAndWait(
			getThread(),
			StepRequest.STEP_MIN,
			StepRequest.STEP_OVER);
	}

	protected StepEvent triggerStepAndWait(
		ThreadReference thread,
		int gran,
		int depth) {
		// Request for step events
		EventRequest eventRequest =
			fVM.eventRequestManager().createStepRequest(thread, gran, depth);
		eventRequest.addCountFilter(1);
		eventRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		eventRequest.enable();

		return triggerStepAndWait(thread, eventRequest, TIMEOUT);
	}

	protected StepEvent triggerStepAndWait(
		ThreadReference thread,
		EventRequest eventRequest,
		int timeout) {
		// Prepare to receive the event
		EventWaiter waiter = new EventWaiter(eventRequest, true);
		fEventReader.addEventListener(waiter);

		// Trigger step event
		int suspendCount = thread.suspendCount();
		for (int i = 0; i < suspendCount; i++)
			thread.resume();

		// Wait for the event to come in
		StepEvent event = (StepEvent) waitForEvent(waiter, timeout);
		fEventReader.removeEventListener(waiter);
		if (event == null)
			throw new Error("StepEvent didn't come in after " + timeout + "ms");

		// Stop getting step events
		fVM.eventRequestManager().deleteEventRequest(eventRequest);

		// Wait for the program to be ready
		waitUntilReady();

		return event;
	}
	/**
	 * Output verbose string if asked for.
	 */
	protected void verbose(String verboseString) {
		if (fVerbose)
			System.out.println(verboseString);
	}
	/**
	 * Waits for an event to come in using the given waiter.
	 * Waits for the given time. If it times out, returns null.
	 */
	protected Event waitForEvent(EventWaiter waiter, long time) {
		Event event;
		try {
			event = waiter.waitEvent(time);
		} catch (InterruptedException e) {
			event = null;
		}
		return event;
	}
	/**
	 * Waits until the program is ready to be tested.
	 * The default behavior is to wait until the "Test Thread" throws and catches
	 * an exception.
	 */
	protected void waitUntilReady() {
		// Make sure the program is running
		ThreadReference thread = getThread();
		while (thread == null || thread.suspendCount() > 0) {
			fVM.resume();
			thread = getThread();
		}

		// Create exception request
		EventRequest request =
			fVM.eventRequestManager().createExceptionRequest(null, true, false);
		request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);

		// Prepare to receive the event
		EventWaiter waiter = new EventWaiter(request, false);
		fEventReader.addEventListener(waiter);

		request.enable();

		while (true) {
			// Wait for the event to come in
			ExceptionEvent event = (ExceptionEvent) waitForEvent(waiter, TIMEOUT);

			// Throw error if event is null
			if (event == null)
				throw new Error("Target program was not ready after " + TIMEOUT + "ms");

			// Get the method where the exception was thrown
			Method meth = event.location().method();
			if (meth == null || !meth.name().equals("printAndSignal"))
				fVM.resume();
			else
				break;
		}

		// Disable request
		fEventReader.removeEventListener(waiter);
		fVM.eventRequestManager().deleteEventRequest(request);
	}
}
