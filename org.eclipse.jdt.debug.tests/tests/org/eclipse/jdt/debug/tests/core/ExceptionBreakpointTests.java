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
			assertTrue("Should have been suspended at line number 35, not " + frame.getLineNumber(), frame.getLineNumber() == 35);
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
			debugTarget= launchAndTerminate(typeName);
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
			debugTarget= launchAndTerminate(typeName);
			ex.delete();
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
		}		
	}
	
	public void testInclusiveScopedException() throws Exception {
		String typeName = "ThrowsNPE";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);		
		ex.setInclusionFilters(new String[] {"ThrowsNPE"});
		
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
		ex.setExclusionFilters(new String[] {"ThrowsNPE"});
		
		IJavaDebugTarget debugTarget = null;
		try {
			debugTarget = launchAndTerminate(typeName);
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
	
	public void testMultiExclusiveScopedExceptionMissed() throws Exception {
		String typeName = "ThrowsNPE";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);		
		ex.setExclusionFilters(new String[] {"TestIO", "ThrowsNPE"});
		
		IJavaDebugTarget target= null;
		try {
			target = launchAndTerminate(typeName);
			ex.delete();
		} finally {
			terminateAndRemove(target);
			removeAllBreakpoints();
		}		
	}			
	
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
}
