/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests deferred breakpoints.
 */
public class DeferredBreakpointTests extends AbstractDebugTest {
	
	public DeferredBreakpointTests(String name) {
		super(name);
	}

	public void testDeferredBreakpoints() throws Exception {
		String typeName = "Breakpoints";
		List bps = new ArrayList();
		// anonymous class
		bps.add(createLineBreakpoint(43, typeName));
		// blocks
		bps.add(createLineBreakpoint(102, typeName));
		// constructor
		bps.add(createLineBreakpoint(77, typeName));
		// else
		bps.add(createLineBreakpoint(88, typeName));
		//finally after catch
		bps.add(createLineBreakpoint(120, typeName));
		//finally after try
		bps.add(createLineBreakpoint(128, typeName));
		// for loop
		bps.add(createLineBreakpoint(93, typeName));
		// if
		bps.add(createLineBreakpoint(81, typeName));
		// initializer
		bps.add(createLineBreakpoint(17, typeName));
		// inner class
		bps.add(createLineBreakpoint(22, typeName));
		// return true
		bps.add(createLineBreakpoint(72, typeName));
		// instance method
		bps.add(createLineBreakpoint(107, typeName));
		// static method 
		bps.add(createLineBreakpoint(53, typeName));
		// case statement
		bps.add(createLineBreakpoint(133, typeName));
		// default statement
		bps.add(createLineBreakpoint(140, typeName));
		// synchronized blocks
		bps.add(createLineBreakpoint(146, typeName));
		// try
		bps.add(createLineBreakpoint(125, typeName));
		//catch
		bps.add(createLineBreakpoint(118, typeName));
		// while
		bps.add(createLineBreakpoint(97, typeName));
		
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
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

	public void testDisabledBreakpoint() throws Exception {
		String typeName = "Breakpoints";
		ILineBreakpoint bp = createLineBreakpoint(52, typeName);
		bp.setEnabled(false);
		
		IJavaDebugTarget debugTarget = null;
		try {
			debugTarget= launchAndTerminate(typeName);
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
		}				
	}

	public void testEnableDisableBreakpoint() throws Exception {
		String typeName = "HitCountLooper";
		ILineBreakpoint bp = createLineBreakpoint(16, typeName);
		bp.setEnabled(true);
		
		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			bp.setEnabled(false);
			resumeAndExit(thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}				
	}
}
