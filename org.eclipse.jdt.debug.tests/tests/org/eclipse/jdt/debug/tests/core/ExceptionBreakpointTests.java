package org.eclipse.jdt.debug.tests.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
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
			thread= launch(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("suspended, but not by exception breakpoint", ex ,hit);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	public void testInclusiveScopedException() throws Exception {
		String typeName = "ThrowsNPE";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);		
		ex.setFilters(new String[] {"ThrowsNPE"}, true);
		
		IJavaThread thread= null;
		try {
			thread= launch(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("suspended, but not by exception breakpoint", ex ,hit);

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
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
		}		
	}
}
