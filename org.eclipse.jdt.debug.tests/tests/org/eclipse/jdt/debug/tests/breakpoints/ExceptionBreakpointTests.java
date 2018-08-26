/*******************************************************************************
 *  Copyright (c) 2000, 2006 IBM Corporation and others.
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
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests exception breakpoints.
 */

public class ExceptionBreakpointTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name the name of the test
	 */
	public ExceptionBreakpointTests(String name) {
		super(name);
	}

	/**
	 * tests that breakpoint suspends on caught exceptions
	 * @throws Exception
	 */
	public void testCaughtException() throws Exception {
		String typeName = "ThrowsException";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("TestException", true, false);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("suspended, but not by exception breakpoint", ex ,hit);
			ex.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * tests that breakpoint suspends on uncaught exceptions
	 * @throws Exception
	 */
	public void testUncaughtException() throws Exception {
		String typeName = "HitCountException";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", false, true);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("suspended, but not by exception breakpoint", ex ,hit);
			IJavaStackFrame frame= (IJavaStackFrame)thread.getTopStackFrame();
			assertTrue("Should have been suspended at line number 38, not " + frame.getLineNumber(), frame.getLineNumber() == 38);
			ex.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 *
	 * @throws Exception
	 */
	public void testDisabledCaughtException() throws Exception {
		String typeName = "ThrowsNPE";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);
		ex.setEnabled(false);

		IJavaDebugTarget debugTarget= null;
		try {
			debugTarget= launchAndTerminate(typeName);
			ex.delete();
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
		}
	}

	/**
	 * tests that the breakpoint does not suspend for disabled breakpoint set for uncaught exceptions
	 * @throws Exception
	 */
	public void testDisabledUncaughtNPE() throws Exception {
		String typeName = "MultiThreadedException";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", false, true);
		ex.setEnabled(false);

		IJavaDebugTarget debugTarget= null;
		try {
			debugTarget= launchAndTerminate(typeName);
			ex.delete();
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
		}
	}

	/**
	 * tests that the breakpoint will only suspend on a breakpoint in the inclusion filters
	 * @throws Exception
	 */
	public void testInclusiveScopedException() throws Exception {
		String typeName = "ThrowsException";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("TestException", true, false);
		ex.setInclusionFilters(new String[] {"ThrowsException"});

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("suspended, but not by exception breakpoint", ex ,hit);
			ex.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * test that the breakpoint will not suspend as the class that throws the exception has been added to the exclusion filters
	 * @throws Exception
	 */
	public void testExclusiveScopedException() throws Exception {
		String typeName = "ThrowsException";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("TestException", true, false);
		ex.setExclusionFilters(new String[] {"ThrowsException"});

		IJavaDebugTarget debugTarget = null;
		try {
			debugTarget = launchAndTerminate(typeName);
			ex.delete();
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
		}
	}

	/**
	 * tests the hit count of an exception breakpoint
	 * @throws Exception
	 */
	public void testHitCountException() throws Exception {
		String typeName = "HitCountException";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, true);
		ex.setHitCount(2);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			IJavaStackFrame frame= (IJavaStackFrame)thread.getTopStackFrame();
			assertEquals("Should have been suspended at linenumber", 38, frame.getLineNumber());

			ex.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * tests that the breakpoint will suspend at an NPE with more than one exclusion filter, just not suspend in either
	 * of the classes for the exclusion filter
	 * @throws Exception
	 */
	public void testMultiExclusiveScopedExceptionHit() throws Exception {
		String typeName = "ThrowsNPE";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);
		ex.setExclusionFilters(new String[] {"TestIO", "Breakpoints"});

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("Did not suspend", thread);
			assertEquals("Should have suspended at NPE", ex, thread.getBreakpoints()[0]);
			ex.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * tests that the breakpoint does not suspend for multiple exclusion filters
	 * @throws Exception
	 */
	public void testMultiExclusiveScopedExceptionMissed() throws Exception {
		String typeName = "ThrowsException";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("TestException", true, false);
		ex.setExclusionFilters(new String[] {"ThrowsException2", "ThrowsException"});

		IJavaDebugTarget target= null;
		try {
			target = launchAndTerminate(typeName);
			ex.delete();
		} finally {
			terminateAndRemove(target);
			removeAllBreakpoints();
		}
	}

	/**
	 * tests that a breakpoint is hit with multiple inclusion filters set
	 * @throws Exception
	 */
	public void testMultiInclusiveScopedExceptionHit() throws Exception {
		String typeName = "ThrowsNPE";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);
		ex.setInclusionFilters(new String[] {"ThrowsNPE", "Breakpoints"});

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("Did not suspend", thread);
			assertEquals("Should have suspended at NPE", ex, thread.getBreakpoints()[0]);
			ex.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * tests that the breakpoint does not suspend with multiple inclusion filters
	 * @throws Exception
	 */
	public void testMultiInclusiveScopedExceptionMissed() throws Exception {
		String typeName = "ThrowsNPE";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);
		ex.setInclusionFilters(new String[] {"TestIO", "Breakpoints"});

		IJavaDebugTarget target= null;
		try {
			target = launchAndTerminate(typeName);
			ex.delete();
		} finally {
			terminateAndRemove(target);
			removeAllBreakpoints();
		}
	}

	/**
	 * test that breakpoint suspends with multi inclusion and exclusion filters
	 * @throws Exception
	 */
	public void testMultiInclusiveExclusiveScopedExceptionHit() throws Exception {
		String typeName = "ThrowsNPE";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);
		ex.setInclusionFilters(new String[] {"ThrowsNPE", "Breakpoints"});
		ex.setExclusionFilters(new String[] {"HitCountException", "MethodLoop"});

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("Did not suspend", thread);
			assertEquals("Should have suspended at NPE", ex, thread.getBreakpoints()[0]);
			ex.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * tests that breakpoint does not suspend with multi inclusion and exclusion filters
	 * @throws Exception
	 */
	public void testMultiInclusiveExclusiveScopedExceptionMissed() throws Exception {
		String typeName = "ThrowsNPE";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);
		ex.setInclusionFilters(new String[] {"TestIO", "Breakpoints"});
		ex.setExclusionFilters(new String[] {"ThrowsNPE", "MethodLoop"});

		IJavaDebugTarget target= null;
		try {
			target = launchAndTerminate(typeName);
			ex.delete();
		} finally {
			terminateAndRemove(target);
			removeAllBreakpoints();
		}
	}

	/**
	 * tests that breakpoint is skipped when told to do so
	 * @throws Exception
	 */
	public void testSkipExceptionBreakpoint() throws Exception {
		String typeName = "ThrowsNPE";
		createExceptionBreakpoint("java.lang.NullPointerException", true, false);
		IJavaLineBreakpoint lineBreakpoint = createLineBreakpoint(15, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, lineBreakpoint);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			getBreakpointManager().setEnabled(false);
			resumeAndExit(thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			getBreakpointManager().setEnabled(true);
		}
	}
}
