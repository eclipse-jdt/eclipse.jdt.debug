/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.debug.testplugin.EvalualtionBreakpointListener;
import org.eclipse.jdt.debug.testplugin.GlobalBreakpointListener;
import org.eclipse.jdt.debug.testplugin.ResumeBreakpointListener;
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
		@Override
		public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		}

		/**
		 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHasCompilationErrors(org.eclipse.jdt.debug.core.IJavaLineBreakpoint, org.eclipse.jdt.core.dom.Message[])
		 */
		@Override
		public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) {
		}

		/**
		 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHasRuntimeException(org.eclipse.jdt.debug.core.IJavaLineBreakpoint, org.eclipse.debug.core.DebugException)
		 */
		@Override
		public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) {
		}

		/**
		 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHit(org.eclipse.jdt.debug.core.IJavaThread, org.eclipse.jdt.debug.core.IJavaBreakpoint)
		 */
		@Override
		public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
			if (breakpoint == fTheBreakpoint) {
				return fVote;
			}
			return DONT_CARE;
		}

		/**
		 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointInstalled(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint)
		 */
		@Override
		public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		}

		/**
		 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointRemoved(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint)
		 */
		@Override
		public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		}

		/**
		 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#installingBreakpoint(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint, org.eclipse.jdt.debug.core.IJavaType)
		 */
		@Override
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
		@Override
		public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
			if (breakpoint.equals(fTheBreakpoint)) {
				return fVote;
			}
			return DONT_CARE;
		}
	}

	/**
	 * Collects hit breakpoints.
	 */
	class Collector implements IJavaBreakpointListener {

		public List<IJavaBreakpoint> HIT = new ArrayList<>();
		public List<IJavaLineBreakpoint> COMPILATION_ERRORS = new ArrayList<>();
		public List<IJavaLineBreakpoint> RUNTIME_ERRORS = new ArrayList<>();

		@Override
		public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		}

		@Override
		public void breakpointHasCompilationErrors( IJavaLineBreakpoint breakpoint, Message[] errors) {
			COMPILATION_ERRORS.add(breakpoint);
		}

		@Override
		public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) {
			RUNTIME_ERRORS.add(breakpoint);
		}

		@Override
		public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
			HIT.add(breakpoint);
			return DONT_CARE;
		}

		@Override
		public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		}

		@Override
		public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		}

		@Override
		public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
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
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(57, "Breakpoints");
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
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(57, "Breakpoints");
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
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(57, "Breakpoints");
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
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(57, "Breakpoints");
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
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(57, "Breakpoints");
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
		IJavaLineBreakpoint breakpoint1 = createLineBreakpoint(57, "Breakpoints");
		IJavaLineBreakpoint breakpoint2 = createLineBreakpoint(58, "Breakpoints");
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
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(57, "Breakpoints");
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
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(57, "Breakpoints");
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
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(57, "Breakpoints");
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
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(57, "Breakpoints");
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
	@Override
	public void breakpointHasCompilationErrors(
		IJavaLineBreakpoint breakpoint,
		Message[] errors) {
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHasRuntimeException(org.eclipse.jdt.debug.core.IJavaLineBreakpoint, org.eclipse.debug.core.DebugException)
	 */
	@Override
	public void breakpointHasRuntimeException(
		IJavaLineBreakpoint breakpoint,
		DebugException exception) {
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHit(org.eclipse.jdt.debug.core.IJavaThread, org.eclipse.jdt.debug.core.IJavaBreakpoint)
	 */
	@Override
	public int breakpointHit(
		IJavaThread thread,
		IJavaBreakpoint breakpoint) {
		return DONT_CARE;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointInstalled(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint)
	 */
	@Override
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
	@Override
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
	@Override
	public int installingBreakpoint(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint,
		IJavaType type) {
			return DONT_CARE;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#addingBreakpoint(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.debug.core.IJavaBreakpoint)
	 */
	@Override
	public void addingBreakpoint(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint) {
			if (breakpoint == fBreakpoint) {
				fAddCallbacks++;
			}
	}

	/**
	 * Tests a breakpoint listener extension.
	 */
	public void testEvalListenerExtension() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createLineBreakpoint(19, typeName);
		bp.addBreakpointListener("org.eclipse.jdt.debug.tests.evalListener");
		EvalualtionBreakpointListener.reset();
		EvalualtionBreakpointListener.PROJECT = get14Project();
		EvalualtionBreakpointListener.EXPRESSION = "return new Integer(i);";
		EvalualtionBreakpointListener.VOTE = IJavaBreakpointListener.SUSPEND;

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IEvaluationResult result = EvalualtionBreakpointListener.RESULT;
			assertNotNull("Missing eval result", result);
			assertFalse("Should be no errors", result.hasErrors());
			IJavaValue value = result.getValue();
			assertEquals("Wrong result type", "java.lang.Integer", value.getReferenceTypeName());
			IVariable[] variables = value.getVariables();
			for (int i = 0; i < variables.length; i++) {
				IVariable variable = variables[i];
				if (variable.getName().equals("value")) {
					IValue iValue = variable.getValue();
					assertTrue("Should be an int", iValue.getReferenceTypeName().equals("int"));
					assertTrue("Should be a primitive", iValue instanceof IJavaPrimitiveValue);
					int intValue = ((IJavaPrimitiveValue)iValue).getIntValue();
					assertEquals("Wrong value", 0, intValue);
					break;
				}
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that a step end that lands on a breakpoint listener that votes to resume
	 * results in the step completing and suspending.
	 *
	 * @throws Exception
	 */
	public void testStepEndResumeVote() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint first = createLineBreakpoint(19, typeName);
		IJavaLineBreakpoint second = createLineBreakpoint(20, typeName);
		second.addBreakpointListener("org.eclipse.jdt.debug.tests.resumeListener");

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, first);
			thread = stepOver((IJavaStackFrame) thread.getTopStackFrame());
			assertTrue("Listener not notified", ResumeBreakpointListener.WAS_HIT);
			assertTrue("Thread should be suspended", thread.isSuspended());
			assertNotNull("Top frame should be available", thread.getTopStackFrame());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Test that a step over hitting a breakpoint deeper up the stack with a listener
	 * can perform an evaluation and resume to complete the step.
	 *
	 * @throws Exception
	 */
	public void testStepOverHitsNestedEvaluationHandlerResume() throws Exception {
		String typeName = "MethodLoop";
		// breakpoint on line 24 is where the step is initiated from
		IJavaLineBreakpoint first = createLineBreakpoint(27, typeName);
		// second breakpoint is where the evaluation is performed with a resume vote
		IJavaLineBreakpoint second = createLineBreakpoint(32, typeName);
		second.addBreakpointListener("org.eclipse.jdt.debug.tests.evalListener");
		EvalualtionBreakpointListener.reset();
		EvalualtionBreakpointListener.PROJECT = get14Project();
		EvalualtionBreakpointListener.EXPRESSION = "return new Integer(sum);";
		EvalualtionBreakpointListener.VOTE = IJavaBreakpointListener.DONT_SUSPEND;
		EvalualtionBreakpointListener.RESULT = null;

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, first);
			IStackFrame top = thread.getTopStackFrame();
			assertNotNull("Missing top frame", top);
			thread = stepOver((IJavaStackFrame) top);
			assertTrue("Thread should be suspended", thread.isSuspended());
			assertEquals("Should be in frame where step originated", top, thread.getTopStackFrame());
			IEvaluationResult result = EvalualtionBreakpointListener.RESULT;
			assertNotNull("Missing eval result", result);
			assertFalse("Should be no errors", result.hasErrors());
			if (result.getException() != null) {
				result.getException().printStackTrace();
			}
			IJavaValue value = result.getValue();
			assertEquals("Wrong result type", "java.lang.Integer", value.getReferenceTypeName());
			IVariable[] variables = value.getVariables();
			for (int i = 0; i < variables.length; i++) {
				IVariable variable = variables[i];
				if (variable.getName().equals("value")) {
					IValue iValue = variable.getValue();
					assertTrue("Should be an int", iValue.getReferenceTypeName().equals("int"));
					assertTrue("Should be a primitive", iValue instanceof IJavaPrimitiveValue);
					int intValue = ((IJavaPrimitiveValue)iValue).getIntValue();
					assertEquals("Wrong value", 0, intValue);
					break;
				}
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Test that a step over hitting a breakpoint deeper up the stack with a listener
	 * can perform an evaluation and suspend to abort the step.
	 *
	 * @throws Exception
	 */
	public void testStepOverHitsNestedEvaluationHandlerSuspend() throws Exception {
		String typeName = "MethodLoop";
		// breakpoint on line 24 is where the step is initiated from
		IJavaLineBreakpoint first = createLineBreakpoint(27, typeName);
		// second breakpoint is where the evaluation is performed with a resume vote
		IJavaLineBreakpoint second = createLineBreakpoint(32, typeName);
		second.addBreakpointListener("org.eclipse.jdt.debug.tests.evalListener");
		EvalualtionBreakpointListener.reset();
		EvalualtionBreakpointListener.PROJECT = get14Project();
		EvalualtionBreakpointListener.EXPRESSION = "return new Integer(sum);";
		EvalualtionBreakpointListener.VOTE = IJavaBreakpointListener.SUSPEND;
		EvalualtionBreakpointListener.RESULT = null;

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, first);
			IStackFrame top = thread.getTopStackFrame();
			assertNotNull("Missing top frame", top);
			thread = stepOverToBreakpoint((IJavaStackFrame) top);
			assertTrue("Thread should be suspended", thread.isSuspended());
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull("Missin top frame", frame);
			assertEquals("Wrong location", "calculateSum", frame.getName());
			IEvaluationResult result = EvalualtionBreakpointListener.RESULT;
			assertNotNull("Missing eval result", result);
			assertFalse("Should be no errors", result.hasErrors());
			if (result.getException() != null) {
				result.getException().printStackTrace();
			}
			IJavaValue value = result.getValue();
			assertEquals("Wrong result type", "java.lang.Integer", value.getReferenceTypeName());
			IVariable[] variables = value.getVariables();
			for (int i = 0; i < variables.length; i++) {
				IVariable variable = variables[i];
				if (variable.getName().equals("value")) {
					IValue iValue = variable.getValue();
					assertTrue("Should be an int", iValue.getReferenceTypeName().equals("int"));
					assertTrue("Should be a primitive", iValue instanceof IJavaPrimitiveValue);
					int intValue = ((IJavaPrimitiveValue)iValue).getIntValue();
					assertEquals("Wrong value", 0, intValue);
					break;
				}
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Suspends an evaluation. Ensures we're returned to the proper top frame.
	 *
	 * @throws Exception
	 */
	public void testSuspendEvaluation() throws Exception {
		String typeName = "MethodLoop";
		IJavaLineBreakpoint first = createLineBreakpoint(22, typeName);
		IJavaLineBreakpoint second = createLineBreakpoint(32, typeName);
		second.addBreakpointListener("org.eclipse.jdt.debug.tests.evalListener");
		EvalualtionBreakpointListener.reset();
		EvalualtionBreakpointListener.PROJECT = get14Project();
		EvalualtionBreakpointListener.EXPRESSION = "for (int x = 0; x < 1000; x++) { System.out.println(x);} Thread.sleep(200);";
		EvalualtionBreakpointListener.VOTE = IJavaBreakpointListener.DONT_SUSPEND;
		EvalualtionBreakpointListener.RESULT = null;

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, first);
			IStackFrame top = thread.getTopStackFrame();
			assertNotNull("Missing top frame", top);
			thread.resume();
			Thread.sleep(100);
			thread.suspend();
			assertTrue("Thread should be suspended", thread.isSuspended());
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull("Missing top frame", frame);
			assertEquals("Wrong location", "calculateSum", frame.getName());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Test that a global listener gets notifications.
	 *
	 * @throws Exception
	 */
	public void testGlobalListener() throws Exception {
		GlobalBreakpointListener.clear();
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createLineBreakpoint(19, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			assertTrue("Should be ADDED", GlobalBreakpointListener.ADDED.contains(bp));
			assertTrue("Should be INSTALLING", GlobalBreakpointListener.INSTALLING.contains(bp));
			assertTrue("Should be INSTALLED", GlobalBreakpointListener.INSTALLED.contains(bp));
			assertTrue("Should be HIT", GlobalBreakpointListener.HIT.contains(bp));
			assertFalse("Should not be REMOVED", GlobalBreakpointListener.REMOVED.contains(bp));

			bp.delete();
			assertTrue("Should be REMOVED", GlobalBreakpointListener.REMOVED.contains(bp));
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that breakpoint listeners are only notified when condition is true.
	 *
	 * @throws Exception
	 */
	public void testListenersOnConditionalBreakpoint() throws Exception {
		String typeName = "HitCountLooper";
		Collector collector = new Collector();
		JDIDebugModel.addJavaBreakpointListener(collector);
		IJavaLineBreakpoint bp = createConditionalLineBreakpoint(19, typeName, "return false;", true);
		IJavaLineBreakpoint second = createConditionalLineBreakpoint(20, typeName, "i == 3", true);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, second);
			assertEquals("Wrong number of breakpoints hit", 1, collector.HIT.size());
			assertTrue("Wrong breakpoint hit", collector.HIT.contains(second));
			assertFalse("Wrong breakpoint hit", collector.HIT.contains(bp));
		} finally {
			JDIDebugModel.removeJavaBreakpointListener(collector);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that breakpoint listeners are only notified when condition is true
	 * while stepping to a breakpoint.
	 *
	 * @throws Exception
	 */
	public void testListenersOnConditionalBreakpointStepping() throws Exception {
		String typeName = "HitCountLooper";
		Collector collector = new Collector();
		JDIDebugModel.addJavaBreakpointListener(collector);
		IJavaLineBreakpoint bp = createLineBreakpoint(19, typeName);
		IJavaLineBreakpoint second = createConditionalLineBreakpoint(20, typeName, "i == 1", true);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			// step over to next line with conditional (should be false && not hit)
			thread = stepOver((IJavaStackFrame) thread.getTopStackFrame());
			assertEquals("Wrong number of breakpoints hit", 1, collector.HIT.size());
			assertTrue("Wrong breakpoint hit", collector.HIT.contains(bp));
			assertFalse("Wrong breakpoint hit", collector.HIT.contains(second));
			collector.HIT.clear();

			// resume to line breakpoint again
			thread = resumeToLineBreakpoint(thread, bp);
			// step over to next line with conditional (should be true && hit)
			thread = stepOver((IJavaStackFrame) thread.getTopStackFrame());
			assertEquals("Wrong number of breakpoints hit", 2, collector.HIT.size());
			assertTrue("Wrong breakpoint hit", collector.HIT.contains(bp));
			assertTrue("Wrong breakpoint hit", collector.HIT.contains(second));

		} finally {
			JDIDebugModel.removeJavaBreakpointListener(collector);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that breakpoint listeners are not notified of "hit" when condition has compilation
	 * errors. Also they should be notified of the compilation errors.
	 *
	 * @throws Exception
	 */
	public void testListenersOnCompilationError() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createConditionalLineBreakpoint(20, typeName, "x == 1", true);
		bp.addBreakpointListener("org.eclipse.jdt.debug.tests.evalListener");
		EvalualtionBreakpointListener.reset();

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			assertFalse(EvalualtionBreakpointListener.HIT);
			assertEquals(1, EvalualtionBreakpointListener.COMPILATION_ERRORS.size());
			assertEquals(bp, EvalualtionBreakpointListener.COMPILATION_ERRORS.get(0));
			assertEquals(0, EvalualtionBreakpointListener.RUNTIME_ERRORS.size());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that breakpoint listeners are not notified of "hit" when condition has compilation
	 * errors. Also they should be notified of the compilation errors.
	 *
	 * @throws Exception
	 */
	public void testListenersOnRuntimeError() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createConditionalLineBreakpoint(17, typeName, "(new String()).charAt(34) == 'c'", true);
		bp.addBreakpointListener("org.eclipse.jdt.debug.tests.evalListener");
		EvalualtionBreakpointListener.reset();

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			assertFalse(EvalualtionBreakpointListener.HIT);
			assertEquals(1, EvalualtionBreakpointListener.RUNTIME_ERRORS.size());
			assertEquals(bp, EvalualtionBreakpointListener.RUNTIME_ERRORS.get(0));
			assertEquals(0, EvalualtionBreakpointListener.COMPILATION_ERRORS.size());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests addition and removal of breakpoint listeners to a breakpoint.
	 *
	 * @throws Exception
	 */
	public void testAddRemoveListeners() throws Exception {
		try {
			String typeName = "HitCountLooper";
			IJavaLineBreakpoint bp = createLineBreakpoint(16, typeName);

			String[] listeners = bp.getBreakpointListeners();
			assertEquals(0, listeners.length);

			bp.addBreakpointListener("a");
			listeners = bp.getBreakpointListeners();
			assertEquals(1, listeners.length);
			assertEquals("a", listeners[0]);

			bp.addBreakpointListener("b");
			listeners = bp.getBreakpointListeners();
			assertEquals(2, listeners.length);
			assertEquals("a", listeners[0]);
			assertEquals("b", listeners[1]);

			bp.addBreakpointListener("c");
			listeners = bp.getBreakpointListeners();
			assertEquals(3, listeners.length);
			assertEquals("a", listeners[0]);
			assertEquals("b", listeners[1]);
			assertEquals("c", listeners[2]);

			bp.removeBreakpointListener("a");
			listeners = bp.getBreakpointListeners();
			assertEquals(2, listeners.length);
			assertEquals("b", listeners[0]);
			assertEquals("c", listeners[1]);

			bp.removeBreakpointListener("c");
			listeners = bp.getBreakpointListeners();
			assertEquals(1, listeners.length);
			assertEquals("b", listeners[0]);

			bp.removeBreakpointListener("b");
			listeners = bp.getBreakpointListeners();
			assertEquals(0, listeners.length);
		} finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests addition of duplicate breakpoint listeners to a breakpoint.
	 *
	 * @throws Exception
	 */
	public void testAddDuplicateListeners() throws Exception {
		try {
			String typeName = "HitCountLooper";
			IJavaLineBreakpoint bp = createLineBreakpoint(19, typeName);

			String[] listeners = bp.getBreakpointListeners();
			assertEquals(0, listeners.length);

			bp.addBreakpointListener("a");
			listeners = bp.getBreakpointListeners();
			assertEquals(1, listeners.length);
			assertEquals("a", listeners[0]);

			bp.addBreakpointListener("a");
			listeners = bp.getBreakpointListeners();
			assertEquals(1, listeners.length);
			assertEquals("a", listeners[0]);

			bp.addBreakpointListener("b");
			listeners = bp.getBreakpointListeners();
			assertEquals(2, listeners.length);
			assertEquals("a", listeners[0]);
			assertEquals("b", listeners[1]);
		} finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that listeners can be retrieved after breakpoint deletion.
	 *
	 * @throws Exception
	 */
	public void testGetListenersAfterDelete() throws Exception {
		try {
			String typeName = "HitCountLooper";
			IJavaLineBreakpoint bp = createLineBreakpoint(19, typeName);

			String[] listeners = bp.getBreakpointListeners();
			assertEquals(0, listeners.length);

			bp.addBreakpointListener("a");
			bp.addBreakpointListener("b");

			listeners = bp.getBreakpointListeners();
			assertEquals(2, listeners.length);
			assertEquals("a", listeners[0]);
			assertEquals("b", listeners[1]);

			bp.delete();
			listeners = bp.getBreakpointListeners();
			assertEquals(2, listeners.length);
			assertEquals("a", listeners[0]);
			assertEquals("b", listeners[1]);

		} finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a breakpoint listener extension gets removal notification when the underlying
	 * marker is deleted.
	 */
	public void testRemovedNotification() throws Exception {
		String typeName = "HitCountLooper";
		final IJavaLineBreakpoint bp = createLineBreakpoint(17, typeName);
		bp.addBreakpointListener("org.eclipse.jdt.debug.tests.evalListener");
		EvalualtionBreakpointListener.reset();
		EvalualtionBreakpointListener.VOTE = SUSPEND;

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			assertTrue(EvalualtionBreakpointListener.HIT);
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					bp.getMarker().delete();
					get14Project().getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
				}
			};
			ResourcesPlugin.getWorkspace().run(runnable, null);
			synchronized (EvalualtionBreakpointListener.REMOVE_LOCK) {
				if (!EvalualtionBreakpointListener.REMOVED) {
					EvalualtionBreakpointListener.REMOVE_LOCK.wait(10000);
					if (!EvalualtionBreakpointListener.REMOVED) {
						System.out.println("oops");
					}
				}
			}
			assertTrue(EvalualtionBreakpointListener.REMOVED);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

}
