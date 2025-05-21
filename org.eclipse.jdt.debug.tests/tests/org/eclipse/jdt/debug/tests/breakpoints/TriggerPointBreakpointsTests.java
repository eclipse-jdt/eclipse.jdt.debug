/*******************************************************************************
 *  Copyright (c) 2016, 2025 IBM Corporation and others.
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

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests trigger point of breakpoints
 */
public class TriggerPointBreakpointsTests extends AbstractDebugTest {

	/**
	 * Constructor
	 */
	public TriggerPointBreakpointsTests(String name) {
		super(name);
	}

	/**
	 * Tests the trigger point
	 */
	public void testTriggerPointBreakpoint() throws Exception {
		String typeName = "TriggerPoint_01";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(28, typeName);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(33, typeName);
		bp2.setTriggerPoint(true);

		IJavaThread thread= null;
		try {
			thread = launchToLineBreakpoint(typeName, bp2);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable var = findVariable(frame, "i");
			assertNotNull("Could not find variable 'i'", var);

			IJavaPrimitiveValue value = (IJavaPrimitiveValue)var.getValue();
			assertNotNull("variable 'i' has no value", value);
			int iValue = value.getIntValue();
			assertEquals("value of 'i' should be '1', but was " + iValue, 1, iValue);

			var = findVariable(frame, "j");
			assertNotNull("Could not find variable 'j'", var);

			value = (IJavaPrimitiveValue) var.getValue();
			assertNotNull("variable 'j' has no value", value);
			int jValue = value.getIntValue();
			assertEquals("value of 'j' should be '1', but was " + jValue, 1, jValue);

			bp1.delete();
			bp2.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testTriggerPointBreakpointWithResume() throws Exception {
		String typeName = "TriggerPoint_01";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(20, typeName);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(21, typeName);
		bp1.setTriggerPoint(true);
		bp1.setSuspendPolicy(IJavaBreakpoint.RESUME_ON_HIT); // Resume mode
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp2);

			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			int lineNumber = frame.getLineNumber();
			assertEquals("Breakpoint should resume and hit next breakpoint", 21, lineNumber);
			bp1.delete();
			bp2.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testTriggerPointBreakpointWithResumeAndCondition() throws Exception {
		String typeName = "TriggerPoint_01";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(20, typeName);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(21, typeName);
		bp1.setTriggerPoint(true);
		bp1.setConditionEnabled(true);
		bp1.setCondition("false"); // this wont let breakpoint to resume
		bp1.setSuspendPolicy(IJavaBreakpoint.RESUME_ON_HIT); // Resume mode
		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			int lineNumber = frame.getLineNumber();
			assertEquals("Breakpoint should not resume as condition is false", 20, lineNumber);
			bp1.delete();
			bp2.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testTriggerPointBreakpointWithResumeAndConditionAsTrue() throws Exception {
		String typeName = "TriggerPoint_01";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(20, typeName);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(21, typeName);
		bp1.setTriggerPoint(true);
		bp1.setConditionEnabled(true);
		bp1.setCondition("true"); // this will let breakpoint to resume
		bp1.setSuspendPolicy(IJavaBreakpoint.RESUME_ON_HIT); // Resume mode
		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			int lineNumber = frame.getLineNumber();
			assertEquals("Breakpoint should not resume as condition is false", 21, lineNumber);
			bp1.delete();
			bp2.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	// see bug : https://github.com/eclipse-jdt/eclipse.jdt.debug/issues/546
	public void testTriggerPointAutoEnableAfterConditionalBp() throws Exception {
		String typeName = "TriggerPoint_01";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(20, typeName);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(21, typeName);
		bp1.setTriggerPoint(true);
		bp2.setCondition("true");
		bp2.setConditionEnabled(true);
		bp2.setConditionSuspendOnTrue(true);
		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			int lineNumber = frame.getLineNumber();
			assertEquals("Breakpoint should hit at triggeroint", 20, lineNumber);
			IJavaDebugTarget debugTarget = (IJavaDebugTarget) thread.getDebugTarget();
			ILaunch launch = debugTarget.getLaunch();
			thread.getLaunch().terminate();
			Thread.sleep(3000);
			IBreakpoint[] bps = getBreakpointManager().getBreakpoints();
			boolean enabledStatus = bps[0].isEnabled();
			assertTrue("Trigger point should be enabled after termination", enabledStatus);
			getLaunchManager().removeLaunch(launch);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
