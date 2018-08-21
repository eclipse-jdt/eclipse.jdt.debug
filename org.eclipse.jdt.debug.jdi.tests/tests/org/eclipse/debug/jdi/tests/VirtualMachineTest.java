/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import junit.framework.Test;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;

/**
 * Tests for JDI com.sun.jdi.VirtualMachine
 * and JDWP VM command set.
 *
 * Example of arguments:
 *   -launcher SunVMLauncher -address c:\jdk1.2.2\ -classpath d:\target
 */
public class VirtualMachineTest extends AbstractJDITest {

	/**
	 * Creates a new test .
	 */
	public VirtualMachineTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new VirtualMachineTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.VirtualMachine";
	}
	/**
	 * Don't start the program yet, so that the testNotStarted* tests can run before.
	 */
	@Override
	protected void setUp() {
		launchTargetAndConnectToVM();
	}
	/**
	 * Starts the target program.
	 * NB: This method is copied in this class only so that it can be invoked
	 *     dynamically.
	 */
	@Override
	public void startProgram() {
		super.startProgram();
	}
	/**
	 * Returns all tests
	 */
	@Override
	protected Test suite() {
		JDITestSuite suite = new JDITestSuite(this);

		// Tests that run before the program is started
		Vector<?> testNames = getAllMatchingTests("testNotStarted");
		Iterator<?> iterator = testNames.iterator();
		while (iterator.hasNext()) {
			String name = (String) iterator.next();
			suite.addTest(new JDITestCase(this, name));
		}

		// The method that starts the program
		suite.addTest(new JDITestCase(this, "startProgram"));

		// Tests that run after the program has started
		testNames = getAllMatchingTests("testStarted");
		iterator = testNames.iterator();
		while (iterator.hasNext()) {
			String name = (String) iterator.next();
			suite.addTest(new JDITestCase(this, name));
		}

		// All other tests
		testNames = getAllMatchingTests("testJDI");
		iterator = testNames.iterator();
		while (iterator.hasNext()) {
			String name = (String) iterator.next();
			suite.addTest(new JDITestCase(this, name));
		}

		return suite;
	}
	/**
	 * Test JDI canGetBytecodes().
	 */
	public void testJDICanGetBytecodes() {
		fVM.canGetBytecodes();
	}
	/**
	 * Test JDI canGetCurrentContendedMonitor().
	 */
	public void testJDICanGetCurrentContendedMonitor() {
		fVM.canGetCurrentContendedMonitor();
	}
	/**
	 * Test JDI canGetMonitorInfo().
	 */
	public void testJDICanGetMonitorInfo() {
		fVM.canGetMonitorInfo();
	}
	/**
	 * Test JDI canGetOwnedMonitorInfo().
	 */
	public void testJDICanGetOwnedMonitorInfo() {
		fVM.canGetOwnedMonitorInfo();
	}
	/**
	 * Test JDI canGetSyntheticAttribute().
	 */
	public void testJDICanGetSyntheticAttribute() {
		//  This is optional functionality, thus this is not a failure
		fVM.canGetSyntheticAttribute();
	}
	/**
	 * Test JDI canWatchFieldAccess().
	 */
	public void testJDICanWatchFieldAccess() {
		//  This is optional functionality, thus this is not a failure
		fVM.canWatchFieldAccess();
	}
	/**
	 * Test JDI canWatchFieldModification().
	 */
	public void testJDICanWatchFieldModification() {
		//  This is optional functionality, thus this is not a failure
		fVM.canWatchFieldModification();
	}
	/**
	 * Test JDI eventQueue().
	 */
	public void testJDIEventQueue() {
		assertNotNull("1", fVM.eventQueue());
	}
	/**
	 * Test JDI eventRequestManager().
	 */
	public void testJDIEventRequestManager() {
		assertNotNull("1", fVM.eventRequestManager());
	}
	/**
	 * Test JDI mirrorOf(boolean).
	 */
	public void testJDIMirrorOfBoolean() {
		boolean value = true;
		BooleanValue mirror = fVM.mirrorOf(value);
		assertTrue("1", value == mirror.value());
	}
	/**
	 * Test JDI mirrorOf(byte).
	 */
	public void testJDIMirrorOfByte() {
		byte value = 1;
		ByteValue mirror = fVM.mirrorOf(value);
		assertEquals("1", value, mirror.value());
	}
	/**
	 * Test JDI mirrorOf(char).
	 */
	public void testJDIMirrorOfChar() {
		char value = 'a';
		CharValue mirror = fVM.mirrorOf(value);
		assertEquals("1", value, mirror.value());
	}
	/**
	 * Test JDI mirrorOf(double).
	 */
	public void testJDIMirrorOfDouble() {
		double value = 12345.6789;
		DoubleValue mirror = fVM.mirrorOf(value);
		assertEquals("1", value, mirror.value(), 0);
	}
	/**
	 * Test JDI mirrorOf(float).
	 */
	public void testJDIMirrorOfFloat() {
		float value = 12345.6789f;
		FloatValue mirror = fVM.mirrorOf(value);
		assertEquals("1", value, mirror.value(), 0);
	}
	/**
	 * Test JDI mirrorOf(int).
	 */
	public void testJDIMirrorOfInt() {
		int value = 12345;
		IntegerValue mirror = fVM.mirrorOf(value);
		assertEquals("1", value, mirror.value());
	}
	/**
	 * Test JDI mirrorOf(long).
	 */
	public void testJDIMirrorOfLong() {
		long value = 1234567890l;
		LongValue mirror = fVM.mirrorOf(value);
		assertEquals("1", value, mirror.value());
	}
	/**
	 * Test JDI mirrorOf(short).
	 */
	public void testJDIMirrorOfShort() {
		short value = (short) 12345;
		ShortValue mirror = fVM.mirrorOf(value);
		assertEquals("1", value, mirror.value());
	}
	/**
	 * Test JDI mirrorOf(String) and JDWP 'VM - Create String'.
	 */
	public void testJDIMirrorOfString() {
		String testString = "Test";
		StringReference newString = null;
		newString = fVM.mirrorOf(testString);
		assertEquals("1", newString.value(), testString);
	}
	/**
	 * Test JDI setDebugTraceMode(int).
	 */
	public void testJDISetDebugTraceMode() {
		fVM.setDebugTraceMode(com.sun.jdi.VirtualMachine.TRACE_ALL);
		fVM.setDebugTraceMode(com.sun.jdi.VirtualMachine.TRACE_SENDS);
		fVM.setDebugTraceMode(com.sun.jdi.VirtualMachine.TRACE_RECEIVES);
		fVM.setDebugTraceMode(com.sun.jdi.VirtualMachine.TRACE_NONE);

		// restore original value
		fVM.setDebugTraceMode(fVMTraceFlags);
	}
	/**
	 * Test JDI getVersion().
	 */
	public void testJDIVersion() {
		String version = fVM.version();
		assertTrue("1", version != null);
	}
	/**
	 * Test JDI allClasses() and JDWP 'VM - Get all classes'
	 * while the test program has not been started.
	 */
	public void testNotStartedAllClasses() {
		List<?> classes = fVM.allClasses();
		Iterator<?> iterator = classes.listIterator();
		int i = 0;
		while (iterator.hasNext())
			assertTrue(Integer.toString(i++), iterator.next() instanceof ReferenceType);
	}
	/**
	 * Test JDI allThreads() and JDWP 'VM - Get all threads'
	 * while the test program has not been started.
	 */
	public void testNotStartedAllThreads() {
		List<?> threads = fVM.allThreads();
		Iterator<?> iterator = threads.listIterator();
		int i = 0;
		while (iterator.hasNext())
			assertTrue(Integer.toString(i++), iterator.next() instanceof ThreadReference);
	}
	/**
	 * Test JDI classesByName() while the test program has not been started.
	 */
	public void testNotStartedClassesByName() {
		List<?> classes = fVM.classesByName("java.lang.Object");
		assertEquals("1", 1, classes.size());
	}
	/**
	 * Test JDI allClasses() and JDWP 'VM- Get all classes'
	 * once the test program has been started.
	 */
	public void testStartedAllClasses() {

		// The test program has started, the number of classes is != 0
		List<?> classes = fVM.allClasses();
		assertTrue("1", classes.size() != 0);

		// Collect names of received classes
		String[] names = new String[classes.size()];
		ListIterator<?> iterator = classes.listIterator();
		int i = 0;
		while (iterator.hasNext()) {
			ReferenceType type = (ReferenceType) iterator.next();
			names[i++] = type.name();
		}

		// Check that they are the expected names
		String[] expected =
			new String[] {
				"java.lang.Object",
				"java.util.Date",
				"org.eclipse.debug.jdi.tests.program.Printable",
				"org.eclipse.debug.jdi.tests.program.MainClass" };
		for (int j = 0; j < expected.length; j++) {
			boolean isIncluded = false;
			iteration : for (int k = 0; k < names.length; k++) {
				if (names[k].equals(expected[j])) {
					isIncluded = true;
					break iteration;
				}
			}
			assertTrue("2." + j, isIncluded);
		}
	}
	/**
	 * Test JDI allThreads() and JDWP 'VM - Get all threads'
	 * once the test program has been started.
	 */
	public void testStartedAllThreads() {

		// The test program has started, the number of threads is != 0
		List<?> threads = fVM.allThreads();
		assertTrue("1", threads.size() != 0);

		// Collect names of received threads
		String[] names = new String[threads.size()];
		ListIterator<?> iterator = threads.listIterator();
		int i = 0;
		while (iterator.hasNext()) {
			ThreadReference thread = (ThreadReference) iterator.next();
			names[i++] = thread.name();
		}

		// Check that they contain at least the expected names
		String[] expected = new String[] { "Test Thread" };
		boolean isIncluded = false;
		iteration : for (int j = 0; j < expected.length; j++) {
			for (int k = 0; k < names.length; k++) {
				if (expected[j].equals(names[k])) {
					isIncluded = true;
					break iteration;
				}
			}
		}
		assertTrue("2", isIncluded);
	}
	/**
	 * Test JDI classesByName() once the test program has been started.
	 */
	public void testStartedClassesByName() {

		// The test program has started, the number of java.lang.Object is 1
		List<?> classes = fVM.classesByName("java.lang.Object");
		assertEquals("1", classes.size(), 1);

		// Collect names of received classes
		String[] names = new String[classes.size()];
		ListIterator<?> iterator = classes.listIterator();
		int i = 0;
		while (iterator.hasNext()) {
			ReferenceType type = (ReferenceType) iterator.next();
			names[i++] = type.name();
		}

		// Check that they are all "java.lang.Object"
		for (int j = 0; j < names.length; j++) {
			assertEquals("2." + j, "java.lang.Object", names[j]);
		}
	}
	/**
	 * Test JDI suspend() and resume() once the test program has been started.
	 */
	public void testStartedSuspendResume() {
		// Suspend
		fVM.suspend();
		ListIterator<?> threads = fVM.allThreads().listIterator();
		while (threads.hasNext()) {
			ThreadReference thread = (ThreadReference) threads.next();
			assertTrue("1." + thread.name(), thread.isSuspended());
		}

		// Resume
		fVM.resume();
		// Cannot assertTrue that all threads are not suspended because they might have been suspended
		// by the program itself

		// Suspend VM and suspend one thread
		fVM.suspend();
		threads = fVM.allThreads().listIterator();
		ThreadReference suspended = getThread();
		suspended.suspend();
		while (threads.hasNext()) {
			ThreadReference thread = (ThreadReference) threads.next();
			assertTrue("2." + thread.name(), thread.isSuspended());
		}

		// Resume VM and ensure that the one thread that was suspended is still suspended
		fVM.resume();
		assertTrue("3", suspended.isSuspended());
	}
	/**
	 * Test JDI topLevelThreadGroups().
	 */
	public void testStartedTopLevelThreadGroups() {
		List<?> topLevelThreadGroups = fVM.topLevelThreadGroups();
		assertEquals("1", 1, topLevelThreadGroups.size());
		assertTrue("2", topLevelThreadGroups.get(0) instanceof ThreadGroupReference);
	}
}
