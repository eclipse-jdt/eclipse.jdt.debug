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

	public void testDependencyBpWithDependencyDisabled() throws Exception {
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

	public void testDependencyBpWithExternalTypeDependency() throws Exception {
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

	public void testDependencyBpWithExternalTypeDependencyWithBpDisabled() throws Exception {
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

	public void testDependencyBpWithExternalTypeDependencyWithBpDependencyDisabled() throws Exception {
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
			assertFalse("Breakpoint should have hit as dependency is disabled", bp1.hasBeenHit());
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
			assertTrue("Breakpoint should have hit as dependency is hit", bp1.hasBeenHit());
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

	public void testSharedDependencyNotClearedWhenOneWaitingBpChanges() throws Exception {
		String typeName = "DependencyTest";
		IJavaLineBreakpoint depBp = createLineBreakpoint(16, typeName);
		IJavaLineBreakpoint newDepBp = createLineBreakpoint(17, typeName);
		IJavaLineBreakpoint waitingBp1 = createLineBreakpoint(18, typeName);
		IJavaLineBreakpoint waitingBp2 = createLineBreakpoint(19, typeName);

		depBp.setDependencyBreakpoint(true);
		waitingBp1.setDependentBreakpoint(depBp);
		waitingBp2.setDependentBreakpoint(depBp);

		try {
			assertTrue("depBp must be a dependency breakpoint while both waiting BPs reference it", depBp.isDependencyBreakpoint());
			assertEquals("waitingBp1 must point to depBp", depBp, waitingBp1.getDependentBreakpoint());
			assertEquals("waitingBp2 must point to depBp", depBp, waitingBp2.getDependentBreakpoint());

			newDepBp.setDependencyBreakpoint(true);
			waitingBp1.setDependentBreakpoint(newDepBp);

			assertEquals("waitingBp1 must now point to newDepBp", newDepBp, waitingBp1.getDependentBreakpoint());
			assertTrue("depBp must remain a dependency breakpoint because waitingBp2 still references it", depBp.isDependencyBreakpoint());
			assertEquals("waitingBp2 must still point to depBp", depBp, waitingBp2.getDependentBreakpoint());
		} finally {
			depBp.delete();
			newDepBp.delete();
			waitingBp1.delete();
			waitingBp2.delete();
			removeAllBreakpoints();
		}
	}

	public void testSharedDependencyClearedWhenAllWaitingBpsChange() throws Exception {
		String typeName = "DependencyTest";
		IJavaLineBreakpoint depBp = createLineBreakpoint(16, typeName);
		IJavaLineBreakpoint newDepBp1 = createLineBreakpoint(17, typeName);
		IJavaLineBreakpoint newDepBp2 = createLineBreakpoint(18, typeName);
		IJavaLineBreakpoint waitingBp1 = createLineBreakpoint(19, typeName);
		IJavaLineBreakpoint waitingBp2 = createLineBreakpoint(20, typeName);

		depBp.setDependencyBreakpoint(true);
		waitingBp1.setDependentBreakpoint(depBp);
		waitingBp2.setDependentBreakpoint(depBp);

		try {
			assertTrue("depBp must be a dependency breakpoint while both waiting BPs reference it", depBp.isDependencyBreakpoint());

			newDepBp1.setDependencyBreakpoint(true);
			waitingBp1.setDependentBreakpoint(newDepBp1);
			assertTrue("depBp must still be a dependency breakpoint because waitingBp2 still references it", depBp.isDependencyBreakpoint());

			newDepBp2.setDependencyBreakpoint(true);
			waitingBp2.setDependentBreakpoint(newDepBp2);

			assertFalse("depBp must no longer be a dependency breakpoint when no waiting BP references it", depBp.isDependencyBreakpoint());
			assertEquals("waitingBp1 must point to newDepBp1", newDepBp1, waitingBp1.getDependentBreakpoint());
			assertEquals("waitingBp2 must point to newDepBp2", newDepBp2, waitingBp2.getDependentBreakpoint());
		} finally {
			depBp.delete();
			newDepBp1.delete();
			newDepBp2.delete();
			waitingBp1.delete();
			waitingBp2.delete();
			removeAllBreakpoints();
		}
	}

	public void testAtoBtoCwithAdisabled() throws Exception {
		String typeName = "DependencyTest";
		IJavaLineBreakpoint A = createLineBreakpoint(16, typeName);
		IJavaLineBreakpoint B = createLineBreakpoint(17, typeName);
		IJavaLineBreakpoint C = createLineBreakpoint(18, typeName);
		IJavaLineBreakpoint D = createLineBreakpoint(19, typeName);

		A.setDependencyBreakpoint(true);
		B.setDependencyBreakpoint(true);
		B.setDependentBreakpoint(A);
		C.setDependentBreakpoint(B);

		A.setEnabled(false);
		IJavaThread t = null;
		try {
			IJavaThread thread = launchToBreakpoint(typeName);
			t = thread;
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			int lineNumber = frame.getLineNumber();
			assertEquals("Wrong line, Since A is disabled no chained dependency should work", 19, lineNumber);
		} finally {
			A.delete();
			B.delete();
			C.delete();
			D.delete();
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
