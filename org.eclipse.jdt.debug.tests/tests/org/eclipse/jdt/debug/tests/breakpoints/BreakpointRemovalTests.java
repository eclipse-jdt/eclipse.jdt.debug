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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.testplugin.EvalualtionBreakpointListener;
import org.eclipse.jdt.debug.testplugin.GlobalBreakpointListener;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

/**
 * Tests marker-independent breakpoint removal and cleanup after a request
 * deregistration failure, including both sides of the installation, target
 * filters, listener notifications and real VM requests.
 */
public class BreakpointRemovalTests extends AbstractDebugTest {

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

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, breakpoint);
			assertNotNull("Breakpoint was not hit", thread); //$NON-NLS-1$
			JDIDebugTarget target = (JDIDebugTarget) thread.getDebugTarget();
			assertTrue("Breakpoint should be tracked by the debug target", containsByIdentity(target.getBreakpoints(), breakpoint)); //$NON-NLS-1$
			assertTrue("Breakpoint should be current in the suspended thread", containsByIdentity(thread.getBreakpoints(), breakpoint)); //$NON-NLS-1$

			try (RemovalLog log = new RemovalLog()) {
				IWorkspaceRunnable runnable = monitor -> {
					breakpoint.getMarker().delete();
					get14Project().getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
				};
				ResourcesPlugin.getWorkspace().run(runnable, null);

				waitForRemovalNotification();
				assertTrue("Breakpoint removal listener was not notified", EvalualtionBreakpointListener.REMOVED); //$NON-NLS-1$
				waitForCleanup(target, thread, breakpoint);
				assertFalse("Deleted breakpoint remained in the debug target", containsByIdentity(target.getBreakpoints(), breakpoint)); //$NON-NLS-1$
				assertFalse("Deleted breakpoint remained current in the suspended thread", containsByIdentity(thread.getBreakpoints(), breakpoint)); //$NON-NLS-1$
				log.assertNoErrors();

				IJavaLineBreakpoint replacement = createLineBreakpoint(loopLine, typeName);
				assertTrue("Replacement breakpoint was not installed in the debug target", containsByIdentity(target.getBreakpoints(), replacement)); //$NON-NLS-1$
				thread = resumeToLineBreakpoint(thread, replacement);
				assertNotNull("Replacement breakpoint was not hit", thread); //$NON-NLS-1$
				assertTrue("Replacement breakpoint should be current in the suspended thread", containsByIdentity(thread.getBreakpoints(), replacement)); //$NON-NLS-1$
				assertFalse("Deleted breakpoint became current again", containsByIdentity(thread.getBreakpoints(), breakpoint)); //$NON-NLS-1$
				log.assertNoErrors();
			}
		} finally {
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
			try (RemovalLog log = new RemovalLog()) {
				target.breakpointRemoved(breakpoint, null);

				assertTrue("Breakpoint removal listener was not notified", EvalualtionBreakpointListener.REMOVED); //$NON-NLS-1$
				assertFalse("Breakpoint without marker remained in the debug target", containsByIdentity(target.getBreakpoints(), breakpoint)); //$NON-NLS-1$
				assertFalse("Breakpoint without marker remained current in the suspended thread", containsByIdentity(thread.getBreakpoints(), breakpoint)); //$NON-NLS-1$
				log.assertNoErrors();
				assertTrue("Global listener did not record removal", GlobalBreakpointListener.REMOVED.contains(breakpoint)); //$NON-NLS-1$
			}
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
			try (RemovalLog log = new RemovalLog()) {
				target.breakpointRemoved(breakpoint, null);

				assertTrue("Breakpoint removal listener was not notified", EvalualtionBreakpointListener.REMOVED); //$NON-NLS-1$
				assertFalse("Class prepare breakpoint without marker remained in the debug target", containsByIdentity(target.getBreakpoints(), breakpoint)); //$NON-NLS-1$
				log.assertNoErrors();
				assertTrue("Global listener did not record removal", GlobalBreakpointListener.REMOVED.contains(breakpoint)); //$NON-NLS-1$
			}
		} finally {
			restoreMarker(breakpoint, marker);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * A marker update failure while deregistering one request must not leave
	 * subsequent requests active in the VM. Both the target and the breakpoint
	 * must release their per-target state and notify removal, so the same
	 * breakpoint can be installed and hit again.
	 */
	public void testCleanupContinuesAfterRequestDeregistrationFailure() throws Exception {
		assertCleanupContinuesAfterRequestDeregistrationFailure(false);
	}

	/** A later notification failure must not hide the original removal failure. */
	public void testRemovalPreservesFailureWhenChangeNotificationFails() throws Exception {
		assertCleanupContinuesAfterRequestDeregistrationFailure(true);
	}

	private void assertCleanupContinuesAfterRequestDeregistrationFailure(boolean failChangeNotification) throws Exception {
		String typeName = "HitCountLooper"; //$NON-NLS-1$
		FailingLineBreakpoint breakpoint = new FailingLineBreakpoint(getType(typeName).getResource(), typeName);
		getBreakpointManager().addBreakpoint(breakpoint);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, breakpoint);
			JDIDebugTarget target = (JDIDebugTarget) thread.getDebugTarget();
			EventRequestManager manager = target.getEventRequestManager();
			// Use the public filter API and real objects from the suspended VM.
			// Two adjacent instance filters catch incomplete list removal as well.
			breakpoint.setThreadFilter(thread);
			breakpoint.addInstanceFilter(thread.getThreadObject());
			breakpoint.addInstanceFilter((IJavaObject) target.newValue("cleanup filter")); //$NON-NLS-1$
			assertSame(thread, breakpoint.getThreadFilter(target));
			assertEquals(2, breakpoint.getInstanceFilters().length);
			assertTrue("Breakpoint should remember its installed target", breakpoint.installedIn(target)); //$NON-NLS-1$
			assertTrue("Initial install notification was not delivered", GlobalBreakpointListener.INSTALLED.contains(breakpoint)); //$NON-NLS-1$
			GlobalBreakpointListener.REMOVED.remove(breakpoint);
			breakpoint.addDuplicateRequest(target);
			List<EventRequest> installedRequests = breakpoint.requestsIn(target);
			assertTrue("Test needs multiple real JDI requests", installedRequests.size() >= 2); //$NON-NLS-1$
			assertTrue("Breakpoint must be current before removal", containsByIdentity(thread.getBreakpoints(), breakpoint)); //$NON-NLS-1$

			breakpoint.failuresRemaining = 2;
			RuntimeException changeFailure = failChangeNotification ? new IllegalStateException("Injected change notification failure") : null; //$NON-NLS-1$
			breakpoint.changeFailure = changeFailure;
			try (RemovalLog log = new RemovalLog()) {
				target.breakpointRemoved(breakpoint, null);
				assertFalse("Target reference survived failed removal", containsByIdentity(target.getBreakpoints(), breakpoint)); //$NON-NLS-1$
				assertFalse("Thread reference survived failed removal", containsByIdentity(thread.getBreakpoints(), breakpoint)); //$NON-NLS-1$
				for (EventRequest request : installedRequests) {
					assertFalse("A breakpoint request survived failed removal", manager.breakpointRequests().contains(request)); //$NON-NLS-1$
					assertFalse("A class-prepare request survived failed removal", manager.classPrepareRequests().contains(request)); //$NON-NLS-1$
				}
				assertTrue("Breakpoint retained removed requests", breakpoint.requestsIn(target).isEmpty()); //$NON-NLS-1$
				assertFalse("Breakpoint retained its installed target", breakpoint.installedIn(target)); //$NON-NLS-1$
				assertNull("Breakpoint retained its thread filter", breakpoint.getThreadFilter(target)); //$NON-NLS-1$
				assertEquals("Breakpoint retained instance filters", 0, breakpoint.getInstanceFilters().length); //$NON-NLS-1$
				assertTrue("Removal notification was not delivered after deregistration failure", GlobalBreakpointListener.REMOVED.contains(breakpoint)); //$NON-NLS-1$
				assertEquals("Cleanup stopped at the first failure", 2, breakpoint.failures.size()); //$NON-NLS-1$
				CoreException firstFailure = breakpoint.failures.get(0);
				assertEquals("Additional failure was lost", failChangeNotification ? 2 : 1, firstFailure.getSuppressed().length); //$NON-NLS-1$
				assertSame(breakpoint.failures.get(1), firstFailure.getSuppressed()[0]);
				if (failChangeNotification) {
					assertSame(changeFailure, firstFailure.getSuppressed()[1]);
				}
				log.assertOnly(firstFailure);

				// Repeated notifications must be harmless and must not repeat cleanup.
				GlobalBreakpointListener.REMOVED.remove(breakpoint);
				target.breakpointRemoved(breakpoint, null);
				assertFalse("Repeated removal notified the listener again", GlobalBreakpointListener.REMOVED.contains(breakpoint)); //$NON-NLS-1$
				log.assertOnly(firstFailure);

				// Reuse the same object: a fresh breakpoint cannot expose stale
				// fInstalledTargets state suppressing the next install notification.
				breakpoint.failuresRemaining = 0;
				breakpoint.changeFailure = null;
				GlobalBreakpointListener.INSTALLED.remove(breakpoint);
				target.breakpointAdded(breakpoint);
				assertTrue("The same breakpoint was not tracked again", containsByIdentity(target.getBreakpoints(), breakpoint)); //$NON-NLS-1$
				assertTrue("The same breakpoint was not marked installed again", breakpoint.installedIn(target)); //$NON-NLS-1$
				assertTrue("Reinstalling the same breakpoint did not notify the listener", GlobalBreakpointListener.INSTALLED.contains(breakpoint)); //$NON-NLS-1$
				thread = resumeToLineBreakpoint(thread, breakpoint);
				assertTrue("The same breakpoint was not hit after reinstallation", containsByIdentity(thread.getBreakpoints(), breakpoint)); //$NON-NLS-1$
				log.assertOnly(firstFailure);
			}
		} finally {
			breakpoint.failuresRemaining = 0;
			breakpoint.changeFailure = null;
			try {
				terminateAndRemove(thread);
			} finally {
				removeAllBreakpoints();
			}
		}
	}

	/** Injects a marker-update failure only after real request deregistration. */
	private static final class FailingLineBreakpoint extends JavaLineBreakpoint {
		int failuresRemaining;
		RuntimeException changeFailure;
		final List<CoreException> failures = new ArrayList<>();

		FailingLineBreakpoint(IResource resource, String typeName) throws DebugException {
			super(resource, typeName, 19, -1, -1, 0, false, new HashMap<>());
		}

		boolean installedIn(JDIDebugTarget target) {
			return isInstalledIn(target);
		}

		@Override
		protected void fireChanged() {
			if (changeFailure != null) {
				throw changeFailure;
			}
			super.fireChanged();
		}

		List<EventRequest> requestsIn(JDIDebugTarget target) {
			return new ArrayList<>(getRequests(target));
		}

		void addDuplicateRequest(JDIDebugTarget target) throws CoreException {
			for (EventRequest request : requestsIn(target)) {
				if (request instanceof BreakpointRequest installed) {
					BreakpointRequest additional = target.getEventRequestManager().createBreakpointRequest(installed.location());
					configureRequest(additional, target);
					registerRequest(additional, target);
					return;
				}
			}
			fail("No real breakpoint request was installed"); //$NON-NLS-1$
		}

		@Override
		protected void deregisterRequest(EventRequest request, JDIDebugTarget target) throws CoreException {
			super.deregisterRequest(request, target);
			if (failuresRemaining > 0) {
				failuresRemaining--;
				CoreException failure = new CoreException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
						"Injected request deregistration failure")); //$NON-NLS-1$
				failures.add(failure);
				throw failure;
			}
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
		long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(DEFAULT_TIMEOUT);
		synchronized (EvalualtionBreakpointListener.REMOVE_LOCK) {
			while (!EvalualtionBreakpointListener.REMOVED) {
				long remaining = deadline - System.nanoTime();
				if (remaining <= 0) {
					return;
				}
				TimeUnit.NANOSECONDS.timedWait(EvalualtionBreakpointListener.REMOVE_LOCK, remaining);
			}
		}
	}

	private static void waitForCleanup(JDIDebugTarget target, IJavaThread thread, IBreakpoint breakpoint) throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(DEFAULT_TIMEOUT);
		while (containsByIdentity(target.getBreakpoints(), breakpoint) || containsByIdentity(thread.getBreakpoints(), breakpoint)) {
			if (System.nanoTime() - deadline >= 0) {
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

	/** Collects all ERROR statuses only while the removal under test runs. */
	private static final class RemovalLog implements AutoCloseable {
		private final List<IStatus> errors = Collections.synchronizedList(new ArrayList<>());
		private final ILogListener listener = (status, _) -> {
			if (status.matches(IStatus.ERROR)) {
				errors.add(status);
			}
		};

		RemovalLog() {
			Platform.addLogListener(listener);
		}

		void assertNoErrors() {
			assertTrue("Unexpected errors during breakpoint removal: " + errors, errors.isEmpty()); //$NON-NLS-1$
		}

		void assertOnly(CoreException expected) {
			assertEquals("Unexpected errors during breakpoint removal: " + errors, 1, errors.size()); //$NON-NLS-1$
			assertSame("The original cleanup failure must be reported", expected, errors.get(0).getException()); //$NON-NLS-1$
		}

		@Override
		public void close() {
			Platform.removeLogListener(listener);
		}
	}
}
