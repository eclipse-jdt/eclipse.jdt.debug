package org.eclipse.jdt.debug.tests.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
*********************************************************************/


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
	
	public TargetPatternBreakpointTests(String name) {
		super(name);
	}

	public void testTargetPatternBreakpoints() throws Exception {
		JDIDebugPlugin.getDefault().addJavaBreakpointListener(this);
		
		String sourceName = "Breakpoints.java";
		List bps = new ArrayList();
		// anonymous class
		bps.add(createTargetPatternBreakpoint(32, sourceName));
		// blocks
		bps.add(createTargetPatternBreakpoint(91, sourceName));
		// constructor
		bps.add(createTargetPatternBreakpoint(66, sourceName));
		// else
		bps.add(createTargetPatternBreakpoint(77, sourceName));
		//finally after catch
		bps.add(createTargetPatternBreakpoint(109, sourceName));
		//finally after try
		bps.add(createTargetPatternBreakpoint(117, sourceName));
		// for loop
		bps.add(createTargetPatternBreakpoint(82, sourceName));
		// if
		bps.add(createTargetPatternBreakpoint(70, sourceName));
		// initializer
		bps.add(createTargetPatternBreakpoint(6, sourceName));
		// inner class
		bps.add(createTargetPatternBreakpoint(11, sourceName));
		// return true
		bps.add(createTargetPatternBreakpoint(61, sourceName));
		// instance method
		bps.add(createTargetPatternBreakpoint(96, sourceName));
		// static method 
		bps.add(createTargetPatternBreakpoint(42, sourceName));
		// case statement
		bps.add(createTargetPatternBreakpoint(122, sourceName));
		// default statement
		bps.add(createTargetPatternBreakpoint(129, sourceName));
		// synchronized blocks
		bps.add(createTargetPatternBreakpoint(135, sourceName));
		// try
		bps.add(createTargetPatternBreakpoint(114, sourceName));
		//catch
		bps.add(createTargetPatternBreakpoint(107, sourceName));
		// while
		bps.add(createTargetPatternBreakpoint(86, sourceName));
		
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint("Breakpoints");
			assertNotNull("Breakpoint not hit within timeout period", thread);
			while (!bps.isEmpty()) {
				IBreakpoint hit = getBreakpoint(thread);
				assertNotNull("suspended, but not by breakpoint", hit);
				assertTrue("hit un-registered breakpoint", bps.contains(hit));
				assertTrue("suspended, but not by line breakpoint", hit instanceof ILineBreakpoint);
				ILineBreakpoint breakpoint= (ILineBreakpoint) hit;
				int lineNumber = breakpoint.getLineNumber();
				int stackLine = thread.getTopStackFrame().getLineNumber();
				assertTrue("line numbers of breakpoint and stack frame do not match", lineNumber == stackLine);
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
	public boolean breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		return true;
	}

	/**
	 * @see IJavaBreakpointListener#breakpointInstalled(IJavaDebugTarget, IJavaBreakpoint)
	 */
	public void breakpointInstalled(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint) {
	}

	/**
	 * @see IJavaBreakpointListener#breakpointRemoved(IJavaDebugTarget, IJavaBreakpoint)
	 */
	public void breakpointRemoved(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint) {
	}

	/**
	 * @see IJavaBreakpointListener#installingBreakpoint(IJavaDebugTarget, IJavaBreakpoint, IJavaType)
	 */
	public boolean installingBreakpoint(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint,
		IJavaType type) {
		return true;
	}

	/**
	 * @see IJavaBreakpointListener#breakpointHasCompilationErrors(IJavaLineBreakpoint, Message[])
	 */
	public void breakpointHasCompilationErrors(
		IJavaLineBreakpoint breakpoint,
		Message[] errors) {
	}

	/**
	 * @see IJavaBreakpointListener#breakpointHasRuntimeException(IJavaLineBreakpoint, DebugException)
	 */
	public void breakpointHasRuntimeException(
		IJavaLineBreakpoint breakpoint,
		DebugException exception) {
	}
}
