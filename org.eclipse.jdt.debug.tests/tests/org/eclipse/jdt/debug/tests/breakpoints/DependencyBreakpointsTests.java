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
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.DebugElementKindEventWaiter;
import org.eclipse.jdt.debug.testplugin.DebugEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;

/**
 * Tests breakpoint dependency feature
 */
public class DependencyBreakpointsTests extends AbstractDebugTest {

	public DependencyBreakpointsTests(String name) {
		super(name);
	}


	public void testNormalDependency() throws Exception {
		String typeName = "DependencyTest";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(16, typeName);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(17, typeName);
		IJavaLineBreakpoint bp3 = createLineBreakpoint(18, typeName);

		bp2.setSuspendPolicy(IJavaBreakpoint.RESUME_ON_HIT);
		bp2.setDependencyBreakpoint(true);
		bp3.setDependentBreakpoint(bp2);

		IJavaThread t = null;
		try {
			IJavaThread thread = launchToBreakpoint(typeName);
			t = thread;
			runAndWaitForDebugEvent(() -> thread.resume(), DebugEvent.SUSPEND);
			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			int lineNumber = frame.getLineNumber();
			assertEquals("Suspend Policy should be restored", IJavaBreakpoint.SUSPEND_THREAD, bp2.getSuspendPolicy());
			assertEquals("Wrong line", 18, lineNumber);

		} finally {
			bp1.delete();
			bp2.delete();
			bp3.delete();
			terminateAndRemove(t);
			removeAllBreakpoints();
		}
	}

	public void testDependencyBpDisabled() throws Exception {
		String typeName = "DependencyTest";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(16, typeName);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(17, typeName);
		IJavaLineBreakpoint bp3 = createLineBreakpoint(18, typeName);
		IJavaLineBreakpoint bp4 = createLineBreakpoint(20, typeName);

		bp2.setSuspendPolicy(IJavaBreakpoint.RESUME_ON_HIT);
		bp2.setDependencyBreakpoint(true);
		bp3.setDependentBreakpoint(bp2);
		bp2.setEnabled(false);

		IJavaThread t = null;
		try {
			IJavaThread thread = launchToBreakpoint(typeName);
			t = thread;
			runAndWaitForDebugEvent(() -> thread.resume(), DebugEvent.SUSPEND);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			int lineNumber = frame.getLineNumber();
			assertEquals("Wrong line", 20, lineNumber);
		} finally {
			bp1.delete();
			bp2.delete();
			bp3.delete();
			bp4.delete();
			terminateAndRemove(t);
			removeAllBreakpoints();
		}
	}

	public void testDependencyBpWithDependancyDisabled() throws Exception {
		String typeName = "DependencyTest";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(16, typeName);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(17, typeName);
		IJavaLineBreakpoint bp3 = createLineBreakpoint(18, typeName);

		bp2.setSuspendPolicy(IJavaBreakpoint.RESUME_ON_HIT);
		bp2.setDependencyBreakpoint(true);
		bp3.setDependentBreakpoint(bp2);
		bp2.setEnabled(false);
		bp3.setDependencyEnabled(false);
		IJavaThread t = null;
		try {
			IJavaThread thread = launchToBreakpoint(typeName);
			t = thread;
			runAndWaitForDebugEvent(() -> thread.resume(), DebugEvent.SUSPEND);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			int lineNumber = frame.getLineNumber();
			assertEquals("Wrong line", 18, lineNumber);
		} finally {
			bp1.delete();
			bp2.delete();
			bp3.delete();
			terminateAndRemove(t);
			removeAllBreakpoints();
		}
	}

	public void testDependencyBpWithExternalTypeDependancy() throws Exception {
		String typeName1 = "ExternalClassTst";
		String typeName2 = "DependencyTest";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(17, typeName1);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(17, typeName2);

		bp1.setDependencyBreakpoint(true);
		bp2.setDependentBreakpoint(bp1);
		IJavaThread t1 = null;
		IJavaThread t2 = null;
		try {
			IJavaThread thread1 = launchToBreakpoint(typeName1);
			t1 = thread1;
			IJavaStackFrame frame = (IJavaStackFrame) thread1.getTopStackFrame();
			int lineNumber = frame.getLineNumber();
			assertEquals("Breakpoint should hit", 17, lineNumber);
			assertTrue("Breakpoint should hit", bp1.hasBeenHit());
			runAndWaitForDebugEvent(() -> thread1.resume(), DebugEvent.TERMINATE);
			IJavaThread thread2 = launchToBreakpoint(typeName2);
			t2 = thread2;
			frame = (IJavaStackFrame) thread2.getTopStackFrame();
			lineNumber = frame.getLineNumber();
			assertEquals("Breakpoint should hit", 17, lineNumber);
		} finally {
			bp1.delete();
			bp2.delete();
			terminateAndRemove(t1);
			terminateAndRemove(t2);
			removeAllBreakpoints();
		}
	}

