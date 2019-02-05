/*******************************************************************************
 * Copyright (c) 2019 Andrey Loskutov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import java.util.Arrays;
import java.util.List;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;

/**
 * Tests lambda breakpoints.
 */
public class LambdaBreakpointsInJava8Tests extends AbstractDebugTest {

	public LambdaBreakpointsInJava8Tests(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get18Project();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		assertNoErrorMarkersExist();
	}

	@Override
	protected void tearDown() throws Exception {
		terminateAndRemoveJavaLaunches();
		removeAllBreakpoints();
		super.tearDown();
	}

	/**
	 * Test for bug 543385 - we should stop multiple times on same line with many lambdas
	 */
	public void testBug541110_unconditional() throws Exception {
		String typeName = "Bug541110";
		int breakpointLineNumber = 22;

		IJavaLineBreakpoint bp = createLineBreakpoint(breakpointLineNumber, typeName);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp);
			thread.resume();
			// now we should stop again in the lambda
			TestUtil.waitForJobs(getName(), 1000, DEFAULT_TIMEOUT, ProcessConsole.class);
			assertTrue("Thread should be suspended", thread.isSuspended());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Test for bug 543385/541110 - we should stop only once if there is a condition.
	 *
	 * Note: if we implement proper lambda debugging support some time later, this test will probably fail.
	 */
	public void testBug541110_conditional() throws Exception {
		String typeName = "Bug541110";
		String breakpointCondition = "true";
		int breakpointLineNumber = 22;

		IJavaLineBreakpoint bp = createConditionalLineBreakpoint(breakpointLineNumber, typeName, breakpointCondition, true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp);
			thread.resume();
			// now we should NOT stop again in the lambda (a more complex condition would most likely fail)
			TestUtil.waitForJobs(getName(), 1000, DEFAULT_TIMEOUT, ProcessConsole.class);
			assertTrue("Thread should be suspended", thread.isTerminated());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	private void terminateAndRemoveJavaLaunches() {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		List<ILaunch> launches = Arrays.asList(launchManager.getLaunches());
		for (ILaunch launch : launches) {
			IDebugTarget debugTarget = launch.getDebugTarget();
			if (debugTarget instanceof IJavaDebugTarget) {
				terminateAndRemove((IJavaDebugTarget) debugTarget);
			}
		}
	}

}
