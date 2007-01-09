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
package org.eclipse.jdt.debug.tests.breakpoints;

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
	
	/**
	 * count of add callbacks
	 */
	public int fAddCallbacks = 0;
	
	/**
	 * count of remove callbacks
	 */
	public int fRemoveCallbacks = 0;
	
	/**
	 * count of installed callbacks
	 */
	public int fInstalledCallbacks = 0;
	
	/** 
	 * a java breakpoint
	 */
	public IJavaBreakpoint fBreakpoint;
	
	/**
	 * Used to test breakpoint install/suspend voting.
	 */
	class SuspendVoter implements IJavaBreakpointListener {
		
		int fVote;
		IJavaBreakpoint fTheBreakpoint;
		
		/**
		 * Constructor
		 * @param suspendVote
		 * @param breakpoint
		 */
		public SuspendVoter(int suspendVote, IJavaBreakpoint breakpoint) {
			fVote = suspendVote;
			fTheBreakpoint = breakpoint;
		}
		
		/**
		 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#addingBreakpoint(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint)
		 */
		public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		}

		/**
		 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHasCompilationErrors(org.eclipse.jdt.debug.core.IJavaLineBreakpoint, org.eclipse.jdt.core.dom.Message[])
		 */
		public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) {
		}

		/**
		 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHasRuntimeException(org.eclipse.jdt.debug.core.IJavaLineBreakpoint, org.eclipse.debug.core.DebugException)
		 */
		public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) {
		}

		/**
		 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHit(org.eclipse.jdt.debug.core.IJavaThread, org.eclipse.jdt.debug.core.IJavaBreakpoint)
		 */
		public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
			if (breakpoint == fTheBreakpoint) {
				return fVote;
			}
			return DONT_CARE;
		}

		/**
		 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointInstalled(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint)
		 */
		public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		}

		/**
		 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointRemoved(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint)
		 */
		public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		}

		/**
		 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#installingBreakpoint(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint, org.eclipse.jdt.debug.core.IJavaType)
		 */
		public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
			return DONT_CARE;
		}

	}
	
	/**
	 * Aids in the voting for a breakpoint to be installed
	 */
	class InstallVoter extends SuspendVoter {
		
		/**
		 * Constructor
		 * @param installVote
		 * @param breakpoint
		 */
		public InstallVoter(int installVote, IJavaBreakpoint breakpoint) {
			super(installVote, breakpoint);
		}
		
		/**
		 * @see org.eclipse.jdt.debug.tests.breakpoints.JavaBreakpointListenerTests.SuspendVoter#installingBreakpoint(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint, org.eclipse.jdt.debug.core.IJavaType)
		 */
		public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
			if (breakpoint.equals(fTheBreakpoint)) {
				return fVote;
			}
			return DONT_CARE;
		}
	}
			
	/**
	 * Constructor
	 * @param name
	 */
	public JavaBreakpointListenerTests(String name) {
		super(name);
	}

	/**
	 * Resets the number of callbacks to zero
	 */
	protected void resetCallbacks() {
		fAddCallbacks = 0;
	
		fRemoveCallbacks = 0;
	
		fInstalledCallbacks = 0;	
	}
	
	/**
	 * Tests the functionality of a single line breakpoint
	 * @throws Exception
	 */
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
	
	/**
	 * Tests the functionality of an exception breakpoint
	 * @throws Exception
	 */
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
	
	/**
	 * Tests the functionality of a method breakpoint
	 * @throws Exception
	 */
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
	
	/**
	 * Vote: Install 3, Don't Care 0, Don't Install 0 == INSTALL
	 * @throws Exception
	 */
	public void testUnanimousInstallVote() throws Exception {
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(54, "Breakpoints");
		InstallVoter v1 = new InstallVoter(SUSPEND, breakpoint);
		InstallVoter v2 = new InstallVoter(SUSPEND, breakpoint);
		InstallVoter v3 = new InstallVoter(SUSPEND, breakpoint);
		JDIDebugModel.addJavaBreakpointListener(v1);
		JDIDebugModel.addJavaBreakpointListener(v2);
		JDIDebugModel.addJavaBreakpointListener(v3);
		IJavaThread thread = null;
		try {		
			thread = launchToBreakpoint("Breakpoints");
			assertNotNull(thread);
			assertEquals(breakpoint, thread.getBreakpoints()[0]);
		} finally {
			JDIDebugModel.removeJavaBreakpointListener(v1);
			JDIDebugModel.removeJavaBreakpointListener(v2);
			JDIDebugModel.removeJavaBreakpointListener(v3);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	/**
	 * Vote: Install 0, Don't Care 3, Don't Install 0 == INSTALL
	 * @throws Exception
	 */
	public void testDontCareInstallVote() throws Exception {
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(54, "Breakpoints");
		InstallVoter v1 = new InstallVoter(DONT_CARE, breakpoint);
		InstallVoter v2 = new InstallVoter(DONT_CARE, breakpoint);
		InstallVoter v3 = new InstallVoter(DONT_CARE, breakpoint);
		JDIDebugModel.addJavaBreakpointListener(v1);
		JDIDebugModel.addJavaBreakpointListener(v2);
		JDIDebugModel.addJavaBreakpointListener(v3);
		IJavaThread thread = null;
		try {		
			thread = launchToBreakpoint("Breakpoints");
			assertNotNull(thread);
			assertEquals(breakpoint, thread.getBreakpoints()[0]);
		} finally {
			JDIDebugModel.removeJavaBreakpointListener(v1);
			JDIDebugModel.removeJavaBreakpointListener(v2);
			JDIDebugModel.removeJavaBreakpointListener(v3);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	
	
	/**
	 * Vote: Install 1, Don't Care 2, Don't Install 0 == INSTALL
	 * @throws Exception
	 */
	public void testInstallDontCareVote() throws Exception {
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(54, "Breakpoints");
		InstallVoter v1 = new InstallVoter(SUSPEND, breakpoint);
		InstallVoter v2 = new InstallVoter(DONT_CARE, breakpoint);
		InstallVoter v3 = new InstallVoter(DONT_CARE, breakpoint);
		JDIDebugModel.addJavaBreakpointListener(v1);
		JDIDebugModel.addJavaBreakpointListener(v2);
		JDIDebugModel.addJavaBreakpointListener(v3);
		IJavaThread thread = null;
		try {		
			thread = launchToBreakpoint("Breakpoints");
			assertNotNull(thread);
			assertEquals(breakpoint, thread.getBreakpoints()[0]);
		} finally {
			JDIDebugModel.removeJavaBreakpointListener(v1);
			JDIDebugModel.removeJavaBreakpointListener(v2);
			JDIDebugModel.removeJavaBreakpointListener(v3);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}		
		
	/**
	 * Vote: Install 1, Don't Care 0, Don't Install 2 == INSTALL
	 * @throws Exception
	 */
	public void testInstallDontVote() throws Exception {
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(54, "Breakpoints");
		InstallVoter v1 = new InstallVoter(SUSPEND, breakpoint);
		InstallVoter v2 = new InstallVoter(DONT_SUSPEND, breakpoint);
		InstallVoter v3 = new InstallVoter(DONT_SUSPEND, breakpoint);
		JDIDebugModel.addJavaBreakpointListener(v1);
		JDIDebugModel.addJavaBreakpointListener(v2);
		JDIDebugModel.addJavaBreakpointListener(v3);
		IJavaThread thread = null;
		try {		
			thread = launchToBreakpoint("Breakpoints");
			assertNotNull(thread);
			assertEquals(breakpoint, thread.getBreakpoints()[0]);
		} finally {
			JDIDebugModel.removeJavaBreakpointListener(v1);
			JDIDebugModel.removeJavaBreakpointListener(v2);
			JDIDebugModel.removeJavaBreakpointListener(v3);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	
	
	/**
	 * Vote: Install 0, Don't Care 1, Don't Install 2 = RESUME
	 * @throws Exception
	 */
	public void testDontInstallVote() throws Exception {
		IJavaLineBreakpoint breakpoint1 = createLineBreakpoint(54, "Breakpoints");
		IJavaLineBreakpoint breakpoint2 = createLineBreakpoint(55, "Breakpoints");
		InstallVoter v1 = new InstallVoter(DONT_CARE, breakpoint1);
		InstallVoter v2 = new InstallVoter(DONT_SUSPEND, breakpoint1);
		InstallVoter v3 = new InstallVoter(DONT_SUSPEND, breakpoint1);
		JDIDebugModel.addJavaBreakpointListener(v1);
		JDIDebugModel.addJavaBreakpointListener(v2);
		JDIDebugModel.addJavaBreakpointListener(v3);
		IJavaThread thread = null;
		try {		
			thread = launchToBreakpoint("Breakpoints");
			assertNotNull(thread);
			assertEquals(breakpoint2, thread.getBreakpoints()[0]);
		} finally {
			JDIDebugModel.removeJavaBreakpointListener(v1);
			JDIDebugModel.removeJavaBreakpointListener(v2);
			JDIDebugModel.removeJavaBreakpointListener(v3);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	/**
	 * Vote: Suspend 3, Don't Care 0, Don't Suspend 0 == SUSPEND
	 * @throws Exception
	 */
	public void testUnanimousSuspendVote() throws Exception {
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(54, "Breakpoints");
		SuspendVoter v1 = new SuspendVoter(SUSPEND, breakpoint);
		SuspendVoter v2 = new SuspendVoter(SUSPEND, breakpoint);
		SuspendVoter v3 = new SuspendVoter(SUSPEND, breakpoint);
		JDIDebugModel.addJavaBreakpointListener(v1);
		JDIDebugModel.addJavaBreakpointListener(v2);
		JDIDebugModel.addJavaBreakpointListener(v3);
		IJavaThread thread = null;
		try {		
			thread = launchToBreakpoint("Breakpoints");
			assertNotNull(thread);
			assertEquals(breakpoint, thread.getBreakpoints()[0]);
		} finally {
			JDIDebugModel.removeJavaBreakpointListener(v1);
			JDIDebugModel.removeJavaBreakpointListener(v2);
			JDIDebugModel.removeJavaBreakpointListener(v3);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	/**
	 * Vote: Suspend 0, Don't Care 3, Don't Suspend 0 == SUSPEND
	 * @throws Exception
	 */
	public void testDontCareSuspendVote() throws Exception {
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(54, "Breakpoints");
		SuspendVoter v1 = new SuspendVoter(DONT_CARE, breakpoint);
		SuspendVoter v2 = new SuspendVoter(DONT_CARE, breakpoint);
		SuspendVoter v3 = new SuspendVoter(DONT_CARE, breakpoint);
		JDIDebugModel.addJavaBreakpointListener(v1);
		JDIDebugModel.addJavaBreakpointListener(v2);
		JDIDebugModel.addJavaBreakpointListener(v3);
		IJavaThread thread = null;
		try {		
			thread = launchToBreakpoint("Breakpoints");
			assertNotNull(thread);
			assertEquals(breakpoint, thread.getBreakpoints()[0]);
		} finally {
			JDIDebugModel.removeJavaBreakpointListener(v1);
			JDIDebugModel.removeJavaBreakpointListener(v2);
			JDIDebugModel.removeJavaBreakpointListener(v3);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	
	
	/**
	 * Vote: Suspend 1, Don't Care 2, Don't Suspend 0 == SUSPEND
	 * @throws Exception
	 */
	public void testSuspendDontCareVote() throws Exception {
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(54, "Breakpoints");
		SuspendVoter v1 = new SuspendVoter(SUSPEND, breakpoint);
		SuspendVoter v2 = new SuspendVoter(DONT_CARE, breakpoint);
		SuspendVoter v3 = new SuspendVoter(DONT_CARE, breakpoint);
		JDIDebugModel.addJavaBreakpointListener(v1);
		JDIDebugModel.addJavaBreakpointListener(v2);
		JDIDebugModel.addJavaBreakpointListener(v3);
		IJavaThread thread = null;
		try {		
			thread = launchToBreakpoint("Breakpoints");
			assertNotNull(thread);
			assertEquals(breakpoint, thread.getBreakpoints()[0]);
		} finally {
			JDIDebugModel.removeJavaBreakpointListener(v1);
			JDIDebugModel.removeJavaBreakpointListener(v2);
			JDIDebugModel.removeJavaBreakpointListener(v3);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}		
		
	/**
	 * Vote: Suspend 1, Don't Care 0, Don't Suspend 2 == SUSPEND
	 * @throws Exception
	 */
	public void testSuspendDontVote() throws Exception {
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(54, "Breakpoints");
		SuspendVoter v1 = new SuspendVoter(SUSPEND, breakpoint);
		SuspendVoter v2 = new SuspendVoter(DONT_SUSPEND, breakpoint);
		SuspendVoter v3 = new SuspendVoter(DONT_SUSPEND, breakpoint);
		JDIDebugModel.addJavaBreakpointListener(v1);
		JDIDebugModel.addJavaBreakpointListener(v2);
		JDIDebugModel.addJavaBreakpointListener(v3);
		IJavaThread thread = null;
		try {		
			thread = launchToBreakpoint("Breakpoints");
			assertNotNull(thread);
			assertEquals(breakpoint, thread.getBreakpoints()[0]);
		} finally {
			JDIDebugModel.removeJavaBreakpointListener(v1);
			JDIDebugModel.removeJavaBreakpointListener(v2);
			JDIDebugModel.removeJavaBreakpointListener(v3);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	
	
	/**
	 * Vote: Suspend 0, Don't Care 1, Don't Suspend 2 = RESUME
	 * @throws Exception
	 */
	public void testDontSuspendVote() throws Exception {
		IJavaLineBreakpoint breakpoint1 = createLineBreakpoint(54, "Breakpoints");
		IJavaLineBreakpoint breakpoint2 = createLineBreakpoint(55, "Breakpoints");
		SuspendVoter v1 = new SuspendVoter(DONT_CARE, breakpoint1);
		SuspendVoter v2 = new SuspendVoter(DONT_SUSPEND, breakpoint1);
		SuspendVoter v3 = new SuspendVoter(DONT_SUSPEND, breakpoint1);
		JDIDebugModel.addJavaBreakpointListener(v1);
		JDIDebugModel.addJavaBreakpointListener(v2);
		JDIDebugModel.addJavaBreakpointListener(v3);
		IJavaThread thread = null;
		try {		
			thread = launchToBreakpoint("Breakpoints");
			assertNotNull(thread);
			assertEquals(breakpoint2, thread.getBreakpoints()[0]);
		} finally {
			JDIDebugModel.removeJavaBreakpointListener(v1);
			JDIDebugModel.removeJavaBreakpointListener(v2);
			JDIDebugModel.removeJavaBreakpointListener(v3);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	
	
	/**
	 * Vote: Suspend 0, Don't Care 1 (java debug options manager), Don't Suspend 1 = RESUME
	 * @throws Exception
	 */
	public void testMethodBreakpointDontSuspendVote() throws Exception {
		IJavaMethodBreakpoint breakpoint1 = createMethodBreakpoint("DropTests", "method2", "()V", true, false);
		IJavaMethodBreakpoint breakpoint2 = createMethodBreakpoint("DropTests", "method4", "()V", true, false);
		SuspendVoter v1 = new SuspendVoter(DONT_SUSPEND, breakpoint1);
		JDIDebugModel.addJavaBreakpointListener(v1);
		IJavaThread thread = null;
		try {		
			thread = launchToBreakpoint("DropTests");
			assertNotNull(thread);
			assertEquals(breakpoint2, thread.getBreakpoints()[0]);
		} finally {
			JDIDebugModel.removeJavaBreakpointListener(v1);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}		
	
	/**
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHasCompilationErrors(org.eclipse.jdt.debug.core.IJavaLineBreakpoint, org.eclipse.jdt.core.dom.Message[])
	 */
	public void breakpointHasCompilationErrors(
		IJavaLineBreakpoint breakpoint,
		Message[] errors) {
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHasRuntimeException(org.eclipse.jdt.debug.core.IJavaLineBreakpoint, org.eclipse.debug.core.DebugException)
	 */
	public void breakpointHasRuntimeException(
		IJavaLineBreakpoint breakpoint,
		DebugException exception) {
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHit(org.eclipse.jdt.debug.core.IJavaThread, org.eclipse.jdt.debug.core.IJavaBreakpoint)
	 */
	public int breakpointHit(
		IJavaThread thread,
		IJavaBreakpoint breakpoint) {
		return DONT_CARE;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointInstalled(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint)
	 */
	public void breakpointInstalled(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint) {
			if (breakpoint == fBreakpoint) {
				fInstalledCallbacks++;
			}
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointRemoved(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint)
	 */
	public void breakpointRemoved(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint) {
			if (breakpoint == fBreakpoint) {
				fRemoveCallbacks++;				
			}
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#installingBreakpoint(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint, org.eclipse.jdt.debug.core.IJavaType)
	 */
	public int installingBreakpoint(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint,
		IJavaType type) {
			return DONT_CARE;
	}

	/**
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
