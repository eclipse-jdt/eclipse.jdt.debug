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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Test that a SUSPEND_VM breakpoint suspends all threads
 */
public class SuspendVMBreakpointsTests extends AbstractDebugTest {

	public SuspendVMBreakpointsTests(String name) {
		super(name);
	}

	public void testSuspendVmLineBreakpoint() throws Exception {
		String typeName = "MultiThreadedLoop";
		IJavaLineBreakpoint bp = createLineBreakpoint(40, typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			
			verifyAllThreadsSuspended(thread);
			
			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}

	public void testSuspendVmExceptionBreakpoint() throws Exception {
		String typeName = "ThrowsNPE";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, false);		
		ex.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("suspended, but not by exception breakpoint", ex ,hit);
			
			verifyAllThreadsSuspended(thread);
			
			ex.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	public void testSuspendVmAccessWatchpointBreakpoint() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.Watchpoint";
		
		IJavaWatchpoint wp = createWatchpoint(typeName, "list", true, false);
		wp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			IStackFrame frame = thread.getTopStackFrame();
			assertNotNull("No breakpoint", hit);
			assertTrue("Should be an access", wp.isAccessSuspend(thread.getDebugTarget()));
			assertEquals("Should be line 30", 30, frame.getLineNumber());			
			
			verifyAllThreadsSuspended(thread);
			
			wp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	

	public void testSuspendVmModificationWatchpointBreakpoint() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.Watchpoint";
		
		IJavaWatchpoint wp = createWatchpoint(typeName, "list", false, true);
		wp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);
		
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
			
			verifyAllThreadsSuspended(thread);
			
			wp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	
	
	public void testSuspendVmMethodEntryBreakpoint() throws Exception {
		String typeName = "DropTests";
		IJavaBreakpoint bp = createMethodBreakpoint(typeName, "method4", "()V", true, false);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertTrue("breakpoint was not a method breakpoint", hit instanceof IJavaMethodBreakpoint);

			verifyAllThreadsSuspended(thread);
			
			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	public void testSuspendVmMethodExitBreakpoint() throws Exception {
		String typeName = "DropTests";
		IJavaBreakpoint bp = createMethodBreakpoint(typeName, "method1", "()V", false, true);		
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertTrue("breakpoint was not a method breakpoint", hit instanceof IJavaMethodBreakpoint);

			verifyAllThreadsSuspended(thread);
						
			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	protected void verifyAllThreadsSuspended(IJavaThread thread) {
		try {
			IJavaDebugTarget debugTarget = (IJavaDebugTarget)thread.getDebugTarget();
			IThread[] threads = debugTarget.getThreads();
			for (int i = 0; i < threads.length; i++) {
				assertTrue("Thread wasn't suspended when a SUSPEND_VM breakpoint was hit, thread=" + threads[i].getName(), threads[i].isSuspended());
			}		
		} catch (DebugException e) {
			fail(e.getMessage());
		}
	}	
}
