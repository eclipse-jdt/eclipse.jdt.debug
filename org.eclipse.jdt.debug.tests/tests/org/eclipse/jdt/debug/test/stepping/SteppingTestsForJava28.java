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
 *  This is an implementation of an early-draft specification developed under the Java
 *  Community Process (JCP) and is made available for testing and evaluation purposes
 *  only. The code is not compatible with any specification of the JCP.
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.test.stepping;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.DebugElementKindEventWaiter;
import org.eclipse.jdt.debug.testplugin.DebugEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;

public class SteppingTestsForJava28 extends AbstractDebugTest {


	public SteppingTestsForJava28(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return super.get28Project();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testEarlyReadStepping() throws Exception {
		String typeName = "EarlyRead";
		ILineBreakpoint bp = createLineBreakpoint(66, typeName);
		IJavaThread t = null;
		try {
			IJavaThread thread = launchToLineBreakpoint(typeName, bp, true);
			t = thread;
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Suspended at wrong line", 66, stackFrame.getLineNumber());
			runAndWaitForSuspendEvent(() -> thread.stepInto());
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Suspended at wrong line", 34, stackFrame.getLineNumber());
			runAndWaitForSuspendEvent(() -> thread.stepOver());
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Suspended at wrong line", 35, stackFrame.getLineNumber());

			final IJavaStackFrame frameToDrop = stackFrame;
			runAndWaitForSuspendEvent(() -> frameToDrop.dropToFrame());
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Suspended at wrong line after drop to frame", 34, stackFrame.getLineNumber());

		} finally {
			terminateAndRemove(t);
			removeAllBreakpoints();
		}
	}

	public void testWithInitializerStepping() throws Exception {
		String typeName = "EarlyRead";
		ILineBreakpoint bp = createLineBreakpoint(65, typeName);
		IJavaThread t = null;
		try {
			IJavaThread thread = launchToLineBreakpoint(typeName, bp, true);
			t = thread;
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Suspended at wrong line", 65, stackFrame.getLineNumber());
			runAndWaitForSuspendEvent(() -> thread.stepInto());
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Suspended at wrong line inside constructor", 57, stackFrame.getLineNumber());
			runAndWaitForSuspendEvent(() -> thread.stepOver());
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Suspended at wrong line inside initializer", 54, stackFrame.getLineNumber());
			runAndWaitForSuspendEvent(() -> thread.stepOver());
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Suspended at wrong line in constructor body", 58, stackFrame.getLineNumber());

		} finally {
			terminateAndRemove(t);
			removeAllBreakpoints();
		}
	}

	public void testDerived2Stepping() throws Exception {
		String typeName = "EarlyRead";
		ILineBreakpoint bp = createLineBreakpoint(64, typeName);
		IJavaThread t = null;
		try {
			IJavaThread thread = launchToLineBreakpoint(typeName, bp, true);
			t = thread;
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Suspended at wrong line", 64, stackFrame.getLineNumber());
			runAndWaitForSuspendEvent(() -> thread.stepInto());
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Suspended at wrong line inside constructor of Derived2", 50, stackFrame.getLineNumber());

		} finally {
			terminateAndRemove(t);
			removeAllBreakpoints();
		}
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
