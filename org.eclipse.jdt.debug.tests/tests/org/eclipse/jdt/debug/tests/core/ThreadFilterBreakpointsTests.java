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

import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests breakpoints with thread filters
 */
public class ThreadFilterBreakpointsTests extends AbstractDebugTest {

	public ThreadFilterBreakpointsTests(String name) {
		super(name);
	}

	public void testSimpleThreadFilterBreakpoint() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createLineBreakpoint(16, typeName);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			bp.setThreadFilter(thread);
			resumeToLineBreakpoint(thread, bp);
						
			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}

	public void testMultiThreadFilterBreakpoint() throws Exception {
		String typeName = "MultiThreadedLoop";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(17, typeName);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp1);
			
			IJavaLineBreakpoint bp2 = createLineBreakpoint(40, typeName);
			bp2.setThreadFilter(thread);
			
			thread = resumeToLineBreakpoint(thread, bp2);
			assertTrue("Suspended thread should have been '1stThread'", thread.getName().equals("1stThread"));
						
			bp1.delete();
			bp2.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}

	public void testExceptionThreadFilterBreakpoint() throws Exception {
		String typeName = "MultiThreadedException";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(17, typeName);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp1);
			IJavaExceptionBreakpoint ex1 = createExceptionBreakpoint("java.lang.NullPointerException", false, true);
			ex1.setThreadFilter(thread);
			
			thread = resume(thread);
			assertTrue("Suspended thread should have been '1stThread'", thread.getName().equals("1stThread"));
			
			bp1.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
			
	public void testAccessWatchpointThreadFilterBreakpoint() throws Exception {
		String typeName = "MultiThreadedList";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(21, typeName);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp1);
			IJavaWatchpoint wp = createWatchpoint(typeName, "list", true, false);			
			wp.setThreadFilter(thread);			
			
			thread = resume(thread);
			assertTrue("Suspended thread should have been '1stThread'", thread.getName().equals("1stThread"));
			
			bp1.delete();
			wp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
			
	public void testModificationWatchpointThreadFilterBreakpoint() throws Exception {
		String typeName = "MultiThreadedList";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(22, typeName);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp1);
			IJavaWatchpoint wp = createWatchpoint(typeName, "i", false, true);			
			wp.setThreadFilter(thread);			
			
			thread = resume(thread);
			assertTrue("Suspended thread should have been '1stThread'", thread.getName().equals("1stThread"));
			
			bp1.delete();
			wp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
}
