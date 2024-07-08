/*******************************************************************************
 *  Copyright (c) 2000, 2007 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;

/**
 * Test that a SUSPEND_VM breakpoint suspends all threads
 */
public class SuspendVMBreakpointsTests extends AbstractDebugTest {

	/**
	 * Constructor
	 */
	public SuspendVMBreakpointsTests(String name) {
		super(name);
	}

	/**
	 * Tests that the VM is suspended when the specific line breakpoint is hit
	 */
	public void testSuspendVmLineBreakpoint() throws Exception {
		String typeName = "MultiThreadedLoop";
		IJavaLineBreakpoint bp = createLineBreakpoint(40, typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			verifyAllThreadsSuspended(thread);

			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that the VM is suspended when a specific exception breakpoint is hit
	 */
	public void testSuspendVmExceptionBreakpoint() throws Exception {
		String typeName = "ThrowsNPE";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);
		ex.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("suspended, but not by exception breakpoint", ex ,hit);

			verifyAllThreadsSuspended(thread);

			ex.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that the VM is suspended when a specific access watchpoint is hit
	 */
	public void testSuspendVmAccessWatchpointBreakpoint() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.Watchpoint";

		IJavaWatchpoint wp = createWatchpoint(typeName, "list", true, false);
		wp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			IStackFrame frame = thread.getTopStackFrame();
			assertNotNull("No breakpoint", hit);
			assertTrue("Should be an access", wp.isAccessSuspend(thread.getDebugTarget()));
			assertEquals("Should be line 33", 33, frame.getLineNumber());

			verifyAllThreadsSuspended(thread);

			wp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that the VM is suspended when a specific modification watchpoint is hit
	 */
	public void testSuspendVmModificationWatchpointBreakpoint() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.Watchpoint";

		IJavaWatchpoint wp = createWatchpoint(typeName, "list", false, true);
		wp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			IStackFrame frame = thread.getTopStackFrame();
			assertNotNull("No breakpoint", hit);

			// should be modification
			assertFalse("First hit should be modification", wp.isAccessSuspend(thread.getDebugTarget()));
			// line 27
			assertEquals("Should be on line 30", 30, frame.getLineNumber());

			verifyAllThreadsSuspended(thread);

			wp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that the VM is suspended when a specific method entry breakpoint is hit
	 */
	public void testSuspendVmMethodEntryBreakpoint() throws Exception {
		String typeName = "DropTests";
		IJavaBreakpoint bp = createMethodBreakpoint(typeName, "method4", "()V", true, false);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertTrue("breakpoint was not a method breakpoint", hit instanceof IJavaMethodBreakpoint);

			verifyAllThreadsSuspended(thread);

			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that the VM is suspended when a specific method exit breakpoint is hit
	 */
	public void testSuspendVmMethodExitBreakpoint() throws Exception {
		String typeName = "DropTests";
		IJavaBreakpoint bp = createMethodBreakpoint(typeName, "method1", "()V", false, true);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertTrue("breakpoint was not a method breakpoint", hit instanceof IJavaMethodBreakpoint);

			verifyAllThreadsSuspended(thread);

			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Verifies that all of the threads of the related debug target are suspended
	 */
	protected void verifyAllThreadsSuspended(IJavaThread thread) {
		try {
			IJavaDebugTarget debugTarget = (IJavaDebugTarget)thread.getDebugTarget();
			IThread[] threads = debugTarget.getThreads();
			for (int i = 0; i < threads.length; i++) {
				assertTrue("Thread wasn't suspended when a SUSPEND_VM breakpoint was hit, thread=" + threads[i].getName(), threads[i].isSuspended());
			}
		} catch (DebugException e) {
			fail(e.getMessage());
		}
	}


	/**
	 * Tests when multiple {@code IJavaBreakpoint#SUSPEND_VM} are installed and user resumes the execution each time by selecting
	 * {@code IJavaThread#resume()}. This is because default selection is Thread where Breakpoint is hit in Debug View. At the end Debug Target is
	 * resumed. As User threads have completed execution Debug Target should terminate itself.
	 *
	 * @throws Exception
	 */
	public void testMultipleSuspendVmLineBreakpoints() throws Exception {
		String typeName = "DropTests";
		IJavaLineBreakpoint bp18 = createLineBreakpoint(18, typeName);
		bp18.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);

		IJavaLineBreakpoint bp23 = createLineBreakpoint(23, typeName);
		bp23.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);

		IJavaLineBreakpoint bp27 = createLineBreakpoint(27, typeName);
		bp27.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp18);
			verifyAllThreadsSuspended(thread);
			IJavaDebugTarget debugTarget = (IJavaDebugTarget) thread.getDebugTarget();

			// Resume thread at every breakpoint.
			// vm is suspended at bp18.
			thread.resume();
			verifyAllThreadsSuspended(debugTarget, thread);

			// vm is suspended at bp23.
			thread.resume();
			verifyAllThreadsSuspended(debugTarget, thread);

			// vm is suspended at bp27.
			thread.resume();
			verifyAllThreadsSuspended(debugTarget, thread);

			// Now resume the debug target.
			debugTarget.resume();
			verifyNothingIsSuspended(debugTarget);

			// VM should regularly terminate execution and disconnect from target
			assertTrue("Debug target should be disconnected", debugTarget.isDisconnected());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Verifies that all of the threads of the given Debug Target are suspended except the one is passed
	 */
	protected void verifyAllThreadsSuspended(IJavaDebugTarget debugTarget, IJavaThread notSuspended) throws Exception {
		TestUtil.waitForJobs(getName(), 50, 10000);
		TestUtil.runEventLoop();
		IThread[] threads = debugTarget.getThreads();
		for (IThread thread : threads) {
			if (thread == notSuspended) {
				continue;
			}
			assertTrue("Thread wasn't suspended when a SUSPEND_VM breakpoint was hit, thread=" + thread.getName(), thread.isSuspended());
		}
	}

	/**
	 * Verifies that all of the threads of the given Debug Target are resumed
	 */
	protected void verifyNothingIsSuspended(IJavaDebugTarget debugTarget) throws Exception {
		TestUtil.waitForJobs(getName(), 100, 10000);
		TestUtil.runEventLoop();
		IThread[] threads = debugTarget.getThreads();
		for (IThread thread : threads) {
			assertFalse("All threads should be suspended, but at least one not: thread=" + thread.getName(), thread.isSuspended());
		}
		assertFalse("Shouldn't be suspended", debugTarget.isSuspended());
	}
}
