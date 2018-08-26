/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaTargetPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

/**
 * Tests deferred target pattern breakpoints.
 */
public class TargetPatternBreakpointTests extends AbstractDebugTest implements IJavaBreakpointListener {

	/**
	 * Constructor
	 * @param name
	 */
	public TargetPatternBreakpointTests(String name) {
		super(name);
	}

	/**
	 * Tests that several pattern breakpoints are suspending properly
	 * @throws Exception
	 */
	public void testTargetPatternBreakpoints() throws Exception {
		JDIDebugPlugin.getDefault().addJavaBreakpointListener(this);

		String sourceName = "Breakpoints.java";
		List<IJavaTargetPatternBreakpoint> bps = new ArrayList<>();
		// anonymous class
		bps.add(createTargetPatternBreakpoint(46, sourceName));
		// blocks
		bps.add(createTargetPatternBreakpoint(105, sourceName));
		// constructor
		bps.add(createTargetPatternBreakpoint(80, sourceName));
		// else
		bps.add(createTargetPatternBreakpoint(69, sourceName));
		//finally after catch
		bps.add(createTargetPatternBreakpoint(123, sourceName));
		//finally after try
		bps.add(createTargetPatternBreakpoint(131, sourceName));
		// for loop
		bps.add(createTargetPatternBreakpoint(96, sourceName));
		// if
		bps.add(createTargetPatternBreakpoint(84, sourceName));
		// initializer
		bps.add(createTargetPatternBreakpoint(20, sourceName));
		// inner class
		bps.add(createTargetPatternBreakpoint(25, sourceName));
		// return true
		bps.add(createTargetPatternBreakpoint(75, sourceName));
		// instance method
		bps.add(createTargetPatternBreakpoint(110, sourceName));
		// static method
		bps.add(createTargetPatternBreakpoint(56, sourceName));
		// case statement
		bps.add(createTargetPatternBreakpoint(136, sourceName));
		// default statement
		bps.add(createTargetPatternBreakpoint(143, sourceName));
		// synchronized blocks
		bps.add(createTargetPatternBreakpoint(149, sourceName));
		// try
		bps.add(createTargetPatternBreakpoint(128, sourceName));
		//catch
		bps.add(createTargetPatternBreakpoint(121, sourceName));
		// while
		bps.add(createTargetPatternBreakpoint(100, sourceName));


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
			JDIDebugPlugin.getDefault().removeJavaBreakpointListener(this);
		}
	}
	/**
	 * @see IJavaBreakpointListener#addingBreakpoint(IJavaDebugTarget, IJavaBreakpoint)
	 */
	@Override
	public void addingBreakpoint(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint) {
			if (breakpoint instanceof IJavaTargetPatternBreakpoint) {
				IJavaTargetPatternBreakpoint bp = (IJavaTargetPatternBreakpoint)breakpoint;
				try {
					bp.setPattern(target,"Breakp");
				} catch (CoreException e) {
					assertTrue("Failed to set pattern", false);
				}
			}
	}

	/**
	 * @see IJavaBreakpointListener#breakpointHit(IJavaThread, IJavaBreakpoint)
	 */
	@Override
	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		return DONT_CARE;
	}

	/**
	 * @see IJavaBreakpointListener#breakpointInstalled(IJavaDebugTarget, IJavaBreakpoint)
	 */
	@Override
	public void breakpointInstalled(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint) {
	}

	/**
	 * @see IJavaBreakpointListener#breakpointRemoved(IJavaDebugTarget, IJavaBreakpoint)
	 */
	@Override
	public void breakpointRemoved(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint) {
	}

	/**
	 * @see IJavaBreakpointListener#installingBreakpoint(IJavaDebugTarget, IJavaBreakpoint, IJavaType)
	 */
	@Override
	public int installingBreakpoint(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint,
		IJavaType type) {
		return DONT_CARE;
	}

	/**
	 * @see IJavaBreakpointListener#breakpointHasCompilationErrors(IJavaLineBreakpoint, Message[])
	 */
	@Override
	public void breakpointHasCompilationErrors(
		IJavaLineBreakpoint breakpoint,
		Message[] errors) {
	}

	/**
	 * @see IJavaBreakpointListener#breakpointHasRuntimeException(IJavaLineBreakpoint, DebugException)
	 */
	@Override
	public void breakpointHasRuntimeException(
		IJavaLineBreakpoint breakpoint,
		DebugException exception) {
	}
}
