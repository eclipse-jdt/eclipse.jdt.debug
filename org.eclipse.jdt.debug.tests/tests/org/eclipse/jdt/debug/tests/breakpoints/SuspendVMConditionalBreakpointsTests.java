/*******************************************************************************
 *  Copyright (c) 2022 Simeon Andreev and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.DebugElementKindEventWaiter;
import org.eclipse.jdt.debug.testplugin.DebugEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;

/**
 * Test that a SUSPEND_VM conditional breakpoints function as expected. Also tests for bug 575131.
 */
public class SuspendVMConditionalBreakpointsTests extends AbstractDebugTest {

	/**
	 * Constructor
	 *
	 * @param name
	 */
	public SuspendVMConditionalBreakpointsTests(String name) {
		super(name);
	}

	/**
	 * Tests that the VM is suspended when a conditional line breakpoint is hit.
	 */
	public void testSuspendVmLineConditionalBreakpoint() throws Exception {
		String typeName = "SuspendVMConditionalBreakpointsTestSnippet";

		IJavaLineBreakpoint bp1 = createLineBreakpoint(28, typeName);
		bp1.setCondition("if (i == 50) { System.out.println(i); } return false;");
		bp1.setConditionEnabled(true);
		bp1.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(36, typeName);
		bp2.setCondition("if (j == 25) { System.out.println(j); } return false;");
		bp2.setConditionEnabled(true);
		bp2.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);

		IJavaDebugTarget debugTarget = null;
		try {
			TestUtil.waitForJobs("testSuspendVmLineConditionalBreakpoint before launch", 250, 500);
			IJavaProject project = getProjectContext();
			ILaunchConfiguration config = getLaunchConfiguration(project, typeName);

			DebugEventWaiter waiter = new DebugElementKindEventWaiter(DebugEvent.BREAKPOINT, IJavaThread.class);
			waiter.setTimeout(DEFAULT_TIMEOUT);
			waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

			boolean registerLaunch = true;
			IJavaThread suspendedThread = (IJavaThread) launchAndWait(config, waiter, registerLaunch);

			debugTarget = (IJavaDebugTarget) suspendedThread.getDebugTarget();
			TestUtil.waitForJobs("testSuspendVmLineConditionalBreakpoint after suspend", 250, 500);

			debugTarget.resume();
			TestUtil.waitForJobs("testSuspendVmLineConditionalBreakpoint after resume", 250, 500);
		} finally {
			if (debugTarget != null) {
				terminateAndRemove(debugTarget);
			}
			removeAllBreakpoints();
		}
	}
}
