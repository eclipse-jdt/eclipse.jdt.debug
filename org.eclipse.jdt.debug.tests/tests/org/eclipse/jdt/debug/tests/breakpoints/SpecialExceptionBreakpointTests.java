/*******************************************************************************
 * Copyright (c) 2020 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint.SuspendOnRecurrenceStrategy;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugOptionsManager;
import org.eclipse.jface.preference.IPreferenceStore;

public class SpecialExceptionBreakpointTests extends AbstractDebugTest {

	private boolean fDefaultSuspendOnUncaught;
	private boolean fDefaultSuspendOnCompilationErrors;
	private SuspendOnRecurrenceStrategy fDefaultRecurrenceStrategy;

	public SpecialExceptionBreakpointTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IPreferenceStore uiPrefStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		fDefaultSuspendOnUncaught = uiPrefStore.getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS);
		fDefaultSuspendOnCompilationErrors = uiPrefStore.getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS);
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JDIDebugPlugin.getUniqueIdentifier());
		String val = prefs.get(JDIDebugModel.PREF_SUSPEND_ON_RECURRENCE_STRATEGY, SuspendOnRecurrenceStrategy.RECURRENCE_UNCONFIGURED.name());
		fDefaultRecurrenceStrategy = SuspendOnRecurrenceStrategy.valueOf(val);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		setPreferences(fDefaultSuspendOnUncaught, fDefaultSuspendOnCompilationErrors, fDefaultRecurrenceStrategy);
	}

	void setPreferences(boolean suspUncaught, boolean suspCompErr, SuspendOnRecurrenceStrategy recurr) {
		IPreferenceStore uiPrefStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		uiPrefStore.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, suspUncaught);
		uiPrefStore.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS, suspCompErr);
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JDIDebugPlugin.getUniqueIdentifier());
		prefs.put(JDIDebugModel.PREF_SUSPEND_ON_RECURRENCE_STRATEGY, recurr.name());
	}

	public void testCaughtException_compErr() throws Exception {
		caughtException(true);
	}
	public void testCaughtException() throws Exception {
		caughtException(false);
	}
	void caughtException(boolean suspendOnCompErr) throws Exception {
		setPreferences(true, suspendOnCompErr, SuspendOnRecurrenceStrategy.SUSPEND_ALWAYS);

		String typeName = "ErrorRecurrence";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.Error", true, true);

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("1. Breakpoint not hit within timeout period", thread);
			assertEquals("Line of 1. suspend", 26, thread.getTopStackFrame().getLineNumber());
			assertExceptionBreakpointHit(thread, ex);

			thread = resume(thread);
			assertExceptionBreakpointHit(thread, ex);
			assertNotNull("2. Breakpoint not hit within timeout period", thread);
			assertEquals("Line of 2. suspend", 20, thread.getTopStackFrame().getLineNumber());

			thread = resume(thread);
			assertExceptionBreakpointHit(thread, ex);
			assertNotNull("3. Breakpoint not hit within timeout period", thread);
			assertEquals("Line of 3. suspend", 12, thread.getTopStackFrame().getLineNumber());

			resumeAndExit(thread);
			ex.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testCaughtExceptionSkip_uncaught_compErr() throws Exception {
		caughtExceptionSkip_uncaught(true);
	}
	public void testCaughtExceptionSkip_uncaught() throws Exception {
		caughtExceptionSkip_uncaught(false);
	}
	void caughtExceptionSkip_uncaught(boolean suspendOnCompErr) throws Exception {
		setPreferences(true, suspendOnCompErr, SuspendOnRecurrenceStrategy.SKIP_RECURRENCES);

		String typeName = "ErrorRecurrence";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.Error", true, true);

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("1. Breakpoint not hit within timeout period", thread);
			assertEquals("Line of 1. suspend", 26, thread.getTopStackFrame().getLineNumber());
			assertExceptionBreakpointHit(thread, ex);

			// L20 skipped recurrence

			// L12: uncaught:
			thread = resume(thread);
			assertExceptionBreakpointHit(thread, ex);
			assertNotNull("2. Breakpoint not hit within timeout period", thread);
			assertEquals("Line of 2. suspend", 12, thread.getTopStackFrame().getLineNumber());

			resumeAndExit(thread);
			ex.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testCaughtExceptionSkip_compErr() throws Exception {
		caughtExceptionSkip(true);
	}
	public void testCaughtExceptionSkip() throws Exception {
		caughtExceptionSkip(false);
	}
	void caughtExceptionSkip(boolean suspendOnCompErr) throws Exception {
		setPreferences(false, suspendOnCompErr, SuspendOnRecurrenceStrategy.SKIP_RECURRENCES);

		String typeName = "ErrorRecurrence";
		IJavaExceptionBreakpoint ex = createExceptionBreakpoint("java.lang.Error", true, true);

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("1. Breakpoint not hit within timeout period", thread);
			assertEquals("Line of 1. suspend", 26, thread.getTopStackFrame().getLineNumber());
			assertExceptionBreakpointHit(thread, ex);

			// L20 skipped recurrence

			// L12 suspend on uncaught disabled
			resumeAndExit(thread);
			ex.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	private void assertExceptionBreakpointHit(IJavaThread thread, IJavaExceptionBreakpoint ex) throws DebugException {
		IMarker problem = JavaDebugOptionsManager.getDefault().getProblem((IJavaStackFrame) thread.getTopStackFrame());
		if (problem != null) {
			fail("unexpected problem marker "+problem);
		}
		IBreakpoint hit = getBreakpoint(thread);
		assertNotNull("suspended, but not by breakpoint", hit);
		assertEquals("suspended, but not by expected exception", ex.getExceptionTypeName(), ((IJavaExceptionBreakpoint) hit).getExceptionTypeName());
	}
}
