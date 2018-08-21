/*******************************************************************************
 * Copyright (c) 2018 Till Brychcy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Till Brychcy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.test.stepping;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

public class StepResultTests extends AbstractDebugTest {

	public StepResultTests(String name) {
		super(name);
	}

	public void testReturnValueAfterStepReturn() throws Exception {
		String typeName = "StepResult1";
		ILineBreakpoint bp1 = createLineBreakpoint(28, "StepResult1");
		bp1.setEnabled(true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp1, false);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("h", stackFrame.getMethodName());

			thread = stepReturn(stackFrame);
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("g", stackFrame.getMethodName());
			IVariable varInG = stackFrame.getVariables()[0];
			assertEquals("h() returned", varInG.getName());
			assertEquals("\"h-i\"", varInG.getValue().toString());

			// skip the synthetic via a step filter.
			IJavaDebugTarget javaDebugTarget = (IJavaDebugTarget) stackFrame.getDebugTarget();
			boolean oldStepFiltersEnabled = javaDebugTarget.isStepFiltersEnabled();
			boolean oldFilterSynthetics = javaDebugTarget.isFilterSynthetics();
			try {
				javaDebugTarget.setStepFiltersEnabled(true);
				javaDebugTarget.setFilterSynthetics(true);
				thread = stepReturn(stackFrame);
			}
			finally {
				javaDebugTarget.setStepFiltersEnabled(oldStepFiltersEnabled);
				javaDebugTarget.setFilterSynthetics(oldFilterSynthetics);
			}
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("f", stackFrame.getMethodName());
			IVariable varInF = stackFrame.getVariables()[0];
			assertEquals("access$0() returned", varInF.getName());
			assertEquals("\"g-h-i-j\"", varInF.getValue().toString());

			thread = stepReturn(stackFrame);
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("main", stackFrame.getMethodName());
			IVariable varInMain1 = stackFrame.getVariables()[0];
			assertEquals("f() returned", varInMain1.getName());

			// test that running the thread a bit will clear the "f() returned"
			thread = stepOver(stackFrame);
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("main", stackFrame.getMethodName());
			IVariable varInMain2 = stackFrame.getVariables()[0];
			assertEquals("no method return value", varInMain2.getName());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * test that if the step return is aborted by some other break point, no return value of a recursive invocation is shown
	 *
	 * @throws Exception
	 */
	public void testNoReturnValueAfterAbortedStepReturn() throws Exception {
		String typeName = "StepResult1";
		ILineBreakpoint bp2A = createLineBreakpoint(49, "StepResult1");
		bp2A.setEnabled(true);
		ILineBreakpoint bp2B = createLineBreakpoint(50, "StepResult1");
		bp2B.setEnabled(true);
		ILineBreakpoint bp2C = createLineBreakpoint(24, "StepResult1");
		bp2C.setEnabled(true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp2A, false);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("j", stackFrame.getMethodName());

			// trigger stepReturn, but bp2B will be hit.
			stackFrame.stepReturn();

			resumeToLineBreakpoint(thread, bp2B);

			IStackFrame[] stackFrames = thread.getStackFrames();
			stackFrame = (IJavaStackFrame) stackFrames[0];
			assertEquals("j", stackFrame.getMethodName());

			stackFrame = (IJavaStackFrame) stackFrames[1];
			assertEquals("g", stackFrame.getMethodName());

			// bp2C is at the target stack level of the stepReturn
			resumeToLineBreakpoint(thread, bp2C);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("g", stackFrame.getMethodName());

			IVariable varInH = stackFrame.getVariables()[0];

			// specifically no "i() returned" must be present
			assertEquals("no method return value", varInH.getName());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testNoReturnValueAfterStepReturnWithException() throws Exception {
		String typeName = "StepResult1";
		ILineBreakpoint bp3A = createLineBreakpoint(41, "StepResult1");
		bp3A.setEnabled(true);
		ILineBreakpoint bp3B = createLineBreakpoint(33, "StepResult1");
		bp3B.setEnabled(true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp3A, false);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("i", stackFrame.getMethodName());

			stepReturn(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("h", stackFrame.getMethodName());

			IVariable varInH = stackFrame.getVariables()[0];

			assertEquals("i() threw", varInH.getName());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testReturnValueAfterStepReturnInStatic() throws Exception {
		String typeName = "StepResult1";
		ILineBreakpoint bp4 = createLineBreakpoint(56, "StepResult1");
		bp4.setEnabled(true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp4, false);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("s", stackFrame.getMethodName());

			thread = stepReturn(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("main", stackFrame.getMethodName());

			IVariable varInMain = stackFrame.getVariables()[0];

			assertEquals("s() returned", varInMain.getName());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testReturnValueAfterStepReturnViaInterface() throws Exception {
		String typeName = "StepResult1";
		ILineBreakpoint bp5 = createLineBreakpoint(66, "StepResult1");
		bp5.setEnabled(true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp5, false);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("get", stackFrame.getMethodName());

			thread = stepReturn(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("testViaInterface", stackFrame.getMethodName());

			IVariable varInMain = stackFrame.getVariables()[0];

			assertEquals("get() returned", varInMain.getName());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testHasExitedWithExceptionFor5BytesDistance() throws Exception {
		String typeName = "StepResult1";
		ILineBreakpoint bp3 = createLineBreakpoint(41, "StepResult1");
		bp3.setEnabled(true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp3, false);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("i", stackFrame.getMethodName());

			stepReturn(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("h", stackFrame.getMethodName());

			IVariable varInH = stackFrame.getVariables()[0];

			assertEquals("i() threw", varInH.getName());

			// will set the thread to running to get the toString result
			IJavaValue stringValue = ((IJavaObject) varInH.getValue()).sendMessage("toString", "()Ljava/lang/String;", null, thread, false);

			assertEquals("\"java.lang.RuntimeException: i\"", stringValue.toString());

			// because the thread has run in between, check that after refreshing the stack, the thrown exception is still present
			IVariable varInH2 = stackFrame.getVariables()[0];
			assertEquals("i() threw", varInH2.getName());

		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testStepReturn_ExceptionWithFinallyAndEmptyCatch() throws Exception {
		String typeName = "StepResult2";
		ILineBreakpoint bp6 = createLineBreakpoint(38, "StepResult2");
		bp6.setEnabled(true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp6, false);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("i", stackFrame.getMethodName());

			stepReturn(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame(); // in finally clause
			assertEquals("g", stackFrame.getMethodName());

			IVariable varInG = stackFrame.getVariables()[0];

			assertEquals("i() threw", varInG.getName());

			stepReturn(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame(); // after empty catch clause
			assertEquals("f", stackFrame.getMethodName());

			IVariable varInF = stackFrame.getVariables()[0];

			assertEquals("g() threw", varInF.getName());

		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testStepOver_ExceptionWithFinallyAndEmptyCatch() throws Exception {
		String typeName = "StepResult2";
		ILineBreakpoint bp6 = createLineBreakpoint(38, "StepResult2");
		bp6.setEnabled(true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp6, false);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("i", stackFrame.getMethodName());

			stepOver(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame(); // in finally clause
			assertEquals("g", stackFrame.getMethodName());

			IVariable varInG = stackFrame.getVariables()[0];

			assertEquals("i() threw", varInG.getName());

			stepOver(stackFrame);
			stepOver(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame(); // still in finally clause
			assertEquals("g", stackFrame.getMethodName());

			IVariable varInG2 = stackFrame.getVariables()[0];

			assertEquals("length() returned", varInG2.getName());

			stepOver(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame(); // after empty catch clause
			assertEquals("f", stackFrame.getMethodName());

			IVariable varInF = stackFrame.getVariables()[0];

			assertEquals("g() threw", varInF.getName());

		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testStepOver() throws Exception {
		String typeName = "StepResult3";
		ILineBreakpoint bp7 = createLineBreakpoint(22, "StepResult3");
		bp7.setEnabled(true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp7, false);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("f", stackFrame.getMethodName());

			stepOver(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("hashCode() returned", stackFrame.getVariables()[0].getName());

			stepOver(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("getClassLoader() returned", stackFrame.getVariables()[0].getName());

			stepOver(stackFrame);
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("<init>() returned", stackFrame.getVariables()[0].getName());

			stepOver(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("newProxyInstance() returned", stackFrame.getVariables()[0].getName());

			stepOver(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("get() returned", stackFrame.getVariables()[0].getName());
			assertEquals("\"hello from proxy\"", stackFrame.getVariables()[0].getValue().toString());

			stepOver(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("h() returned", stackFrame.getVariables()[0].getName());
			assertEquals("null", String.valueOf(stackFrame.getVariables()[0].getValue()));

			stepOver(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("g() returned", stackFrame.getVariables()[0].getName());
			assertEquals("\"XXX\"", stackFrame.getVariables()[0].getValue().toString());

			stepOver(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("main", stackFrame.getMethodName());
			assertEquals("g() threw", stackFrame.getVariables()[0].getName());

			stepOver(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("println() returned", stackFrame.getVariables()[0].getName());

			stepOver(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("currentTimeMillis() returned", stackFrame.getVariables()[0].getName());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testStepInto() throws Exception {
		String typeName = "StepResult3";
		ILineBreakpoint bp8 = createLineBreakpoint(31, "StepResult3");
		bp8.setEnabled(true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp8, false);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("f", stackFrame.getMethodName());

			stepInto(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("g", stackFrame.getMethodName());
			assertFalse(stackFrame.getVariables()[0].getName().contains("returned"));

			stepInto(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("g", stackFrame.getMethodName());
			assertFalse(stackFrame.getVariables()[0].getName().contains("returned"));

			stepInto(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("f", stackFrame.getMethodName());
			assertEquals("g() returned", stackFrame.getVariables()[0].getName());
			assertEquals("\"XXX\"", stackFrame.getVariables()[0].getValue().toString());

			stepInto(stackFrame);
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("f", stackFrame.getMethodName());
			assertFalse(stackFrame.getVariables()[0].getName().contains("returned"));

			stepInto(stackFrame);
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("g", stackFrame.getMethodName());
			assertFalse(stackFrame.getVariables()[0].getName().contains("returned"));

			stepInto(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("g", stackFrame.getMethodName());
			assertFalse(stackFrame.getVariables()[0].getName().contains("returned"));

			stepInto(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("<init>", stackFrame.getMethodName()); // constructor of Exception
			assertFalse(stackFrame.getVariables()[0].getName().contains("returned"));

			stepReturn(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("g", stackFrame.getMethodName());
			assertEquals("<init>() returned", stackFrame.getVariables()[0].getName());

			stepInto(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("main", stackFrame.getMethodName());
			assertEquals("g() threw", stackFrame.getVariables()[0].getName());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testStepUncaught() throws Exception {
		IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		boolean origPrefValue = preferenceStore.getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS);
		preferenceStore.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, true);
		String typeName = "StepUncaught";
		ILineBreakpoint bp = createLineBreakpoint(15, "StepUncaught");
		bp.setEnabled(true);

		IJavaThread thread = null;
		try {
			thread = launchAndSuspend(typeName);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("main", stackFrame.getMethodName());

			stepOver(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("main", stackFrame.getMethodName());
			assertEquals("f() threw", stackFrame.getVariables()[0].getName());

			stepOverToBreakpoint(stackFrame);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("g", stackFrame.getMethodName());
		}
		finally {
			preferenceStore.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, origPrefValue);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testMethodResultOnMethodExitAndExceptionBreakpoints() throws Exception {
		IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		boolean origPrefValue = preferenceStore.getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS);
		preferenceStore.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, true);
		String typeName = "MethodExitAndException";
		IJavaMethodBreakpoint methodExitBreakpoint = createMethodBreakpoint("MethodExitAndException", "f", null, false, true);
		IJavaExceptionBreakpoint exceptionBreakpoint = createExceptionBreakpoint("MyException", true, true);
		methodExitBreakpoint.setEnabled(true);
		exceptionBreakpoint.setEnabled(true);
		IJavaThread thread = null;
		try {
			thread = launchAndSuspend(typeName);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("f", stackFrame.getMethodName());
			assertEquals("f() is returning", stackFrame.getVariables()[0].getName());

			resume(thread);

			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("g", stackFrame.getMethodName());
			assertEquals("g() is throwing", stackFrame.getVariables()[0].getName());
		}
		finally {
			preferenceStore.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, origPrefValue);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testNoReturnValueAfterStepReturnWhichExceedsTimeout() throws Exception {
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(JDIDebugPlugin.getUniqueIdentifier());
		int origPrefValue = node.getInt(JDIDebugModel.PREF_SHOW_STEP_TIMEOUT, JDIDebugModel.DEF_SHOW_STEP_TIMEOUT);
		node.putInt(JDIDebugModel.PREF_SHOW_STEP_TIMEOUT, -1);

		String typeName = "StepResult1";
		ILineBreakpoint bp1 = createLineBreakpoint(28, "StepResult1");
		bp1.setEnabled(true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp1, false);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("h", stackFrame.getMethodName());

			thread = stepReturn(stackFrame);
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("g", stackFrame.getMethodName());
			IVariable varInG = stackFrame.getVariables()[0];
			assertEquals("no method return value", varInG.getName());
			assertEquals("(Not observed to speed up the long running step operation)", varInG.getValue().toString());
		} finally {
			node.putInt(JDIDebugModel.PREF_SHOW_STEP_TIMEOUT, origPrefValue);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
