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
public class ConditionalBreakpointsInJava8Tests extends AbstractDebugTest {

	public ConditionalBreakpointsInJava8Tests(String name) {
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
	 * Test for Bug 541110 - ClassCastException in Instruction.popValue and a zombie EventDispatcher$1 job afterwards
	 *
	 * We check that a specific conditional breakpoint on a line with a lambda expression does not cause a {@link ClassCastException}.
	 */
	public void testBug541110() throws Exception {
		String typeName = "Bug541110";
		String breakpointCondition = "map.get(key) != null";
		int breakpointLineNumber = 22;

		// The class cast exception causes a job which runs forever. So we will timeout when waiting for debug events, if the exception occurs.
		assertNoBreakpointHit(typeName, breakpointLineNumber, breakpointCondition);
	}

	/**
	 * Test for Bug 541110 - Cannot display or set conditional breakpoint using local class reference or field
	 *
	 * We check that a conditional breakpoint inside a locally defined anonymous class can access visbile variables.
	 */
	public void testBug404097BreakpointInAnonymousLocalClass() throws Exception {
		String typeName = "Bug404097BreakpointInAnonymousLocalClass";
		int breakpointLineNumber = 25;
		doTestVariableVisibility(typeName, breakpointLineNumber);
	}

	/**
	 * Test for Bug 541110 - Cannot display or set conditional breakpoint using local class reference or field
	 *
	 * We check that a conditional breakpoint inside a lambda can access visible variables.
	 */
	public void testBug404097BreakpointInLambda() throws Exception {
		String typeName = "Bug404097BreakpointInLambda";
		int breakpointLineNumber = 24;
		doTestVariableVisibility(typeName, breakpointLineNumber);
	}

	/**
	 * Test for Bug 541110 - Cannot display or set conditional breakpoint using local class reference or field
	 *
	 * We check that a conditional breakpoint inside a locally defined class can access visible variables.
	 */
	public void testBug404097BreakpointInLocalClass() throws Exception {
		String typeName = "Bug404097BreakpointInLocalClass";
		int breakpointLineNumber = 25;
		doTestVariableVisibility(typeName, breakpointLineNumber);
	}

	/**
	 * Test for Bug 541110 - Cannot display or set conditional breakpoint using local class reference or field
	 *
	 * We check that a conditional breakpoint can access members of a static inner class.
	 */
	public void testBug404097BreakpointUsingInnerClass() throws Exception {
		String typeName = "Bug404097BreakpointUsingInnerClass";
		int breakpointLineNumber = 24;
		doTestClassMemberVisibility(typeName, breakpointLineNumber);
	}

	/**
	 * Test for Bug 541110 - Cannot display or set conditional breakpoint using local class reference or field
	 *
	 * We check that a conditional breakpoint can access members of a class which is defined locally.
	 *
	 * TODO: disabled until a fix is available
	 */
	public void disabled_testBug404097BreakpointUsingLocalClass() throws Exception {
		String typeName = "Bug404097BreakpointUsingLocalClass";
		int breakpointLineNumber = 23;
		doTestClassMemberVisibility(typeName, breakpointLineNumber);
	}

	private void doTestVariableVisibility(String typeName, int breakpointLineNumber) throws Exception {
		/*
		 * We create a condition which does not evaluate to true and expect to not hit the breakpoint.
		 * If condition evaluation runs into a compile error, either the breakpoint becomes unconditional and is therefore always hit.
		 */
		String breakpointCondition = String.join(" || ", Arrays.asList(
				"!\"methodParameter\".equals(methodParameter)",
				"methodVariable == null", "!\"methodVariable\".equals(methodVariable.toString())",
				"!\"lambdaParameter\".equals(lambdaParameter)",
				"!\"lambdaVariable\".equals(lambdaVariable)"));
		assertNoBreakpointHit(typeName, breakpointLineNumber, breakpointCondition);
	}

	private void doTestClassMemberVisibility(String typeName, int breakpointLineNumber) throws Exception {
		/*
		 * We create a condition which does not evaluate to true and expect to not hit the breakpoint. If condition evaluation runs into a compile
		 * error, the breakpoint becomes unconditional and is therefore always hit.
		 */
		String breakpointCondition = "object.i != 0";
		assertNoBreakpointHit(typeName, breakpointLineNumber, breakpointCondition);
	}

	private void assertNoBreakpointHit(String typeName, int breakpointLineNumber, String breakpointCondition) throws Exception {
		boolean suspendOnTrue = true;
		createConditionalLineBreakpoint(breakpointLineNumber, typeName, breakpointCondition, suspendOnTrue);
		ILaunchConfiguration config = getLaunchConfiguration(typeName);
		DebugEventWaiter waiter = new DebugTargetTerminateWaiter();
		launchAndWait(config, waiter);
		TestUtil.waitForJobs(getName(), 1_000, 30_000, ProcessConsole.class);

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

	private static class DebugTargetTerminateWaiter extends DebugEventWaiter {

		public DebugTargetTerminateWaiter() {
			super(DebugEvent.TERMINATE);
		}

		@Override
		public boolean accept(DebugEvent event) {
			if (super.accept(event)) {
				Object source = event.getSource();
				return source instanceof IDebugTarget;
			}
			return false;
		}
	}
}
