package org.eclipse.debug.jdi.tests.program;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
/**
 * Main class for target VM tests.
 * This class is intended to be run by the target VM. 
 * It will use other classes in this package, and it will create and terminate
 * threads as a regular program would do.
 *
 * WARNING, WARNING:
 * Tests in org.eclipse.debug.jdi.tests assume the content of this class, 
 * as well as its behavior. So if this class or one of the types in this
 * package is changed, the corresponding tests must also be changed.
 */
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;

public class MainClass extends Date implements Runnable, Printable {

	private static byte[] byteArray = new byte[0];
	private static byte[][] byteDoubleArray = new byte[0][0];
	private static short[] shortArray = new short[0];
	private static short[][] shortDoubleArray = new short[0][0];
	private static int[] intArray = new int[0];
	private static int[][] intDoubleArray = new int[0][0];
	private static long[] longArray = new long[0];
	private static long[][] longDoubleArray = new long[0][0];
	private static double[] doubleArray = new double[0];
	private static double[][] doubleDoubleArray = new double[0][0];
	private static float[] floatArray = new float[0];
	private static float[][] floatDoubleArray = new float[0][0];
	private static char[] charArray = new char[0];
	private static char[][] charDoubleArray = new char[0][0];
	private static boolean[] booleanArray = new boolean[0];
	private static boolean[][] booleanDoubleArray = new boolean[0][0];

	private String string = "";
	private String[] stringArray = new String[0];
	private String[][] stringDoubleArray = new String[0][0];

	public static int fInt = 0;

	public static MainClass fObject = new MainClass();

	public static String fString = "Hello World";

	public static Thread fThread;

	public static Thread fMainThread;

	public static String[] fArray = new String[] { "foo", "bar", "hop" };

	public static double[] fDoubleArray = new double[] { 1, 2.2, 3.33 };

	public static String fEventType = "";

	public boolean fBool = false;

	private char fChar = 'a';

	private String fString2 = "Hello";

	protected final String fString3 = "HEY";

	public MainClass() {
	}

	/* Used to test ClassType.newInstance */
	public MainClass(int i, Object o1, Object o2) {
	}

