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
package org.eclipse.debug.jdi.tests.program;


import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;

import org.eclipse.debug.jdi.tests.ClassTypeTest;

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

	/**
	 * An integer value
	 */
	public static int fInt = 0;

	/**
	 * The instance of the <code>MainClass</code>
	 */
	public static MainClass fObject = new MainClass();

	/**
	 * A string initialized to 'hello world'
	 */
	public static String fString = "Hello World";

	/**
	 * A <code>Thread</code> object
	 */
	public static Thread fThread;

	/**
	 * A <code>Thread</code> object representing the main thread
	 */
	public static Thread fMainThread;

	/**
	 */
	public static String[] fArray = new String[] { "foo", "bar", "hop" };

	/**
	 * A pre-initialized array of doubles
	 */
	public static double[] fDoubleArray = new double[] { 1, 2.2, 3.33 };

	/**
	 * The name of an event type
	 */
	public static String fEventType = "";

	/**
	 * A boolean value initialized to <code>false</code>
	 */
	public boolean fBool = false;

	private char fChar = 'a';

	private String fString2 = "Hello";

	protected final String fString3 = "HEY";

	/**
	 * Constructor
	 */
	public MainClass() {}

	/**
	 * Constructor
	 * Used to test ClassType.newInstance
	 * @see ClassTypeTest
	 * @param i
	 * @param o1
	 * @param o2
	 */
	public MainClass(int i, Object o1, Object o2) {
	}

	/**
	 * For invocation tests
	 * @param x the integer
	 * @param o
	 * @return a string object representing the specified interger value
	 */
	private static String invoke1(int x, Object o) {
		if (o == null) {
			return (new Integer(x)).toString();
		} else {
			return "";
		}
	}
	/**
	 * For invocation tests
	 */
	private static void invoke2() {
		throw new IndexOutOfBoundsException();
	}

	/**
	 * For invocation tests
	 * @param str
	 * @param o
	 * @return an the integer value of the specified string
	 */
	private int invoke3(String str, Object o) {
		return Integer.parseInt(str);
	}
	/**
	 * For invocation tests
	 * @return nothing, only throws an exception
	 * @throws java.io.EOFException
	 */
	private long invoke4() throws java.io.EOFException {
		throw new java.io.EOFException();
	}
	/**
	 * For variables test
	 * @param l
	 */
	private void variablesTest(long l) {}

	/**
	 * Runs the test program
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		// Start the test program
		ThreadGroup group = new ThreadGroup("Test ThreadGroup");
		fThread = new Thread(group, fObject, "Test Thread");
		fThread.start();

		fMainThread = Thread.currentThread();

		// Prevent this thread from dying
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}
	/**
	 * @see org.eclipse.debug.jdi.tests.program.Printable#print(java.io.OutputStream)
	 */
	@Override
	public void print(OutputStream out) {
		String string = fInt++ +". " + fString;
		PrintWriter writer = new PrintWriter(out);
		writer.println(string);
		writer.flush();
	}

	/**
	 * Prints out a specified integer. This method is used in the force early return tests to ensure we
	 * can specify a different, type compatible return value
	 * @param out a stream to print out to
	 * @param num the number we want to print and return
	 * @return the specified number parameter, just pass it through
	 * @since 3.3
	 */
	public int printNumber(OutputStream out, int num) {
		String blah = "foo"+foo();
		PrintWriter writer = new PrintWriter(out);
		writer.println("The specified number is: "+num);
		writer.flush();
		return num;
	}

	/**
	 * Returns 20
	 *
	 * @return 20
	 */
	public int getInt() {
		int x = Math.max(20, 10);
		return x;
	}

	/**
	 * Returns true.
	 *
	 * @return true
	 */
	public boolean getBoolean() {
		boolean bool = Boolean.valueOf("true").booleanValue();
		return bool;
	}

	/**
	 * Returns 123L.
	 *
	 * @return 123
	 */
	public long getLong() {
		long l = Long.valueOf("123").longValue();
		return l;
	}

	/**
	 * dump out a string
	 * @return a String
	 * @since 3.3
	 */
	public String foo() {
		System.out.println("foobar");
		return "man";
	}

	/**
	 * make a sync'd method so we can stop in it to gather monitor information
	 * @since 3.3
	 */
	synchronized public void sync() {
		System.out.println("sync'd to the moon");
	}

	/**
	 * suspend on the first line of the method to get the argument values from the stackframe.
	 * used in testing the new 1.6VM capability to get argument values when no debugging info is available.
	 * @param str a string
	 * @param num a number
	 * @param obj an object
	 * @since 3.3
	 */
	public void argValues(String str, int num, Object obj) {
		System.out.println("get the arg values");
	}

	/**
	 * Prints to System.out and throws an exception to indicate readiness
	 */
	synchronized public void printAndSignal() {
		print(System.out);
		// Signal readiness by throwing an exception
		try {
			throw new NegativeArraySizeException();
		} catch (NegativeArraySizeException exc) {
		}
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			Thread t = Thread.currentThread();
			MainClass o = new OtherClass();
			// The front-end tests use the class load event to determine that the program has started

			if (CONSTANT == 2) {
				System.out.println("CONSTANT=2");
			}

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
		if (fBool) {
			System.out.println("fBool is true");
		}
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
		if (eventType.equals("")) {
			return;
		} else if(eventType.equals("refclassload")) {
			new RefClass();
		}
		else if(eventType.equals("fooreturn")) {
			foo();
		}
		else if(eventType.equals("argvalues")) {
			argValues("teststr", 5, new Double(1.33));
		}
		else if(eventType.equals("forcereturn2")) {
			printNumber(System.out, 1);
		}
		else if(eventType.equals("forcereturn")) {
			print(System.out);
		}
		else if(eventType.equals("monitorinfo")) {
			sync();
		}
		else if(eventType.equals("refclass1load")) {
			new RefClass1();
		}
		else if(eventType.equals("refclass2load")) {
			new RefClass2();
		}
		else if(eventType.equals("refclass3load")) {
			new RefClass3();
		}
		else if(eventType.equals("refclass4load")) {
			new RefClass4();
		} else if (eventType.equals("getInt")) {
			getInt();
		} else if (eventType.equals("getBoolean")) {
			getBoolean();
		} else if (eventType.equals("getLong")) {
			getLong();
		} else if (eventType.equals("AccessWatchpointEvent")) {
			triggerAccessWatchpointEvent();
		} else if (eventType.equals("StaticAccessWatchpointEvent")) {
			triggerStaticAccessWatchpointEvent();
		} else if (eventType.equals("BreakpointEvent")) {
			triggerBreakpointEvent();
		} else if (eventType.equals("ClassPrepareEvent")) {
			triggerClassPrepareEvent();
		} else if (eventType.equals("ClassPrepareEvent1")) {
			triggerClassPrepareEvent1();
		} else if (eventType.equals("ClassPrepareEvent2")) {
			triggerClassPrepareEvent2();
		} else if (eventType.equals("ClassPrepareEvent3")) {
			triggerClassPrepareEvent3();
		} else if (eventType.equals("ClassPrepareEvent4")) {
			triggerClassPrepareEvent4();
		} else if (eventType.equals("ClassPrepareEvent5")) {
			triggerClassPrepareEvent5();
		} else if (eventType.equals("ClassPrepareEvent6")) {
			triggerClassPrepareEvent6();
		} else if (eventType.equals("ClassPrepareEvent7")) {
			triggerClassPrepareEvent7();
		} else if (eventType.equals("ClassPrepareEvent8")) {
			triggerClassPrepareEvent8();
		} else if (eventType.equals("ExceptionEvent")) {
			triggerExceptionEvent();
		} else if (eventType.equals("ModificationWatchpointEvent")) {
			triggerModificationWatchpointEvent();
		} else if (eventType.equals("StaticModificationWatchpointEvent")) {
			triggerStaticModificationWatchpointEvent();
		} else if (eventType.equals("ThreadStartEvent")) {
			triggerThreadStartEvent();
		} else if (eventType.equals("ThreadDeathEvent")) {
			triggerThreadDeathEvent();
		} else {
			System.out.println("Unknown event type: " + eventType);
		}
	}

	/**
	 *	Trigger an exception event for the front-end.
	 */
	private void triggerExceptionEvent() {
		Thread t = new Thread(new Runnable() {
			@Override
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
		if (fObject == null) {
			System.out.println("fObject is null");
		}
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
		if (t == null) {
			System.out.println("t is null");
		}
		if (o == null) {
			System.out.println("o is null");
		}

	}
}
