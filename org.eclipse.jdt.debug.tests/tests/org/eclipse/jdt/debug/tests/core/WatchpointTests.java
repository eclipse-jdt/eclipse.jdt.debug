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
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class WatchpointTests extends AbstractDebugTest {
	
	public WatchpointTests(String name) {
		super(name);
	}

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
			// line 27
			assertEquals("Should be on line 27", 27, frame.getLineNumber());
			
			// should hit access 10 times
			int count = 10;
			while (count > 0) {
				thread = resume(thread);
				hit = getBreakpoint(thread);
				frame = thread.getTopStackFrame();
				assertNotNull("No breakpoint", hit);
				assertTrue("Should be an access", wp.isAccessSuspend(thread.getDebugTarget()));
				assertEquals("Should be line 30", 30, frame.getLineNumber());
				count--;
			}
			
			resumeAndExit(thread);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
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
			// line 27
			assertEquals("Should be on line 27", 27, frame.getLineNumber());
			
			resumeAndExit(thread);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	
	
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
	
	public void testAccess() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.Watchpoint";
		
		IJavaWatchpoint wp = createWatchpoint(typeName, "list", true, false);
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			IStackFrame frame = thread.getTopStackFrame();
			assertNotNull("No breakpoint", hit);
			assertTrue("Should be an access", wp.isAccessSuspend(thread.getDebugTarget()));
			assertEquals("Should be line 30", 30, frame.getLineNumber());			
			
			// should hit access 9 more times
			int count = 9;
			while (count > 0) {
				thread = resume(thread);
				hit = getBreakpoint(thread);
				frame = thread.getTopStackFrame();
				assertNotNull("No breakpoint", hit);
				assertTrue("Should be an access", wp.isAccessSuspend(thread.getDebugTarget()));
				assertEquals("Should be line 30", 30, frame.getLineNumber());
				count--;
			}
			
			resumeAndExit(thread);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	

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
			assertTrue("Should be an access", wp.isAccessSuspend(thread.getDebugTarget()));
			assertEquals("Should be line 30", 30, frame.getLineNumber());			
			
			wp.setEnabled(false);
						
			resumeAndExit(thread);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	

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
			assertEquals("Should be line 30", 30, frame.getLineNumber());			
			IVariable var = frame.findVariable("value");
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
				assertTrue("Should be an access", wp.isAccessSuspend(thread.getDebugTarget()));
				assertEquals("Should be line 30", 30, frame.getLineNumber());
				count--;
			}
			
			resumeAndExit(thread);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	
}
