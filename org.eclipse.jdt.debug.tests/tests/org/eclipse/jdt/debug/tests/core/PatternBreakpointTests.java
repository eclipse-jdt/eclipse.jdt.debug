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

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests deferred pattern breakpoints.
 */
public class PatternBreakpointTests extends AbstractDebugTest {
	
	public PatternBreakpointTests(String name) {
		super(name);
	}

	public void testPatternBreakpoints() throws Exception {
		String sourceName = "Breakpoints.java";
		String pattern = "Break";
		List bps = new ArrayList();
		// anonymous class
		bps.add(createPatternBreakpoint(32, sourceName, pattern));
		// blocks
		bps.add(createPatternBreakpoint(91, sourceName, pattern));
		// constructor
		bps.add(createPatternBreakpoint(66, sourceName, pattern));
		// else
		bps.add(createPatternBreakpoint(77, sourceName, pattern));
		//finally after catch
		bps.add(createPatternBreakpoint(109, sourceName, pattern));
		//finally after try
		bps.add(createPatternBreakpoint(117, sourceName, pattern));
		// for loop
		bps.add(createPatternBreakpoint(82, sourceName, pattern));
		// if
		bps.add(createPatternBreakpoint(70, sourceName, pattern));
		// initializer
		bps.add(createPatternBreakpoint(6, sourceName, pattern));
		// inner class
		bps.add(createPatternBreakpoint(11, sourceName, pattern));
		// return true
		bps.add(createPatternBreakpoint(61, sourceName, pattern));
		// instance method
		bps.add(createPatternBreakpoint(96, sourceName, pattern));
		// static method 
		bps.add(createPatternBreakpoint(42, sourceName, pattern));
		// case statement
		bps.add(createPatternBreakpoint(122, sourceName, pattern));
		// default statement
		bps.add(createPatternBreakpoint(129, sourceName, pattern));
		// synchronized blocks
		bps.add(createPatternBreakpoint(135, sourceName, pattern));
		// try
		bps.add(createPatternBreakpoint(114, sourceName, pattern));
		//catch
		bps.add(createPatternBreakpoint(107, sourceName, pattern));
		// while
		bps.add(createPatternBreakpoint(86, sourceName, pattern));
		
		
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
		}		
	}
}
