/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
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
		// see Bug 148255 [tests] "should be access" test failure
		boolean targetAborted = true;
		int attempts = 0;
		while (attempts < 10 && targetAborted) {
			targetAborted = false;
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
					IDebugTarget debugTarget = thread.getDebugTarget();
					hit = getBreakpoint(thread);
					frame = thread.getTopStackFrame();
					assertNotNull("No breakpoint", hit);
					if (!(wp.isAccessSuspend(debugTarget))) {
						targetAborted = debugTarget.isTerminated() || debugTarget.isDisconnected();
					}
					if (targetAborted) {
						attempts++;
						System.err.println("WARNING: Target aborted during 'testAccessAndModification()' - attempt #" + attempts);
						break;
					}
					assertTrue("Should be an access", wp.isAccessSuspend(thread.getDebugTarget()));
					assertEquals("Should be line 30", 30, frame.getLineNumber());
					count--;
				}
				if (!targetAborted) {
					resumeAndExit(thread);
				}
			} finally {
				terminateAndRemove(thread);
				removeAllBreakpoints();
				if (targetAborted) {
					Thread.sleep(2000);
				}
			}
		}
		assertFalse("Target aborted test " + attempts + " times", targetAborted);
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
		// see Bug 148255 [tests] "should be access" test failure
		boolean targetAborted = true;
		int attempts = 0;
		
		while (attempts < 10 && targetAborted) {
			targetAborted = false;
			String typeName = "org.eclipse.debug.tests.targets.Watchpoint";
			IJavaWatchpoint wp = createWatchpoint(typeName, "list", true, false);
			IJavaThread thread= null;
			try {
				thread= launchToBreakpoint(typeName);
				assertNotNull("Breakpoint not hit within timeout period", thread);
	
				wp = (IJavaWatchpoint) getBreakpoint(thread);
				IStackFrame frame = thread.getTopStackFrame();
				assertNotNull("No breakpoint", wp);
				assertTrue("Should be an access", wp.isAccessSuspend(thread.getDebugTarget()));
				assertEquals("Should be line 30", 30, frame.getLineNumber());			
				
				// should hit access 9 more times
				int count = 9;
				while (count > 0) {
					thread = resume(thread);
					IDebugTarget debugTarget = thread.getDebugTarget();
					wp = (IJavaWatchpoint) getBreakpoint(thread);
					frame = thread.getTopStackFrame();
					assertNotNull("No breakpoint", wp);
					if (!(wp.isAccessSuspend(debugTarget))) {
						targetAborted = debugTarget.isTerminated() || debugTarget.isDisconnected();
					}
					if (targetAborted) {
						attempts++;
						System.err.println("WARNING: Target aborted during 'testAccess()' - attempt #" + attempts);
						break;
					}
					assertTrue("Should be an access", wp.isAccessSuspend(debugTarget));
					assertEquals("Should be line 30", 30, frame.getLineNumber());
					count--;
				}
				if (!targetAborted) {
					resumeAndExit(thread);
				}
			} finally {
				terminateAndRemove(thread);
				removeAllBreakpoints();
				if (targetAborted) {
					Thread.sleep(2000);
				}
			}
		}
		assertFalse("Target aborted test " + attempts + " times", targetAborted);
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
			IVariable var = findVariable(frame, "value");
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
	
	public void testSkipWatchpoint() throws Exception {
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
			
			getBreakpointManager().setEnabled(false);
			resumeAndExit(thread);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			getBreakpointManager().setEnabled(true);
		}		
	}	
}
