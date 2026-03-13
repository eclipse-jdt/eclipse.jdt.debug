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

import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

public class StatementSteppingTests extends AbstractDebugTest {

	private boolean fOriginalState;

	public StatementSteppingTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fOriginalState = getPrefStore().getBoolean(IJDIPreferencesConstants.PREF_STATEMENT_LEVEL_STEPPING);
		if (!fOriginalState) {
			getPrefStore().setValue(IJDIPreferencesConstants.PREF_STATEMENT_LEVEL_STEPPING, true);
		}
	}

	/**
	 * Tests a step over with lots of arguments
	 */
	public void testLotsOfMultilineArguments() throws Exception {

		String typeName = "StatementStep";
		ILineBreakpoint bp = createLineBreakpoint(21, typeName);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp, true);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			thread.stepOver();
			Thread.sleep(1000);
			assertEquals("Should be at line 26", 26, stackFrame.getLineNumber());

			thread.stepInto();
			Thread.sleep(1000);
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Should be at line 36", 36, stackFrame.getLineNumber());

			thread.stepReturn();
			Thread.sleep(1000);
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Should be at line 22", 22, stackFrame.getLineNumber());

			thread.stepOver();
			Thread.sleep(1000);
			assertEquals("Should be at line 28", 28, stackFrame.getLineNumber());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testStepOverWithSingleMultilineArgument() throws Exception {

		String typeName = "StatementStepArgument";
		ILineBreakpoint bp = createLineBreakpoint(28, typeName);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp, true);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			thread.stepOver();
			Thread.sleep(1000);
			assertEquals("Should be at line 30", 30, stackFrame.getLineNumber());

			thread.stepOver();
			Thread.sleep(1000);
			assertEquals("Should be at line 34", 34, stackFrame.getLineNumber());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testStepOverWithNestedMultiLineArgs() throws Exception {

		String typeName = "StatementStepNested";
		ILineBreakpoint bp = createLineBreakpoint(20, typeName);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp, true);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			thread.stepOver();
			Thread.sleep(1000);
			assertEquals("Should be at line 27", 27, stackFrame.getLineNumber());

			thread.stepInto();
			Thread.sleep(1000);
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Should be at line 38", 38, stackFrame.getLineNumber());

			thread.stepReturn();
			Thread.sleep(1000);
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Should be at line 26", 26, stackFrame.getLineNumber());

			thread.stepOver();
			Thread.sleep(1000);
			assertEquals("Should be at line 24", 24, stackFrame.getLineNumber());

			thread.stepOver();
			Thread.sleep(1000);
			assertEquals("Should be at line 22", 22, stackFrame.getLineNumber());

			thread.stepOver();
			Thread.sleep(1000);
			assertEquals("Should be at line 34", 34, stackFrame.getLineNumber());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Returns the <code>JDIDebugUIPlugin</code> preference store
	 */
	protected IPreferenceStore getPrefStore() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (fOriginalState != getPrefStore().getBoolean(IJDIPreferencesConstants.PREF_STATEMENT_LEVEL_STEPPING)) {
			getPrefStore().setValue(IJDIPreferencesConstants.PREF_STATEMENT_LEVEL_STEPPING, fOriginalState);
		}
	}
}