	public void testDependencyBpWithExternalTypeDependancyWithBpDisabled() throws Exception {
		String typeName1 = "ExternalClassTst";
		String typeName2 = "DependencyTest";
		IJavaLineBreakpoint bp0 = createLineBreakpoint(16, typeName1);
		IJavaLineBreakpoint bp1 = createLineBreakpoint(17, typeName1);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(17, typeName2);
		IJavaLineBreakpoint bp3 = createLineBreakpoint(18, typeName2);
		bp1.setDependencyBreakpoint(true);
		bp2.setDependentBreakpoint(bp1);
		bp1.setEnabled(false);
		IJavaThread t1 = null;
		IJavaThread t2 = null;
		try {
			IJavaThread thread1 = launchToBreakpoint(typeName1);
			t1 = thread1;
			IJavaStackFrame frame = (IJavaStackFrame) thread1.getTopStackFrame();
			int lineNumber = frame.getLineNumber();
			assertEquals("Breakpoint should hit", 16, lineNumber);
			runAndWaitForDebugEvent(() -> thread1.resume(), DebugEvent.TERMINATE);
			IJavaThread thread2 = launchToBreakpoint(typeName2);
			t2 = thread2;
			frame = (IJavaStackFrame) thread2.getTopStackFrame();
			lineNumber = frame.getLineNumber();
			assertFalse("Breakpoint shouldn't have hit", bp1.hasBeenHit());
			assertEquals("Breakpoint should hit", 18, lineNumber);
		} finally {
			bp0.delete();
			bp1.delete();
			bp2.delete();
			bp3.delete();
			terminateAndRemove(t1);
			terminateAndRemove(t2);
			removeAllBreakpoints();
		}
	}

	public void testDependencyBpWithExternalTypeDependancyWithBpDependancyDisabled() throws Exception {
		String typeName1 = "ExternalClassTst";
		String typeName2 = "DependencyTest";
		IJavaLineBreakpoint bp0 = createLineBreakpoint(16, typeName1);
		IJavaLineBreakpoint bp1 = createLineBreakpoint(17, typeName1);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(17, typeName2);
		IJavaLineBreakpoint bp3 = createLineBreakpoint(18, typeName2);
		bp1.setDependencyBreakpoint(true);
		bp2.setDependentBreakpoint(bp1);
		bp2.setDependencyEnabled(false);
		bp1.setEnabled(false);
		IJavaThread t1 = null;
		IJavaThread t2 = null;
		try {
			IJavaThread thread1 = launchToBreakpoint(typeName1);
			t1 = thread1;
			IJavaStackFrame frame = (IJavaStackFrame) thread1.getTopStackFrame();
			int lineNumber = frame.getLineNumber();
			assertEquals("Breakpoint should hit", 16, lineNumber);
			runAndWaitForDebugEvent(() -> thread1.resume(), DebugEvent.TERMINATE);
			IJavaThread thread2 = launchToBreakpoint(typeName2);
			t2 = thread2;
			frame = (IJavaStackFrame) thread2.getTopStackFrame();
			lineNumber = frame.getLineNumber();
			assertFalse("Breakpoint should have hit as dependancy is disabled", bp1.hasBeenHit());
			assertEquals("Breakpoint should hit", 17, lineNumber);
		} finally {
			bp0.delete();
			bp1.delete();
			bp2.delete();
			bp3.delete();
			terminateAndRemove(t1);
			terminateAndRemove(t2);
			removeAllBreakpoints();
		}
	}

