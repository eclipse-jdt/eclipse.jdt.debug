/*******************************************************************************
 *  Copyright (c) 2000, 2014 IBM Corporation and others.
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

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestAgainException;

/**
 * Tests watchpoint, both modification and access watchpoints
 */
public class WatchpointTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public WatchpointTests(String name) {
		super(name);
	}

	/**
	 * Tests both access and modification breakpoints suspend
	 * @throws Exception
	 */
	public void testAccessAndModification() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.Watchpoint";

		IJavaWatchpoint wp = createWatchpoint(typeName, "list", true, true);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			IStackFrame frame = thread.getTopStackFrame();
			assertNotNull("No breakpoint", hit);

			// should be modification
			assertTrue("First hit should be modification", !wp.isAccessSuspend(thread.getDebugTarget()));
			// line 30
			assertEquals("Should be on line 30", 30, frame.getLineNumber());

			// should hit access 10 times
			int count = 10;
			while (count > 0) {
				thread = resume(thread);
				hit = getBreakpoint(thread);
				frame = thread.getTopStackFrame();
				assertNotNull("No breakpoint", hit);
				IDebugTarget debugTarget = thread.getDebugTarget();
				if (debugTarget.isTerminated() || debugTarget.isDisconnected()) {
					throw new TestAgainException("Retest - the debug target is terminated or disconnected");
				}
				assertTrue("Should be an access", wp.isAccessSuspend(thread.getDebugTarget()));
				assertEquals("Should be line 33", 33, frame.getLineNumber());
				count--;
			}

			resumeAndExit(thread);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that a modification breakpoint suspends correctly
	 * @throws Exception
	 */
	public void testModification() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.Watchpoint";

		IJavaWatchpoint wp = createWatchpoint(typeName, "list", false, true);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			IStackFrame frame = thread.getTopStackFrame();
			assertNotNull("No breakpoint", hit);

			// should be modification
			assertTrue("First hit should be modification", !wp.isAccessSuspend(thread.getDebugTarget()));
			// line 30
			assertEquals("Should be on line 30", 30, frame.getLineNumber());

			resumeAndExit(thread);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that a disabled modification watchpoint is NOT hit
	 * @throws Exception
	 */
	public void testDisabledModification() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.Watchpoint";

		IJavaWatchpoint wp = createWatchpoint(typeName, "list", false, true);
		wp.setEnabled(false);

		IJavaDebugTarget debugTarget= null;
		try {
			debugTarget= launchAndTerminate(typeName);
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that an access watchpoint is hit
	 * @throws Exception
	 */
	public void testAccess() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.Watchpoint";

		IJavaWatchpoint wp = createWatchpoint(typeName, "list", true, false);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			wp = (IJavaWatchpoint) getBreakpoint(thread);
			IStackFrame frame = thread.getTopStackFrame();
			assertNotNull("No breakpoint", wp);
			assertTrue("Should be an access", wp.isAccessSuspend(thread.getDebugTarget()));
			assertEquals("Should be line 33", 33, frame.getLineNumber());

			// should hit access 9 more times
			int count = 9;
			while (count > 0) {
				thread = resume(thread);
				wp = (IJavaWatchpoint) getBreakpoint(thread);
				frame = thread.getTopStackFrame();
				assertNotNull("No breakpoint", wp);
				IDebugTarget debugTarget = thread.getDebugTarget();
				if (debugTarget.isTerminated() || debugTarget.isDisconnected()) {
					throw new TestAgainException("Retest - the debug target is terminated or disconnected");
				}
				assertTrue("Should be an access", wp.isAccessSuspend(thread.getDebugTarget()));
				assertEquals("Should be line 33", 33, frame.getLineNumber());
				count--;
			}

			resumeAndExit(thread);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that a disabled access watchpoint is not hit
	 * @throws Exception
	 */
	public void testDisabledAccess() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.Watchpoint";

		IJavaWatchpoint wp = createWatchpoint(typeName, "list", true, false);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			IStackFrame frame = thread.getTopStackFrame();
			assertNotNull("No breakpoint", hit);
			IDebugTarget debugTarget = thread.getDebugTarget();
			if (debugTarget.isTerminated() || debugTarget.isDisconnected()) {
				throw new TestAgainException("Retest - the debug target is terminated or disconnected");
			}
			assertTrue("Should be an access", wp.isAccessSuspend(thread.getDebugTarget()));
			assertEquals("Should be line 33", 33, frame.getLineNumber());

			wp.setEnabled(false);

			resumeAndExit(thread);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that an access watchpoint suspends when it count is hit
	 * @throws Exception
	 */
	public void testHitCountAccess() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.Watchpoint";

		IJavaWatchpoint wp = createWatchpoint(typeName, "list", true, false);
		wp.setHitCount(4);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull("No breakpoint", hit);
			assertTrue("Should be an access", wp.isAccessSuspend(thread.getDebugTarget()));
			assertEquals("Should be line 33", 33, frame.getLineNumber());
			IVariable var = findVariable(frame, "value");
			assertNotNull("Could not find variable 'value'", var);

			// retrieve an instance var
			IJavaPrimitiveValue value = (IJavaPrimitiveValue)var.getValue();
			assertNotNull(value);
			int varValue = value.getIntValue();
			assertTrue("'value' should be 7", varValue == 7);

			wp.setHitCount(0);

			// should hit access 6 more times
			int count = 6;
			while (count > 0) {
				thread = resume(thread);
				hit = getBreakpoint(thread);
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertNotNull("No breakpoint", hit);
				IDebugTarget debugTarget = thread.getDebugTarget();
				if (debugTarget.isTerminated() || debugTarget.isDisconnected()) {
					throw new TestAgainException("Retest - the debug target is terminated or disconnected");
				}
				assertTrue("Should be an access", wp.isAccessSuspend(thread.getDebugTarget()));
				assertEquals("Should be line 33", 33, frame.getLineNumber());
				count--;
			}

			resumeAndExit(thread);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that a watchpoint set to be skipped is indeed skipped
	 * @throws Exception
	 */
	public void testSkipWatchpoint() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.Watchpoint";

		IJavaWatchpoint wp = createWatchpoint(typeName, "list", true, true);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			IStackFrame frame = thread.getTopStackFrame();
			assertNotNull("No breakpoint", hit);

			// should be modification
			assertTrue("First hit should be modification", !wp.isAccessSuspend(thread.getDebugTarget()));
			// line 27
			assertEquals("Should be on line 30", 30, frame.getLineNumber());

			getBreakpointManager().setEnabled(false);
			resumeAndExit(thread);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			getBreakpointManager().setEnabled(true);
		}
	}

	/**
	 * Tests that a watchpoint set to be skipped is indeed skipped
	 *
	 * @throws Exception
	 */
	public void testFinalWatchpoint() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.BreakpointsLocationBug344984";

		IJavaWatchpoint wp = createWatchpoint(typeName, "fWorkingValues", true, true);

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			IStackFrame frame = thread.getTopStackFrame();
			assertNotNull("No watchpoint", hit);

			// should be modification
			assertTrue("First hit should be modification", !wp.isAccessSuspend(thread.getDebugTarget()));
			// line 27
			assertEquals("Should be on line 18", 18, frame.getLineNumber());

			getBreakpointManager().setEnabled(false);
			resumeAndExit(thread);

		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			getBreakpointManager().setEnabled(true);
		}
	}
}
