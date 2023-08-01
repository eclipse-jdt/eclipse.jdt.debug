/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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

import java.lang.reflect.InvocationTargetException;
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
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;

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
				assertNull("1", fThread.currentContendedMonitor());
			} catch (IncompatibleThreadStateException e) {
				fail("2");
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
			fail("2");
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
			fail("2");
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
			fail("1");
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
		assertFalse("1", fThread.isAtBreakpoint());
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
				fail("2");
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
		assertFalse("1", classes.isEmpty());
		ClassType threadDeathClass = (ClassType) classes.get(0);
		Method constructor =
			threadDeathClass.concreteMethodByName("<init>", "()V");
		ObjectReference threadDeath = null;
		try {
			threadDeath =
				threadDeathClass.newInstance(
					thread,
					constructor,
					new java.util.LinkedList<>(),
					ClassType.INVOKE_SINGLE_THREADED);
			threadDeath.disableCollection();
			// This object is going to be used for the lifetime of the VM.
		} catch (ClassNotLoadedException e) {
			fail("2");
		} catch (InvalidTypeException e) {
			fail("3");
		} catch (InvocationException e) {
			fail("4");
		} catch (IncompatibleThreadStateException e) {
			fail("5");
		}

		// Stop the thread
		try {
			thread.stop(threadDeath);
		} catch (InvalidTypeException e) {
			fail("6");
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
		assertFalse("2", fThread.isSuspended());
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

	/**
	 * Test JDI addPlatformThreadsOnlyFilter() is skipped in old JDK version (<=18).
	 */
	public void testJDIPlatformThreadsOnlyFilter() {
		// Make sure the entire VM is not suspended before we start a new thread
		// (otherwise this new thread will start suspended and we will never get the
		// ThreadStart event)
		fVM.resume();

		// Trigger a thread start event
		ThreadStartRequest threadStartRequest = fVM.eventRequestManager().createThreadStartRequest();
		try {
			java.lang.reflect.Method method = threadStartRequest.getClass().getMethod("addPlatformThreadsOnlyFilter");
			method.invoke(threadStartRequest);
		} catch (NoSuchMethodException | SecurityException e) {
			fail("1");
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			fail("2");
		}
		ThreadStartEvent startEvent = (ThreadStartEvent) triggerAndWait(threadStartRequest, "ThreadStartEvent", true, 3000);
		assertNotNull("3", startEvent);

		// Trigger a thread death event
		ThreadDeathRequest threadDeathRequest = fVM.eventRequestManager().createThreadDeathRequest();
		try {
			java.lang.reflect.Method method = threadDeathRequest.getClass().getMethod("addPlatformThreadsOnlyFilter");
			method.invoke(threadDeathRequest);
		} catch (NoSuchMethodException | SecurityException e) {
			fail("4");
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			fail("5");
		}
		ThreadDeathEvent deathEvent = (ThreadDeathEvent) triggerAndWait(threadDeathRequest, "ThreadDeathEvent", true, 3000);
		assertNotNull("6", deathEvent);
	}
}
