/*******************************************************************************
 * Copyright (c) 2026 Carsten Hammer and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Carsten Hammer - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.EvalualtionBreakpointListener;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

/**
 * Tests cleanup of Java breakpoints whose marker has been deleted or detached.
 */
public class BreakpointRemovalTests extends AbstractDebugTest {

	private static final String NO_ASSOCIATED_MARKER = "Breakpoint does not have an associated marker"; //$NON-NLS-1$
	private static final String DELETED_MARKER = "Breakpoint marker does not exist"; //$NON-NLS-1$
	private static final String TEST_LISTENER = "org.eclipse.jdt.debug.tests.evalListener"; //$NON-NLS-1$

	public BreakpointRemovalTests(String name) {
		super(name);
	}

	/**
	 * Tests the complete marker-deletion lifecycle while a debug target is
	 * suspended at the deleted breakpoint.
	 */
	public void testMarkerDeletionCleansTargetAndAllowsReplacementBreakpoint() throws Exception {
		String typeName = "HitCountLooper"; //$NON-NLS-1$
		int loopLine = 19;
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(loopLine, typeName);
		breakpoint.addBreakpointListener(TEST_LISTENER);
		EvalualtionBreakpointListener.reset();
		EvalualtionBreakpointListener.VOTE = IJavaBreakpointListener.SUSPEND;

		List<IStatus> markerErrors = Collections.synchronizedList(new ArrayList<>());
		ILogListener logListener = (status, plugin) -> {
			if (containsMarkerError(status)) {
				markerErrors.add(status);
			}
		};
		IJavaThread thread = null;
		boolean logListenerRegistered = false;
		try {
			thread = launchToLineBreakpoint(typeName, breakpoint);
			assertNotNull("Breakpoint was not hit", thread); //$NON-NLS-1$
			JDIDebugTarget target = (JDIDebugTarget) thread.getDebugTarget();
			assertTrue("Breakpoint should be tracked by the debug target", containsByIdentity(target.getBreakpoints(), breakpoint)); //$NON-NLS-1$
			assertTrue("Breakpoint should be current in the suspended thread", containsByIdentity(thread.getBreakpoints(), breakpoint)); //$NON-NLS-1$

			Platform.addLogListener(logListener);
			logListenerRegistered = true;
			IWorkspaceRunnable runnable = monitor -> {
				breakpoint.getMarker().delete();
				get14Project().getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			};
			ResourcesPlugin.getWorkspace().run(runnable, null);

			waitForRemovalNotification();
			assertTrue("Breakpoint removal listener was not notified", EvalualtionBreakpointListener.REMOVED); //$NON-NLS-1$
			waitForCleanup(target, thread, breakpoint);
			assertFalse("Deleted breakpoint remained in the debug target", containsByIdentity(target.getBreakpoints(), breakpoint)); //$NON-NLS-1$
			assertFalse("Deleted breakpoint remained current in the suspended thread", containsByIdentity(thread.getBreakpoints(), breakpoint)); //$NON-NLS-1$
			assertTrue("Unexpected marker error while deleting breakpoint: " + markerErrors, markerErrors.isEmpty()); //$NON-NLS-1$

			IJavaLineBreakpoint replacement = createLineBreakpoint(loopLine, typeName);
			assertTrue("Replacement breakpoint was not installed in the debug target", containsByIdentity(target.getBreakpoints(), replacement)); //$NON-NLS-1$
			thread = resumeToLineBreakpoint(thread, replacement);
			assertNotNull("Replacement breakpoint was not hit", thread); //$NON-NLS-1$
			assertTrue("Replacement breakpoint should be current in the suspended thread", containsByIdentity(thread.getBreakpoints(), replacement)); //$NON-NLS-1$
			assertFalse("Deleted breakpoint became current again", containsByIdentity(thread.getBreakpoints(), breakpoint)); //$NON-NLS-1$
		} finally {
			if (logListenerRegistered) {
				Platform.removeLogListener(logListener);
			}
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests target-side cleanup when a removal notification carries a line
	 * breakpoint whose marker association has already been cleared.
	 */
	public void testLineBreakpointRemovalAfterMarkerIsDetached() throws Exception {
		String typeName = "HitCountLooper"; //$NON-NLS-1$
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(17, typeName);
		breakpoint.addBreakpointListener(TEST_LISTENER);
		EvalualtionBreakpointListener.reset();

		IJavaThread thread = null;
		IMarker marker = null;
		try {
			thread = launchToLineBreakpoint(typeName, breakpoint);
			assertNotNull("Breakpoint was not hit", thread); //$NON-NLS-1$
			JDIDebugTarget target = (JDIDebugTarget) thread.getDebugTarget();
			assertTrue("Breakpoint should be tracked by the debug target", containsByIdentity(target.getBreakpoints(), breakpoint)); //$NON-NLS-1$
			assertTrue("Breakpoint should be current in the suspended thread", containsByIdentity(thread.getBreakpoints(), breakpoint)); //$NON-NLS-1$

			marker = detachMarker(breakpoint);
			target.breakpointRemoved(breakpoint, null);

			assertTrue("Breakpoint removal listener was not notified", EvalualtionBreakpointListener.REMOVED); //$NON-NLS-1$
			assertFalse("Breakpoint without marker remained in the debug target", containsByIdentity(target.getBreakpoints(), breakpoint)); //$NON-NLS-1$
			assertFalse("Breakpoint without marker remained current in the suspended thread", containsByIdentity(thread.getBreakpoints(), breakpoint)); //$NON-NLS-1$
		} finally {
			restoreMarker(breakpoint, marker);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the class-prepare-specific request cleanup when the breakpoint marker
	 * association has already been cleared.
	 */
	public void testClassPrepareBreakpointRemovalAfterMarkerIsDetached() throws Exception {
		String typeName = "HitCountLooper"; //$NON-NLS-1$
		IJavaClassPrepareBreakpoint breakpoint = createClassPrepareBreakpoint("DropTests"); //$NON-NLS-1$
		breakpoint.addBreakpointListener(TEST_LISTENER);
		IJavaLineBreakpoint launchBreakpoint = createLineBreakpoint(17, typeName);
		EvalualtionBreakpointListener.reset();

		IJavaThread thread = null;
		IMarker marker = null;
		try {
			thread = launchToLineBreakpoint(typeName, launchBreakpoint);
			assertNotNull("Launch breakpoint was not hit", thread); //$NON-NLS-1$
			JDIDebugTarget target = (JDIDebugTarget) thread.getDebugTarget();
			assertTrue("Class prepare breakpoint should be tracked by the debug target", containsByIdentity(target.getBreakpoints(), breakpoint)); //$NON-NLS-1$

			marker = detachMarker(breakpoint);
			target.breakpointRemoved(breakpoint, null);

			assertTrue("Breakpoint removal listener was not notified", EvalualtionBreakpointListener.REMOVED); //$NON-NLS-1$
			assertFalse("Class prepare breakpoint without marker remained in the debug target", containsByIdentity(target.getBreakpoints(), breakpoint)); //$NON-NLS-1$
		} finally {
			restoreMarker(breakpoint, marker);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	private IMarker detachMarker(IJavaBreakpoint breakpoint) {
		IMarker marker = breakpoint.getMarker();
		try {
			breakpoint.setMarker(null);
		} catch (CoreException e) {
			// Configuring the breakpoint cannot complete after the marker was detached.
		}
		assertNull("Breakpoint marker should be detached", breakpoint.getMarker()); //$NON-NLS-1$
		return marker;
	}

	private static void restoreMarker(IJavaBreakpoint breakpoint, IMarker marker) throws CoreException {
		if (breakpoint.getMarker() == null && marker != null) {
			breakpoint.setMarker(marker);
		}
	}

	private static void waitForRemovalNotification() throws InterruptedException {
		long timeout = System.currentTimeMillis() + DEFAULT_TIMEOUT;
		synchronized (EvalualtionBreakpointListener.REMOVE_LOCK) {
			while (!EvalualtionBreakpointListener.REMOVED) {
				long remaining = timeout - System.currentTimeMillis();
				if (remaining <= 0) {
					return;
				}
				EvalualtionBreakpointListener.REMOVE_LOCK.wait(remaining);
			}
		}
	}

	private static void waitForCleanup(JDIDebugTarget target, IJavaThread thread, IBreakpoint breakpoint) throws InterruptedException {
		long timeout = System.currentTimeMillis() + DEFAULT_TIMEOUT;
		while (containsByIdentity(target.getBreakpoints(), breakpoint) || containsByIdentity(thread.getBreakpoints(), breakpoint)) {
			if (System.currentTimeMillis() >= timeout) {
				return;
			}
			Thread.sleep(10);
		}
	}

	private static boolean containsByIdentity(IBreakpoint[] breakpoints, IBreakpoint expected) {
		for (IBreakpoint breakpoint : breakpoints) {
			if (breakpoint == expected) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsByIdentity(List<IBreakpoint> breakpoints, IBreakpoint expected) {
		synchronized (breakpoints) {
			for (IBreakpoint breakpoint : breakpoints) {
				if (breakpoint == expected) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean containsMarkerError(IStatus status) {
		if (containsMarkerError(status.getMessage())) {
			return true;
		}
		for (Throwable exception = status.getException(); exception != null; exception = exception.getCause()) {
			if (containsMarkerError(exception.getMessage())) {
				return true;
			}
		}
		for (IStatus child : status.getChildren()) {
			if (containsMarkerError(child)) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsMarkerError(String message) {
		return message != null && (message.contains(NO_ASSOCIATED_MARKER) || message.contains(DELETED_MARKER));
	}
}
