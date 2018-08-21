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

import java.util.List;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.ThreadStartEvent;

/**
 * Tests for JDI com.sun.jdi.ThreadReference
 * and JDWP Thread command set.
 */
public class ThreadReferenceTest extends AbstractJDITest {

	private ThreadReference fThread;
	/**
	 * Creates a new test .
	 */
	public ThreadReferenceTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get thread
		fThread = getThread();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new ThreadReferenceTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.ThreadReference";
	}
	/**
	 * Test JDI currentContendedMonitor().
	 */
	public void testJDICurrentContendedMonitor() {
		if (fVM.canGetCurrentContendedMonitor()) {
			try {
				assertTrue("1", fThread.currentContendedMonitor() == null);
			} catch (IncompatibleThreadStateException e) {
				assertTrue("2", false);
			}
		}
	}
	/**
	 * Test JDI frame(int).
	 */
	public void testJDIFrame() {
		try {
			StackFrame frame = fThread.frame(0);
			assertTrue("1", fThread.frames().contains(frame));
		} catch (IncompatibleThreadStateException e) {
			assertTrue("2", false);
		}
	}
	/**
	 * Test JDI frameCount.
	 */
	public void testJDIFrameCount() {
		try {
			int count = fThread.frameCount();
			assertTrue("1", count <= 4);
		} catch (IncompatibleThreadStateException e) {
			assertTrue("2", false);
		}
	}
	/**
	 * Test JDI frames() and JDWP 'Thread - Get frames'.
	 */
	public void testJDIFrames() {
		List<?> frames = null;
		try {
			frames = fThread.frames();
		} catch (IncompatibleThreadStateException e) {
			assertTrue("1", false);
		}
		assertTrue("2", frames.size() > 0);
	}
	/**
	 * Test JDI interrupt()().
	 */
	public void testJDIInterrupt() {
		assertEquals("1", 1, fThread.suspendCount());
		fThread.interrupt();
		assertEquals("2", 1, fThread.suspendCount());
	}
	/**
	 * Test JDI isAtBreakpoint().
	 */
	public void testJDIIsAtBreakpoint() {
		assertTrue("1", !fThread.isAtBreakpoint());
	}
	/**
	 * Test JDI isSuspended().
	 */
	public void testJDIIsSuspended() {
		assertTrue("1", fThread.isSuspended());
	}
	/**
	 * Test JDI name() and JDWP 'Thread - Get name'.
	 */
	public void testJDIName() {
		assertEquals("1", "Test Thread", fThread.name());
	}
	/**
	 * Test JDI ownedMonitors().
	 */
	public void testJDIOwnedMonitors() {
		if (fVM.canGetOwnedMonitorInfo()) {
			waitUntilReady();
			try {
				assertEquals("1", 1, fThread.ownedMonitors().size());
			} catch (IncompatibleThreadStateException e) {
				assertTrue("2", false);
			}
		}
	}
	/**
	 * Test JDI status() and JDWP 'Thread - Get status'.
	 */
	public void testJDIStatus() {
		int status = fThread.status();
		assertTrue(
			"1",
			((status == ThreadReference.THREAD_STATUS_RUNNING)
				|| (status == ThreadReference.THREAD_STATUS_SLEEPING)
				|| (status == ThreadReference.THREAD_STATUS_WAIT)));
	}
	/**
	 * Test JDI stop(ObjectReference).
	 */
	public void testJDIStop() {
		// Make sure the entire VM is not suspended before we start a new thread
		// (otherwise this new thread will start suspended and we will never get the
		// ThreadStart event)
		fVM.resume();

		// Trigger a thread start event to get a new thread
		ThreadStartEvent event =
			(ThreadStartEvent) triggerAndWait(fVM
				.eventRequestManager()
				.createThreadStartRequest(),
				"ThreadStartEvent",
				false);
		ThreadReference thread = event.thread();

		// Create a java.lang.Throwable instance in
		List<ReferenceType> classes = fVM.classesByName("java.lang.Throwable");
		assertTrue("1", classes.size() != 0);
		ClassType threadDeathClass = (ClassType) classes.get(0);
		Method constructor =
			threadDeathClass.concreteMethodByName("<init>", "()V");
		ObjectReference threadDeath = null;
		try {
			threadDeath =
				threadDeathClass.newInstance(
					thread,
					constructor,
					new java.util.LinkedList<Value>(),
					ClassType.INVOKE_SINGLE_THREADED);
			threadDeath.disableCollection();
			// This object is going to be used for the lifetime of the VM.
		} catch (ClassNotLoadedException e) {
			assertTrue("2", false);
		} catch (InvalidTypeException e) {
			assertTrue("3", false);
		} catch (InvocationException e) {
			assertTrue("4", false);
		} catch (IncompatibleThreadStateException e) {
			assertTrue("5", false);
		}

		// Stop the thread
		try {
			thread.stop(threadDeath);
		} catch (InvalidTypeException e) {
			assertTrue("6", false);
		}

		waitUntilReady();

	}
	/**
	 * Test JDI suspend() and resume()
	 * and JDWP 'Thread - Suspend' and 'Thread - Resume'.
	 */
	public void testJDISuspendResume() {
		assertEquals("1", 1, fThread.suspendCount());
		fThread.resume();
		assertTrue("2", !fThread.isSuspended());
		fThread.suspend();
		assertTrue("3", fThread.isSuspended());

		waitUntilReady();
	}
	/**
	 * Test JDI threadGroup() and JDWP 'Thread - Get threadGroup'.
	 */
	public void testJDIThreadGroup() {
		assertNotNull("1", fThread.threadGroup());
	}
}
