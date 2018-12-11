/*******************************************************************************
 * Copyright (c) 2018 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import java.util.Arrays;
import java.util.List;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.testplugin.DebugEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;

/**
 * Tests conditional breakpoints.
 */
public class LambdaBreakpointsTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public LambdaBreakpointsTests(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get18Project();
	}


	/**
	 * Test for Bug 541110 - ClassCastException in Instruction.popValue and a zombie EventDispatcher$1 job afterwards
	 *
	 * We check that a specific conditional breakpoint on a line with a lambda expression does not cause a {@link ClassCastException}.
	 */
	public void testBug541110() throws Exception {
		assertNoErrorMarkersExist();

		String typeName = "Bug541110";
		createConditionalLineBreakpoint(22, typeName, "map.get(key) != null", true);

		try {
			// The class cast exception causes a job which runs forever. So we will timeout when waiting for debug events, if the exception occurs.
			ILaunchConfiguration config = getLaunchConfiguration(typeName);
			DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.TERMINATE);
			launchAndWait(config, waiter);
			// Join running jobs in case the launch did go through, but we have the endless job.
			TestUtil.waitForJobs(getName(), 1_000, 30_000, ProcessConsole.class);
		} finally {
			terminateAndRemoveJavaLaunches();
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
