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

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests breakpoint creation/deletion and listener interfaces.
 */
public class JavaBreakpointListenerTests extends AbstractDebugTest implements IJavaBreakpointListener {
	
	public int fAddCallbacks = 0;
	
	public int fRemoveCallbacks = 0;
	
	public int fInstalledCallbacks = 0;
	
	public IJavaBreakpoint fBreakpoint;
		
	public JavaBreakpointListenerTests(String name) {
		super(name);
	}

	protected void resetCallbacks() {
		fAddCallbacks = 0;
	
		fRemoveCallbacks = 0;
	
		fInstalledCallbacks = 0;	
	}
	
	public void testLineBreakpoint() throws Exception {		
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(54, "Breakpoints");
		fBreakpoint = breakpoint;
		resetCallbacks();
		
		IJavaThread thread = null;
		try {
			JDIDebugModel.addJavaBreakpointListener(this);		
			thread = launchToBreakpoint("Breakpoints");
			assertNotNull(thread);
			// breakpoint should be added & installed
			assertEquals("Breakpoint should be added", 1, fAddCallbacks);
			assertEquals("Breakpoint should be installed", 1, fInstalledCallbacks);
			assertEquals("Breakpoint should not be removed", 0, fRemoveCallbacks);	

			// disable and re-enable the breakpoint
			breakpoint.setEnabled(false);
			breakpoint.setEnabled(true);
			
			// should still be installed/added once
			assertEquals("Breakpoint should be added", 1, fAddCallbacks);
			assertEquals("Breakpoint should be installed", 1, fInstalledCallbacks);
			assertEquals("Breakpoint should not be removed", 0, fRemoveCallbacks);
			
			// change the hit count
			breakpoint.setHitCount(34);
			
			// should still be installed/added once
			assertEquals("Breakpoint should be added", 1, fAddCallbacks);
			assertEquals("Breakpoint should be installed", 1, fInstalledCallbacks);
			assertEquals("Breakpoint should not be removed", 0, fRemoveCallbacks);
			
			// delete the breakpoint
			breakpoint.delete();
			
			// should still be installed/added once
			assertEquals("Breakpoint should be added", 1, fAddCallbacks);
			assertEquals("Breakpoint should be installed", 1, fInstalledCallbacks);
			
			// and removed
			assertEquals("Breakpoint should be removed", 1, fRemoveCallbacks);								
			
		} finally {
			JDIDebugModel.removeJavaBreakpointListener(this);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
		
	}	
	
	public void testExceptionBreakpoint() throws Exception {		
		IJavaExceptionBreakpoint breakpoint = createExceptionBreakpoint("java.lang.NullPointerException", true, true);
		fBreakpoint = breakpoint;
		resetCallbacks();
		
		IJavaThread thread = null;
		try {
			JDIDebugModel.addJavaBreakpointListener(this);		
			thread = launchToBreakpoint("ThrowsNPE");
			assertNotNull(thread);
			// breakpoint should be added & installed
			assertEquals("Breakpoint should be added", 1, fAddCallbacks);
			assertEquals("Breakpoint should be installed", 1, fInstalledCallbacks);
			assertEquals("Breakpoint should not be removed", 0, fRemoveCallbacks);	

			// disable and re-enable the breakpoint
			breakpoint.setEnabled(false);
			breakpoint.setEnabled(true);
			
			// should still be installed/added once
			assertEquals("Breakpoint should be added", 1, fAddCallbacks);
			assertEquals("Breakpoint should be installed", 1, fInstalledCallbacks);
			assertEquals("Breakpoint should not be removed", 0, fRemoveCallbacks);
			
			// change the hit count
			breakpoint.setHitCount(34);
			
			// should still be installed/added once
			assertEquals("Breakpoint should be added", 1, fAddCallbacks);
			assertEquals("Breakpoint should be installed", 1, fInstalledCallbacks);
			assertEquals("Breakpoint should not be removed", 0, fRemoveCallbacks);
			
			// toggle caught/uncaught
			breakpoint.setCaught(false);
			breakpoint.setUncaught(false);
			breakpoint.setCaught(true);
			breakpoint.setUncaught(true);
			
			// should still be installed/added once
			assertEquals("Breakpoint should be added", 1, fAddCallbacks);
			assertEquals("Breakpoint should be installed", 1, fInstalledCallbacks);
			assertEquals("Breakpoint should not be removed", 0, fRemoveCallbacks);			
			
			// delete the breakpoint
			breakpoint.delete();
			
			// should still be installed/added once
			assertEquals("Breakpoint should be added", 1, fAddCallbacks);
			assertEquals("Breakpoint should be installed", 1, fInstalledCallbacks);
			
			// and removed
			assertEquals("Breakpoint should be removed", 1, fRemoveCallbacks);								
			
		} finally {
			JDIDebugModel.removeJavaBreakpointListener(this);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
		
	}	
	
	public void testMethodBreakpoint() throws Exception {		
		IJavaMethodBreakpoint breakpoint = createMethodBreakpoint("DropTests", "method4", "()V", true, false);
		fBreakpoint = breakpoint;
		resetCallbacks();
		
		IJavaThread thread = null;
		try {
			JDIDebugModel.addJavaBreakpointListener(this);		
			thread = launchToBreakpoint("DropTests");
			assertNotNull(thread);
			// breakpoint should be added & installed
			assertEquals("Breakpoint should be added", 1, fAddCallbacks);
			assertEquals("Breakpoint should be installed", 1, fInstalledCallbacks);
			assertEquals("Breakpoint should not be removed", 0, fRemoveCallbacks);	

			// disable and re-enable the breakpoint
			breakpoint.setEnabled(false);
			breakpoint.setEnabled(true);
			
			// should still be installed/added once
			assertEquals("Breakpoint should be added", 1, fAddCallbacks);
			assertEquals("Breakpoint should be installed", 1, fInstalledCallbacks);
			assertEquals("Breakpoint should not be removed", 0, fRemoveCallbacks);
			
			// change the hit count
			breakpoint.setHitCount(34);
			
			// should still be installed/added once
			assertEquals("Breakpoint should be added", 1, fAddCallbacks);
			assertEquals("Breakpoint should be installed", 1, fInstalledCallbacks);
			assertEquals("Breakpoint should not be removed", 0, fRemoveCallbacks);
			
			// toggle entry/exit
			breakpoint.setExit(true);
			breakpoint.setEntry(false);
			breakpoint.setExit(false);
			breakpoint.setEnabled(true);
			
			// should still be installed/added once
			assertEquals("Breakpoint should be added", 1, fAddCallbacks);
			assertEquals("Breakpoint should be installed", 1, fInstalledCallbacks);
			assertEquals("Breakpoint should not be removed", 0, fRemoveCallbacks);			
			
			// delete the breakpoint
			breakpoint.delete();
			
			// should still be installed/added once
			assertEquals("Breakpoint should be added", 1, fAddCallbacks);
			assertEquals("Breakpoint should be installed", 1, fInstalledCallbacks);
			
			// and removed
			assertEquals("Breakpoint should be removed", 1, fRemoveCallbacks);								
			
		} finally {
			JDIDebugModel.removeJavaBreakpointListener(this);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}	
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHasCompilationErrors(org.eclipse.jdt.debug.core.IJavaLineBreakpoint, org.eclipse.jdt.core.dom.Message[])
	 */
	public void breakpointHasCompilationErrors(
		IJavaLineBreakpoint breakpoint,
		Message[] errors) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHasRuntimeException(org.eclipse.jdt.debug.core.IJavaLineBreakpoint, org.eclipse.debug.core.DebugException)
	 */
	public void breakpointHasRuntimeException(
		IJavaLineBreakpoint breakpoint,
		DebugException exception) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHit(org.eclipse.jdt.debug.core.IJavaThread, org.eclipse.jdt.debug.core.IJavaBreakpoint)
	 */
	public boolean breakpointHit(
		IJavaThread thread,
		IJavaBreakpoint breakpoint) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointInstalled(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint)
	 */
	public void breakpointInstalled(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint) {
			if (breakpoint == fBreakpoint) {
				fInstalledCallbacks++;
			}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointRemoved(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint)
	 */
	public void breakpointRemoved(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint) {
			if (breakpoint == fBreakpoint) {
				fRemoveCallbacks++;				
			}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#installingBreakpoint(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint, org.eclipse.jdt.debug.core.IJavaType)
	 */
	public boolean installingBreakpoint(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint,
		IJavaType type) {
			return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#addingBreakpoint(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint)
	 */
	public void addingBreakpoint(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint) {
			if (breakpoint == fBreakpoint) {
				fAddCallbacks++;
			}
	}

}
