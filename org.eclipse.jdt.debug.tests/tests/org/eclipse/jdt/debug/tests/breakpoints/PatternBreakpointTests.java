/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStratumLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests deferred pattern breakpoints.
 */
@SuppressWarnings("deprecation")
public class PatternBreakpointTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public PatternBreakpointTests(String name) {
		super(name);
	}

	/**
	 * Tests several pattern breakpoints
	 * @throws Exception
	 */
	public void testPatternBreakpoints() throws Exception {
		String sourceName = "Breakpoints.java";
		String pattern = "Break";
		List<IJavaPatternBreakpoint> bps = new ArrayList<>();
		// anonymous class
		bps.add(createPatternBreakpoint(46, sourceName, pattern));
		// blocks
		bps.add(createPatternBreakpoint(105, sourceName, pattern));
		// constructor
		bps.add(createPatternBreakpoint(80, sourceName, pattern));
		// else
		bps.add(createPatternBreakpoint(91, sourceName, pattern));
		//finally after catch
		bps.add(createPatternBreakpoint(123, sourceName, pattern));
		//finally after try
		bps.add(createPatternBreakpoint(131, sourceName, pattern));
		// for loop
		bps.add(createPatternBreakpoint(96, sourceName, pattern));
		// if
		bps.add(createPatternBreakpoint(84, sourceName, pattern));
		// initializer
		bps.add(createPatternBreakpoint(20, sourceName, pattern));
		// inner class
		bps.add(createPatternBreakpoint(25, sourceName, pattern));
		// return true
		bps.add(createPatternBreakpoint(75, sourceName, pattern));
		// instance method
		bps.add(createPatternBreakpoint(110, sourceName, pattern));
		// static method
		bps.add(createPatternBreakpoint(56, sourceName, pattern));
		// case statement
		bps.add(createPatternBreakpoint(136, sourceName, pattern));
		// default statement
		bps.add(createPatternBreakpoint(143, sourceName, pattern));
		// synchronized blocks
		bps.add(createPatternBreakpoint(149, sourceName, pattern));
		// try
		bps.add(createPatternBreakpoint(128, sourceName, pattern));
		//catch
		bps.add(createPatternBreakpoint(121, sourceName, pattern));
		// while
		bps.add(createPatternBreakpoint(100, sourceName, pattern));


		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint("Breakpoints", false);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			while (!bps.isEmpty()) {
				IBreakpoint hit = getBreakpoint(thread);
				assertNotNull("suspended, but not by breakpoint", hit);
				assertTrue("hit un-registered breakpoint", bps.contains(hit));
				assertTrue("suspended, but not by line breakpoint", hit instanceof ILineBreakpoint);
				ILineBreakpoint breakpoint= (ILineBreakpoint) hit;
				int lineNumber = breakpoint.getLineNumber();
				int stackLine = thread.getTopStackFrame().getLineNumber();
				assertEquals("line numbers of breakpoint and stack frame do not match", lineNumber, stackLine);
				bps.remove(breakpoint);
				breakpoint.delete();
				if (!bps.isEmpty()) {
					thread = resume(thread);
				}
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Bug 74108 - enable/disable a stratum breakpoint that is not yet installed
	 * @throws Exception
	 */
	public void testToggleStratumBreakpoint() throws Exception {
		IJavaStratumLineBreakpoint stratumLineBreakpoint = createStratumBreakpoint(6, "date.jsp", "JSP");
		String typeName = "Breakpoints";
		createLineBreakpoint(55, typeName);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertTrue("suspended, but not by line breakpoint", hit instanceof ILineBreakpoint);
			ILineBreakpoint breakpoint= (ILineBreakpoint) hit;
			int lineNumber = breakpoint.getLineNumber();
			int stackLine = thread.getTopStackFrame().getLineNumber();
			assertEquals("line numbers of breakpoint and stack frame do not match", lineNumber, stackLine);
			breakpoint.delete();
			assertFalse("stratum breakpoint should not be installed", stratumLineBreakpoint.isInstalled());
			// toggle
			stratumLineBreakpoint.setEnabled(false);
			stratumLineBreakpoint.setEnabled(true);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Bug 74108 - enable/disable a pattern breakpoint that is not yet installed
	 * @throws Exception
	 */
	public void testTogglePatternBreakpoint() throws Exception {
		IJavaPatternBreakpoint patternBreakpoint = createPatternBreakpoint(3, "date.jsp", "date");
		String typeName = "Breakpoints";
		createLineBreakpoint(55, typeName);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertTrue("suspended, but not by line breakpoint", hit instanceof ILineBreakpoint);
			ILineBreakpoint breakpoint= (ILineBreakpoint) hit;
			int lineNumber = breakpoint.getLineNumber();
			int stackLine = thread.getTopStackFrame().getLineNumber();
			assertEquals("line numbers of breakpoint and stack frame do not match", lineNumber, stackLine);
			breakpoint.delete();
			assertFalse("pattern breakpoint should not be installed", patternBreakpoint.isInstalled());
			// toggle
			patternBreakpoint.setEnabled(false);
			patternBreakpoint.setEnabled(true);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that a pattern breakpoint is skipped when set to do so
	 * @throws Exception
	 */
	public void testSkipPatternBreakpoint() throws Exception {
		String sourceName = "Breakpoints.java";
		String pattern = "Break";
		List<IJavaPatternBreakpoint> bps = new ArrayList<>();
		// anonymous class
		bps.add(createPatternBreakpoint(46, sourceName, pattern));
		// blocks
		bps.add(createPatternBreakpoint(105, sourceName, pattern));
		// constructor
		bps.add(createPatternBreakpoint(80, sourceName, pattern));
		// else
		bps.add(createPatternBreakpoint(91, sourceName, pattern));
		//finally after catch
		bps.add(createPatternBreakpoint(123, sourceName, pattern));
		//finally after try
		bps.add(createPatternBreakpoint(131, sourceName, pattern));
		// for loop
		bps.add(createPatternBreakpoint(96, sourceName, pattern));
		// if
		bps.add(createPatternBreakpoint(84, sourceName, pattern));
		// initializer
		bps.add(createPatternBreakpoint(20, sourceName, pattern));
		// inner class
		bps.add(createPatternBreakpoint(25, sourceName, pattern));
		// return true
		bps.add(createPatternBreakpoint(75, sourceName, pattern));
		// instance method
		bps.add(createPatternBreakpoint(110, sourceName, pattern));
		// static method
		bps.add(createPatternBreakpoint(56, sourceName, pattern));
		// case statement
		bps.add(createPatternBreakpoint(136, sourceName, pattern));
		// default statement
		bps.add(createPatternBreakpoint(143, sourceName, pattern));
		// synchronized blocks
		bps.add(createPatternBreakpoint(149, sourceName, pattern));
		// try
		bps.add(createPatternBreakpoint(128, sourceName, pattern));
		//catch
		bps.add(createPatternBreakpoint(121, sourceName, pattern));
		// while
		bps.add(createPatternBreakpoint(100, sourceName, pattern));


		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint("Breakpoints");
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