	/* For invocation tests */
	private static String invoke1(int x, Object o) {
		if (o == null)
			return (new Integer(x)).toString();
		else
			return "";
	}
	/* For invocation tests */
	private static void invoke2() {
		throw new IndexOutOfBoundsException();
	}
	/* For invocation tests */
	private int invoke3(String str, Object o) {
		return Integer.parseInt(str);
	}
	/* For invocation tests */
	private long invoke4() throws java.io.EOFException {
		throw new java.io.EOFException();
	}
	/* For variables test */
	private void variablesTest(long l) {
	}
	public static void main(java.lang.String[] args) {
		// Start the test program
		ThreadGroup group = new ThreadGroup("Test ThreadGroup");
		fThread = new Thread(group, fObject, "Test Thread");
		fThread.start();

		fMainThread = Thread.currentThread();

		// Prevent this thread from dying
		while (true)
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
	}
	public void print(OutputStream out) {
		String string = fInt++ +". " + fString;
		PrintWriter writer = new PrintWriter(out);
		writer.println(string);
		writer.flush();
	}
	synchronized public void printAndSignal() {
		print(System.out);

		// Signal readiness by throwing an exception
		try {
			throw new NegativeArraySizeException();
		} catch (NegativeArraySizeException exc) {
		}
	}
	public void run() {
		try {
			Thread t = Thread.currentThread();
			MainClass o = new OtherClass();
			// The front-end tests use the class load event to determine that the program has started

			if (CONSTANT == 2)
				System.out.println("CONSTANT=2");

			while (true) {
				printAndSignal();
				triggerEvent();
				useLocalVars(t, o);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		} finally {
			System.out.println("Running finally block in MainClass.run()");
		}
	}
	/**
	 *	Trigger an access watchpoint event for the front-end.
	 */
	private void triggerAccessWatchpointEvent() {
		if (fBool)
			System.out.println("fBool is true");
	}
	/**
	 *	Trigger a breakpoint event for the front-end.
	 */
	private void triggerBreakpointEvent() {
		System.out.println("Breakpoint");
	}
	/**
	 *	Trigger a class prepare event for the front-end.
	 */
	private void triggerClassPrepareEvent() {
		new TestClass();
	}
	/**
	 *	Trigger a class prepare event for the front-end.
	 */
	private void triggerClassPrepareEvent1() {
		new TestClass1();
	}
	/**
	 *	Trigger a class prepare event for the front-end.
	 */
	private void triggerClassPrepareEvent2() {
		new TestClass2();
	}
	/**
	 *	Trigger a class prepare event for the front-end.
	 */
	private void triggerClassPrepareEvent3() {
		new TestClass3();
	}
	/**
	 *	Trigger a class prepare event for the front-end.
	 */
	private void triggerClassPrepareEvent4() {
		new TestClass4();
	}
	/**
	 *	Trigger a class prepare event for the front-end.
	 */
	private void triggerClassPrepareEvent5() {
		new TestClass5();
	}
	/**
	 *	Trigger a class prepare event for the front-end.
	 */
	private void triggerClassPrepareEvent6() {
		new TestClass6();
	}
	/**
	 *	Trigger a class prepare event for the front-end.
	 */
	private void triggerClassPrepareEvent7() {
		new TestClass7();
		new TestClazz8();
	}
	/**
	 *	Trigger a class prepare event for the front-end.
	 */
	private void triggerClassPrepareEvent8() {
		new TestClazz9();
		new TestClazz10();
	}
	/**
	 *	Trigger an event for the front-end.
	 */
	private void triggerEvent() {
		/* Ensure we do it only once */
		String eventType = fEventType;
		fEventType = "";

		/* Trigger event according to the field fEventType */
		if (eventType.equals(""))
			return;
		else if (eventType.equals("AccessWatchpointEvent"))
			triggerAccessWatchpointEvent();
		else if (eventType.equals("StaticAccessWatchpointEvent"))
			triggerStaticAccessWatchpointEvent();
		else if (eventType.equals("BreakpointEvent"))
			triggerBreakpointEvent();
		else if (eventType.equals("ClassPrepareEvent"))
			triggerClassPrepareEvent();
		else if (eventType.equals("ClassPrepareEvent1"))
			triggerClassPrepareEvent1();
		else if (eventType.equals("ClassPrepareEvent2"))
			triggerClassPrepareEvent2();
		else if (eventType.equals("ClassPrepareEvent3"))
			triggerClassPrepareEvent3();
		else if (eventType.equals("ClassPrepareEvent4"))
			triggerClassPrepareEvent4();
		else if (eventType.equals("ClassPrepareEvent5"))
			triggerClassPrepareEvent5();
		else if (eventType.equals("ClassPrepareEvent6"))
			triggerClassPrepareEvent6();
		else if (eventType.equals("ClassPrepareEvent7"))
			triggerClassPrepareEvent7();
		else if (eventType.equals("ClassPrepareEvent8"))
			triggerClassPrepareEvent8();
		else if (eventType.equals("ExceptionEvent"))
			triggerExceptionEvent();
		else if (eventType.equals("ModificationWatchpointEvent"))
			triggerModificationWatchpointEvent();
		else if (eventType.equals("StaticModificationWatchpointEvent"))
			triggerStaticModificationWatchpointEvent();
		else if (eventType.equals("ThreadStartEvent"))
			triggerThreadStartEvent();
		else if (eventType.equals("ThreadDeathEvent"))
			triggerThreadDeathEvent();
		else
			System.out.println("Unknown event type: " + eventType);
	}
	/**
	 *	Trigger an exception event for the front-end.
	 */
	private void triggerExceptionEvent() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				throw new Error();
			}
		}, "Test Exception Event");
		t.start();
	}
	/**
	 *	Trigger a modification watchpoint event for the front-end.
	 */
	private void triggerModificationWatchpointEvent() {
		fBool = true;
	}
	/**
	 * Trigger an access watchpoint event to a static field
	 * for the front-end.
	 */
	private void triggerStaticAccessWatchpointEvent() {
		if (fObject == null)
			System.out.println("fObject is null");
	}
	/**
	 * Trigger a modification watchpoint event of a static field
	 * for the front-end.
	 */
	private void triggerStaticModificationWatchpointEvent() {
		fString = "Hello Universe";
	}
	/**
	 *	Trigger a thread end event for the front-end.
	 */
	private void triggerThreadDeathEvent() {
		new Thread("Test Thread Death Event").start();
	}
	/**
	 *	Trigger a thread start event for the front-end.
	 */
	private void triggerThreadStartEvent() {
		new Thread("Test Thread Start Event").start();
	}
	private void useLocalVars(Thread t, MainClass o) {
		if (t == null)
			System.out.println("t is null");
		if (o == null)
			System.out.println("o is null");

	}
}
