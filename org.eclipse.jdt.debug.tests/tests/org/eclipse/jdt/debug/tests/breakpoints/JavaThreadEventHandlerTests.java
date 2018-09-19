/*******************************************************************************
 *  Copyright (c) 2017 Andrey Loskutov and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestAgainException;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTEvaluationEngine;
import org.eclipse.jdt.internal.debug.ui.threadgroups.JavaDebugTargetProxy;
import org.eclipse.jdt.internal.debug.ui.threadgroups.JavaThreadEventHandler;

/**
 * Tests for JavaThreadEventHandler
 */
public class JavaThreadEventHandlerTests extends AbstractDebugTest {


	public JavaThreadEventHandlerTests(String name) {
		super(name);
	}

	/**
	 * Tests that we can compute frame index for arbitrary frames, see bug 515696
	 */
	public void testComputeFrameIndexOnSecondFrameAndMonitorsOn() throws Exception {
		final String typeName = "DropTests";
		final int expectedFramesCount = 5;
		IJavaBreakpoint bp = createMethodBreakpoint(typeName, "method" + (expectedFramesCount - 1), "()V", true, false);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertTrue("breakpoint was not a method breakpoint", hit instanceof IJavaMethodBreakpoint);

			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull("There should be a stackframe", frame);

			IDebugTarget debugTarget = thread.getDebugTarget();
			JavaDebugTargetProxy proxy = new JavaDebugTargetProxy(debugTarget);
			MyJavaThreadEventHandler eventHandler = new MyJavaThreadEventHandler(proxy);

			eventHandler.displayMonitors = true;

			IStackFrame[] frames = frame.getThread().getStackFrames();
			assertEquals(expectedFramesCount, frames.length);

			// They are all off by one, because we have one monitor installed
			int monitorCount = 1;
			for (int i = 0; i < frames.length; i++) {
				int index = eventHandler.indexOf(frames[i]);
				assertEquals(i + monitorCount, index);
			}
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that we can compute frame index for arbitrary frames, see bug 515696
	 */
	public void testComputeFrameIndexOnSecondFrameAndMonitorsOff() throws Exception {
		final String typeName = "DropTests";
		final int expectedFramesCount = 5;
		IJavaBreakpoint bp = createMethodBreakpoint(typeName, "method" + (expectedFramesCount - 1), "()V", true, false);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertTrue("breakpoint was not a method breakpoint", hit instanceof IJavaMethodBreakpoint);

			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull("There should be a stackframe", frame);

			IDebugTarget debugTarget = thread.getDebugTarget();
			JavaDebugTargetProxy proxy = new JavaDebugTargetProxy(debugTarget);
			MyJavaThreadEventHandler eventHandler = new MyJavaThreadEventHandler(proxy);

			eventHandler.displayMonitors = false;

			IStackFrame[] frames = frame.getThread().getStackFrames();
			assertEquals(expectedFramesCount, frames.length);

			for (int i = 0; i < frames.length; i++) {
				int index = eventHandler.indexOf(frames[i]);
				assertEquals(i, index);
			}
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that we can (or can't) compute frame index during evaluation
	 *
	 * @throws Exception
	 */
	public void testComputeFrameIndexDuringEvaluation() throws Exception {
		String typeName = "DropTests";
		final int expectedFramesCount = 5;
		IJavaBreakpoint bp = createMethodBreakpoint(typeName, "method" + (expectedFramesCount - 1), "()V", true, false);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertTrue("breakpoint was not a method breakpoint", hit instanceof IJavaMethodBreakpoint);

			final int sleepTimeMillis = 750;
			String snippet = "java.lang.Thread.sleep(" + sleepTimeMillis + "); return true;";
			TaskOnFrame task = new TaskOnFrame() {
				@Override
				public void performChecks(IJavaThread thread, IStackFrame[] frames, MyJavaThreadEventHandler eventHandler) throws Exception {
					assertEquals(expectedFramesCount, frames.length);

					IStackFrame[] currFrames = frames[0].getThread().getStackFrames();
					// thread not suspended, so no stack frames as per method contract
					assertEquals(0, currFrames.length);
					assertTrue(thread.isPerformingEvaluation());

					// indexOf method waits for evaluation and computes the right result
					for (int i = 0; i < expectedFramesCount; i++) {
						int index = eventHandler.indexOf(frames[i]);
						if (index == -1) {
							throw new TestAgainException("Evaluation took too long");
						}
						assertEquals(i, index);
					}
					Thread.sleep(sleepTimeMillis);
					// evaluation should be done by now
					assertFalse(thread.isPerformingEvaluation());
				}
			};
			IValue value = doEvalAndRunInParallel(thread, snippet, task);
			assertTrue("The result of '" + snippet + "') should be true", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	private IValue doEvalAndRunInParallel(IJavaThread thread, String snippet, TaskOnFrame task) throws Exception {
		class Listener implements IEvaluationListener {
			IEvaluationResult fResult;

			@Override
			public void evaluationComplete(IEvaluationResult result) {
				fResult = result;
			}

			public IEvaluationResult getResult() {
				return fResult;
			}
		}
		Listener listener = new Listener();
		IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
		assertNotNull("There should be a stackframe", frame);
		IStackFrame[] frames = thread.getStackFrames();

		IDebugTarget debugTarget = thread.getDebugTarget();
		JavaDebugTargetProxy proxy = new JavaDebugTargetProxy(debugTarget);
		MyJavaThreadEventHandler eventHandler = new MyJavaThreadEventHandler(proxy);

		ASTEvaluationEngine engine = new ASTEvaluationEngine(getProjectContext(), (IJavaDebugTarget) debugTarget);
		try {
			assertTrue(thread.isSuspended());
			engine.evaluate(snippet, frame, listener, DebugEvent.EVALUATION_IMPLICIT, false);
			long timeout = System.currentTimeMillis() + 5000;
			while (thread.isSuspended() && System.currentTimeMillis() < timeout) {
				System.out.println("Waiting for evaluation to start..");
				Thread.sleep(10);
			}

			// evaluation must be running now
			assertTrue(thread.isPerformingEvaluation());
			assertFalse(thread.isSuspended());
			assertNull(listener.getResult());

			// Actual test
			task.performChecks(thread, frames, eventHandler);

			// Checck post-conditions
			IEvaluationResult result = listener.getResult();
			assertNotNull("The evaluation should have result: ", result);
			assertNull("The evaluation should not have exception : " + result.getException(), result.getException());

			String firstError = result.hasErrors() ? result.getErrorMessages()[0] : "";
			assertFalse("The evaluation should not have errors : " + firstError, result.hasErrors());
			return listener.getResult().getValue();
		}
		finally {
			engine.dispose();
			eventHandler.dispose();
		}
	}

	static class MyJavaThreadEventHandler extends JavaThreadEventHandler {
		boolean displayMonitors;

		public MyJavaThreadEventHandler(AbstractModelProxy proxy) {
			super(proxy);
		}

		@Override
		public int indexOf(IStackFrame frame) {
			return super.indexOf(frame);
		}

		@Override
		protected boolean isDisplayMonitors() {
			return displayMonitors;
		}
	}

	interface TaskOnFrame {
		void performChecks(IJavaThread thread, IStackFrame[] frames, MyJavaThreadEventHandler eventHandler) throws Exception;
	}

}