	public void testDependencyBpWithExternalAndChainedDependancies() throws Exception {
		String typeName1 = "ExternalClassTst";
		String typeName2 = "DependencyTest";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(17, typeName1);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(17, typeName2);
		IJavaLineBreakpoint bp3 = createLineBreakpoint(18, typeName2);
		IJavaLineBreakpoint bp4 = createLineBreakpoint(20, typeName2);

		bp1.setDependencyBreakpoint(true);
		bp2.setDependentBreakpoint(bp1);

		bp2.setDependencyBreakpoint(true);
		bp3.setDependentBreakpoint(bp2);

		bp3.setDependencyBreakpoint(true);
		bp3.setSuspendPolicy(IJavaBreakpoint.RESUME_ON_HIT);
		bp4.setDependentBreakpoint(bp3);
		IJavaThread t1 = null;
		IJavaThread t2 = null;
		try {
			IJavaThread thread1 = launchToBreakpoint(typeName1);
			t1 = thread1;
			IJavaStackFrame frame = (IJavaStackFrame) thread1.getTopStackFrame();
			int lineNumber = frame.getLineNumber();
			assertEquals("Breakpoint should hit", 17, lineNumber);
			assertTrue("Breakpoint should have hit as dependancy is hit", bp1.hasBeenHit());
			runAndWaitForDebugEvent(() -> thread1.resume(), DebugEvent.TERMINATE);
			IJavaThread thread2 = launchToBreakpoint(typeName2);
			t2 = thread2;
			frame = (IJavaStackFrame) thread2.getTopStackFrame();
			lineNumber = frame.getLineNumber();
			assertEquals("Breakpoint should hit", 17, lineNumber);

			runAndWaitForDebugEvent(() -> thread2.resume(), DebugEvent.SUSPEND);
			frame = (IJavaStackFrame) thread2.getTopStackFrame();
			lineNumber = frame.getLineNumber();
			assertEquals("Breakpoint should hit", 20, lineNumber);

		} finally {
			bp1.delete();
			bp2.delete();
			bp3.delete();
			bp4.delete();
			terminateAndRemove(t1);
			terminateAndRemove(t2);
			removeAllBreakpoints();
		}
	}

	public void testDependencyWithConditionalBreakpointAsFalse() throws Exception {
		String typeName = "DependencyTest";
		IJavaLineBreakpoint bp1 = createConditionalLineBreakpoint(16, typeName, "1 == 3", true);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(17, typeName);
		IJavaLineBreakpoint bp3 = createLineBreakpoint(19, typeName);

		bp1.setDependencyBreakpoint(true);
		bp2.setDependentBreakpoint(bp1);

		IJavaThread t = null;
		try {
			IJavaThread thread = launchToBreakpoint(typeName);
			t = thread;
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			int lineNumber = frame.getLineNumber();
			assertEquals("Wrong line, conditional breakpoint didnt hit, so its dependent bp should not hit", 19, lineNumber);

		} finally {
			bp1.delete();
			bp2.delete();
			bp3.delete();
			terminateAndRemove(t);
			removeAllBreakpoints();
		}
	}

	public void testDependencyWithConditionalBreakpointAsTrue() throws Exception {
		String typeName = "DependencyTest";
		IJavaLineBreakpoint bp1 = createConditionalLineBreakpoint(16, typeName, "1 == 1", true);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(17, typeName);
		IJavaLineBreakpoint bp3 = createLineBreakpoint(19, typeName);

		bp1.setDependencyBreakpoint(true);
		bp2.setDependentBreakpoint(bp1);

		IJavaThread t = null;
		try {
			IJavaThread thread = launchToBreakpoint(typeName);
			t = thread;
			runAndWaitForDebugEvent(() -> thread.resume(), DebugEvent.SUSPEND);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			int lineNumber = frame.getLineNumber();
			assertEquals("Wrong line, conditional breakpoint hit, so its dependent bp should also hit", 17, lineNumber);
		} finally {
			bp1.delete();
			bp2.delete();
			bp3.delete();
			terminateAndRemove(t);
			removeAllBreakpoints();
		}
	}

	public void testDependencyWithHitcountBreakpoint() throws Exception {
		String typeName = "DependencyTest";
		IJavaLineBreakpoint bp1 = createLineBreakpoint(16, typeName);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(17, typeName);
		IJavaLineBreakpoint bp3 = createLineBreakpoint(19, typeName);

		bp1.setHitCount(3);
		bp1.setDependencyBreakpoint(true);
		bp2.setDependentBreakpoint(bp1);

		IJavaThread t = null;
		try {
			IJavaThread thread = launchToBreakpoint(typeName);
			t = thread;
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			int lineNumber = frame.getLineNumber();
			assertEquals("Wrong line, hitcount enabled didn't breakpoint hit, so its dependent bp should also not hit", 19, lineNumber);
		} finally {
			bp1.delete();
			bp2.delete();
			bp3.delete();
			terminateAndRemove(t);
			removeAllBreakpoints();
		}
	}

	private void runAndWaitForDebugEvent(ISafeRunnable runnable, int debugEvent) throws Exception {
		DebugEventWaiter waiter = new DebugElementKindEventWaiter(debugEvent, IJavaThread.class);
		runnable.run();
		waiter.waitForEvent();
		TestUtil.waitForJobs(getName(), 700, DEFAULT_TIMEOUT);
	}
}
