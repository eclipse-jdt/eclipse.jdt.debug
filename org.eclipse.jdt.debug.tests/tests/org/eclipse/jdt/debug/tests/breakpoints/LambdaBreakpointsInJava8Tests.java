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

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.debug.tests.ui.AbstractDebugUiTests;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;

/**
 * Tests lambda breakpoints.
 */
public class LambdaBreakpointsInJava8Tests extends AbstractDebugUiTests {

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

	public void testBreakpointsInLambdaBlocks() throws Exception {
		final String testClass = "LambdaBreakpoints1";
		IType type = get18Project().findType(testClass);
		assertNotNull("Missing file", type);
		IResource file = type.getResource();
		assertTrue("Missing file", file instanceof IFile);
		JavaEditor editor = (JavaEditor) openEditor((IFile) file);
		processUiEvents();
		IJavaThread javaThread = null;
		IBreakpoint[] bps = getBreakpointManager().getBreakpoints();
		assertArrayEquals(new IBreakpoint[0], bps);

		try {
			BreakpointsMap bpMap = new BreakpointsMap();
			IJavaLineBreakpoint bp1 = createLineBreakpoint(12, editor, bpMap);
			IJavaLineBreakpoint bp2 = createLineBreakpoint(15, editor, bpMap);
			IJavaLineBreakpoint bp3 = createLineBreakpoint(31, editor, bpMap);
			IJavaLineBreakpoint bp4 = createLineBreakpoint(22, editor, bpMap);

			processUiEvents();
			bpMap.assertBreakpointsConsistency();

			// Check the breakpoints aren't moved or removed after saving editor
			// (that will trigger ValidBreakpointLocationLocator)
			sync(() -> editor.doSave(new NullProgressMonitor()));
			TestUtil.waitForJobs(getName(), 100, DEFAULT_TIMEOUT, ProcessConsole.class);
			processUiEvents();

			bpMap.assertBreakpointsConsistency();

			javaThread = launchToBreakpoint(testClass);
			assertNotNull("The program did not suspend", javaThread);
			processUiEvents();

			IBreakpoint hit = getBreakpoint(javaThread);
			assertEquals("should hit breakpoint", bp1, hit);

			hit = resume1(javaThread);
			assertEquals("should hit breakpoint", bp2, hit);

			hit = resume1(javaThread);
			assertEquals("should hit breakpoint", bp3, hit);

			hit = resume1(javaThread);
			assertEquals("should hit breakpoint", bp4, hit);

			IValue value = doEval(javaThread, "result");
			assertEquals("wrong result: ", "0123", value.getValueString());
		} finally {
			sync(() -> editor.getSite().getPage().closeEditor(editor, false));
			terminateAndRemove(javaThread);
			removeAllBreakpoints();
		}
	}

	private IBreakpoint resume1(IJavaThread javaThread) throws Exception {
		super.resume(javaThread);
		TestUtil.waitForJobs(getName(), 50, DEFAULT_TIMEOUT, ProcessConsole.class);
		processUiEvents();
		IBreakpoint hit = getBreakpoint(javaThread);
		return hit;
	}

	@Override
	protected IBreakpoint getBreakpoint(IThread thread) {
		IBreakpoint b = super.getBreakpoint(thread);
		if (b == null) {
			TestUtil.waitForJobs(getName(), 100, DEFAULT_TIMEOUT, ProcessConsole.class);
			b = super.getBreakpoint(thread);
			if (b == null) {
				IJavaThread t = (IJavaThread) thread;
				try {
					IStackFrame[] stackFrames = t.getStackFrames();
					StringBuilder stack = new StringBuilder("Stack for thread " + thread.getName() + "\n");
					for (IStackFrame frame : stackFrames) {
						stack.append(frame.getName()).append("():").append(frame.getLineNumber()).append("\n");
					}
					assertNotNull("suspended, but not by breakpoint! " + stack, b);
				} catch (DebugException e) {
					throwException(e);
				}
			}
		}
		return b;
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
