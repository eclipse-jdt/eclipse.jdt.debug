/*******************************************************************************
 *  Copyright (c) 2026 IBM Corporation.
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
package org.eclipse.jdt.debug.test.stepping;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.DebugElementKindEventWaiter;
import org.eclipse.jdt.debug.testplugin.DebugEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

public class StatementSteppingTests extends AbstractDebugTest {

	private boolean originalPreferenceValue;

	public StatementSteppingTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		originalPreferenceValue = getPrefStore().getBoolean(IJDIPreferencesConstants.PREF_STATEMENT_LEVEL_STEPPING);
		if (!originalPreferenceValue) {
			getPrefStore().setValue(IJDIPreferencesConstants.PREF_STATEMENT_LEVEL_STEPPING, true);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		if (originalPreferenceValue != getPrefStore().getBoolean(IJDIPreferencesConstants.PREF_STATEMENT_LEVEL_STEPPING)) {
			getPrefStore().setValue(IJDIPreferencesConstants.PREF_STATEMENT_LEVEL_STEPPING, originalPreferenceValue);
		}
		super.tearDown();
	}

	/**
	 * Tests a step over with lots of arguments
	 */
	public void testLotsOfMultilineArguments() throws Exception {

		String typeName = "StatementStep";
		ILineBreakpoint bp = createLineBreakpoint(21, typeName);
		IJavaDebugTarget javaDebugTarget = null;
		IJavaThread t = null;
		boolean originalStepFilter = false;
		try {
			IJavaThread thread = launchToLineBreakpoint(typeName, bp, true);
			t = thread;
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			javaDebugTarget = (IJavaDebugTarget) stackFrame.getDebugTarget();
			originalStepFilter = javaDebugTarget.isStepFiltersEnabled();

			javaDebugTarget.setStepFiltersEnabled(true);

			runAndWaitForSuspendEvent(() -> thread.stepOver());
			assertEquals("Suspended at wrong line", 26, stackFrame.getLineNumber());

			runAndWaitForSuspendEvent(() -> thread.stepInto());

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Suspended at wrong line", 36, stackFrame.getLineNumber());

			runAndWaitForSuspendEvent(() -> thread.stepReturn());

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Suspended at wrong line", 22, stackFrame.getLineNumber());

			thread.stepOver();
			runAndWaitForSuspendEvent(() -> thread.stepOver());

			assertEquals("Suspended at wrong line", 28, stackFrame.getLineNumber());
		} finally {
			terminateAndRemove(t);
			removeAllBreakpoints();
			javaDebugTarget.setStepFiltersEnabled(originalStepFilter);
		}
	}

	public void testStepOverWithSingleMultilineArgument() throws Exception {

		String typeName = "StatementStepArgument";
		ILineBreakpoint bp = createLineBreakpoint(28, typeName);
		IJavaDebugTarget javaDebugTarget = null;
		IJavaThread t = null;
		boolean originalStepFilter = false;
		try {
			IJavaThread thread = launchToLineBreakpoint(typeName, bp, true);
			t = thread;
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			javaDebugTarget = (IJavaDebugTarget) stackFrame.getDebugTarget();
			originalStepFilter = javaDebugTarget.isStepFiltersEnabled();

			javaDebugTarget.setStepFiltersEnabled(true);
			runAndWaitForSuspendEvent(() -> thread.stepOver());
			assertEquals("Suspended at wrong line", 30, stackFrame.getLineNumber());

			runAndWaitForSuspendEvent(() -> thread.stepOver());
			assertEquals("Suspended at wrong line", 34, stackFrame.getLineNumber());

		} finally {
			terminateAndRemove(t);
			removeAllBreakpoints();
			javaDebugTarget.setStepFiltersEnabled(originalStepFilter);
		}
	}

	public void testStepOverWithNestedMultiLineArgs() throws Exception {

		String typeName = "StatementStepNested";
		ILineBreakpoint bp = createLineBreakpoint(20, typeName);
		IJavaDebugTarget javaDebugTarget = null;
		IJavaThread t = null;
		boolean originalStepFilter = false;
		try {
			IJavaThread thread = launchToLineBreakpoint(typeName, bp, true);
			t = thread;
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			javaDebugTarget = (IJavaDebugTarget) stackFrame.getDebugTarget();
			originalStepFilter = javaDebugTarget.isStepFiltersEnabled();

			javaDebugTarget.setStepFiltersEnabled(true);
			runAndWaitForSuspendEvent(() -> thread.stepOver());
			assertEquals("Suspended at wrong line", 27, stackFrame.getLineNumber());

			runAndWaitForSuspendEvent(() -> thread.stepInto());
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Suspended at wrong line", 38, stackFrame.getLineNumber());

			runAndWaitForSuspendEvent(() -> thread.stepReturn());
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Suspended at wrong line", 26, stackFrame.getLineNumber());

			runAndWaitForSuspendEvent(() -> thread.stepOver());
			assertEquals("Suspended at wrong line", 24, stackFrame.getLineNumber());

			runAndWaitForSuspendEvent(() -> thread.stepOver());
			assertEquals("Suspended at wrong line", 22, stackFrame.getLineNumber());

			runAndWaitForSuspendEvent(() -> thread.stepOver());
			assertEquals("Suspended at wrong line", 34, stackFrame.getLineNumber());

		} finally {
			terminateAndRemove(t);
			removeAllBreakpoints();
			javaDebugTarget.setStepFiltersEnabled(originalStepFilter);
		}
	}

	/**
	 * Returns the <code>JDIDebugUIPlugin</code> preference store
	 */
	protected IPreferenceStore getPrefStore() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore();
	}

	private void runAndWaitForSuspendEvent(ISafeRunnable runnable) throws Exception {
		DebugEventWaiter waiter = new DebugElementKindEventWaiter(DebugEvent.SUSPEND, IJavaThread.class);
		runnable.run();
		waiter.waitForEvent();
		TestUtil.waitForJobs(getName(), 100, DEFAULT_TIMEOUT);
	}

	@Override
	protected boolean enableUIEventLoopProcessingInWaiter() {
		return true;
	}
}
