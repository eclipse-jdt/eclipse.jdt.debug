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
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests instance filters on breakpoints.
 * 
 * These tests do not work on JDK1.3.1. They do work on 1.4.1,
 * as well as IBM 1.3 and 1.3.1.
 */
public class InstanceFilterTests extends AbstractDebugTest {
	
	public InstanceFilterTests(String name) {
		super(name);
	}

	/**
	 * Instance filter on a line breakpoint
	 * 	 * @throws Exception	 */
	public void testLineBreakpoint() throws Exception {
		String typeName = "Breakpoints";
		// instance method - if
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(81, typeName);		
		// instance method - else
		IJavaLineBreakpoint breakpoint2 = createLineBreakpoint(88, typeName);				
		
		IJavaThread thread= null;
		IJavaThread thread2 = null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("hit un-registered breakpoint", breakpoint, hit);
			
			// can only do test if the VM supports instance filters
			if (supportsInstanceBreakpoints(thread)) {
				// add instance filter
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				IJavaObject thisObject = frame.getThis();
				assertNotNull("Unable to access 'this'", thisObject);
				((IJavaBreakpoint)hit).addInstanceFilter(thisObject);
				
				// launch a second target
				thread2= launchToBreakpoint(typeName);
				assertNotNull("Breakpoint not hit in second target", thread2);
				
				// should miss first breakpoint
				IBreakpoint hit2 = getBreakpoint(thread2);
				assertNotNull("suspended, but not by breakpoint", hit2);
				assertEquals("did not hit 2nd breakpoint", breakpoint2, hit2);
			}
		} finally {
			terminateAndRemove(thread);
			terminateAndRemove(thread2);
			removeAllBreakpoints();
		}		
	}
	
	public void testMethodEntry() throws Exception {
		String typeName = "Breakpoints";
		// instance method
		IJavaMethodBreakpoint breakpoint = createMethodBreakpoint(typeName, "instanceMethod", "()V", true, false);		
		// instance method 2
		IJavaLineBreakpoint breakpoint2 = createMethodBreakpoint(typeName, "instanceMethod2", "()V", true, false);				
		
		IJavaThread thread= null;
		IJavaThread thread2 = null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("hit un-registered breakpoint", breakpoint, hit);
			
			// can only do test if the VM supports instance filters
			if (supportsInstanceBreakpoints(thread)) { 
				// add instance filter
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				IJavaObject thisObject = frame.getThis();
				assertNotNull("Unable to access 'this'", thisObject);
				((IJavaBreakpoint)hit).addInstanceFilter(thisObject);
				
				// launch a second target
				thread2= launchToBreakpoint(typeName);
				assertNotNull("Breakpoint not hit in second target", thread2);
				
				// should miss first breakpoint
				IBreakpoint hit2 = getBreakpoint(thread2);
				assertNotNull("suspended, but not by breakpoint", hit2);
				assertEquals("did not hit 2nd breakpoint", breakpoint2, hit2);
			}
		} finally {
			terminateAndRemove(thread);
			terminateAndRemove(thread2);
			removeAllBreakpoints();
		}		
	}	
	
	public void testWatchpoint() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.Watchpoint";
		
		IJavaWatchpoint wp = createWatchpoint(typeName, "list", true, true);
		IJavaLineBreakpoint bp = createLineBreakpoint(28, typeName);
		
		IJavaThread thread= null;
		IJavaThread thread2 = null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("No breakpoint", hit);
			assertEquals("did not hit watch point", wp, hit);
			
			// can only do test if the VM supports instance filters
			if (supportsInstanceBreakpoints(thread)) {			

				// add instance filter
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				IJavaObject thisObject = frame.getThis();
				assertNotNull("Unable to access 'this'", thisObject);
				((IJavaBreakpoint)hit).addInstanceFilter(thisObject);
				
				// launch a second target
				thread2= launchToBreakpoint(typeName);
				assertNotNull("Breakpoint not hit in second target", thread2);
				
				// should miss watchpoint
				IBreakpoint hit2 = getBreakpoint(thread2);
				assertNotNull("suspended, but not by breakpoint", hit2);
				assertEquals("did not hit line breakpoint", bp, hit2);			
			}
		} finally {
			terminateAndRemove(thread);
			terminateAndRemove(thread2);
			removeAllBreakpoints();
		}		
	}
	
	public void testException() throws Exception {
		String typeName = "ThrowsNPE";
		
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.NullPointerException", true, true);
		IJavaLineBreakpoint bp = createLineBreakpoint(21, typeName);
		
		IJavaThread thread= null;
		IJavaThread thread2 = null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("No breakpoint", hit);
			assertEquals("did not hit exception breakpoint", ex, hit);

			// can only do test if the VM supports instance filters
			if (supportsInstanceBreakpoints(thread)) {
			
				// add instance filter
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				IJavaObject thisObject = frame.getThis();
				assertNotNull("Unable to access 'this'", thisObject);
				((IJavaBreakpoint)hit).addInstanceFilter(thisObject);
				
				// launch a second target
				thread2= launchToBreakpoint(typeName);
				assertNotNull("Breakpoint not hit in second target", thread2);
				
				// should miss exception breakpoint
				IBreakpoint hit2 = getBreakpoint(thread2);
				assertNotNull("suspended, but not by breakpoint", hit2);
				assertEquals("did not hit line breakpoint", bp, hit2);
			}			
						
			
		} finally {
			terminateAndRemove(thread);
			terminateAndRemove(thread2);
			removeAllBreakpoints();
		}		
	}

	/**
	 * Returns whether the associated target supports instance breakpoints
	 * 
	 * @param thread
	 * @return boolean
	 */
	private boolean supportsInstanceBreakpoints(IJavaThread thread) {
		IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
		return target.supportsInstanceBreakpoints();
	}	
}
