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

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests exception breakpoints.
 */

public class ExceptionBreakpointTests extends AbstractDebugTest {
	
	public ExceptionBreakpointTests(String name) {
		super(name);
	}

	public void testCaughtNPE() throws Exception {
		String typeName = "ThrowsNPE";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);		
		
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
	
	public void testUncaughtNPE() throws Exception {
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
			assertTrue("Should have been suspended at linenumber", frame.getLineNumber() == 35);
			ex.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	public void testDisabledCaughtNPE() throws Exception {
		String typeName = "ThrowsNPE";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);		
		ex.setEnabled(false);
		
		IJavaDebugTarget debugTarget= null;
		try {
			debugTarget= launchAndTerminate(typeName, 3000);
			ex.delete();
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
		}		
	}
	
	public void testDisabledUncaughtNPE() throws Exception {
		String typeName = "MultiThreadedException";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", false, true);		
		ex.setEnabled(false);
		
		IJavaDebugTarget debugTarget= null;
		try {
			debugTarget= launchAndTerminate(typeName, 3000);
			ex.delete();
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
		}		
	}
	
	public void testInclusiveScopedException() throws Exception {
		String typeName = "ThrowsNPE";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);		
		ex.setFilters(new String[] {"ThrowsNPE"}, true);
		
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

	public void testExclusiveScopedException() throws Exception {
		String typeName = "ThrowsNPE";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);		
		ex.setFilters(new String[] {"ThrowsNPE"}, false);
		
		IJavaDebugTarget debugTarget = null;
		try {
			debugTarget = launchAndTerminate(typeName, 3000);
			ex.delete();
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
		}		
	}

	public void testHitCountException() throws Exception {
		String typeName = "HitCountException";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, true);		
		ex.setHitCount(2);
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			IJavaStackFrame frame= (IJavaStackFrame)thread.getTopStackFrame();
			assertTrue("Should have been suspended at linenumber", frame.getLineNumber() == 35);
			
			ex.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
}
