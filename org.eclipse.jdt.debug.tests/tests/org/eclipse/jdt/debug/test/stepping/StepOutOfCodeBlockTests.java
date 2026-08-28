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
package org.eclipse.jdt.debug.test.stepping;

import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.DebugElementKindEventWaiter;
import org.eclipse.jdt.debug.testplugin.DebugEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

public class StepOutOfCodeBlockTests extends AbstractDebugTest {

	private static final String STEP_OUT_OF_CODE_BLOCK_COMMAND = "org.eclipse.jdt.debug.ui.StepOutOfCodeBlock";

	public StepOutOfCodeBlockTests(String name) {
		super(name);
	}

	public void testStepOutCodeBlock() throws Exception {
		String typeName = "StepOutCodeBlockPgm";
		ILineBreakpoint bp1 = createLineBreakpoint(27, typeName);
		ILineBreakpoint bp2 = createLineBreakpoint(35, typeName);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp1);

			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Breakpoint not hit", 27, frame.getLineNumber());
			JDIThread jThread = (JDIThread) thread;

			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);

			assertTrue("Thread did not suspend", jThread.isSuspended());
			frame = (IJavaStackFrame) jThread.getTopStackFrame();
			assertEquals("Wrong line after step-out-of-block", 31, frame.getLineNumber());
			runAndWaitForSuspendEvent(() -> jThread.resume());

			frame = (IJavaStackFrame) jThread.getTopStackFrame();
			assertEquals("Wrong line after thread resume", 35, frame.getLineNumber());

			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);

			assertTrue("Thread did not suspend", jThread.isSuspended());
			frame = (IJavaStackFrame) jThread.getTopStackFrame();
			assertEquals("Wrong line after step-out-of-block", 38, frame.getLineNumber());

			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);

			assertTrue("Thread did not suspend", jThread.isSuspended());
			frame = (IJavaStackFrame) jThread.getTopStackFrame();
			assertEquals("Wrong line after step-out-of-block", 41, frame.getLineNumber());

			runAndWaitForSuspendEvent(() -> jThread.stepOver());

			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);

			assertTrue("Thread did not suspend", jThread.isSuspended());
			frame = (IJavaStackFrame) jThread.getTopStackFrame();
			assertEquals("Wrong line after step-out-of-block", 46, frame.getLineNumber());

			runAndWaitForSuspendEvent(() -> jThread.stepOver());
			runAndWaitForSuspendEvent(() -> jThread.stepOver());

			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);

			assertTrue("Thread did not suspend", jThread.isSuspended());
			frame = (IJavaStackFrame) jThread.getTopStackFrame();
			assertEquals("Wrong line after step-out-of-block", 51, frame.getLineNumber());

			runAndWaitForSuspendEvent(() -> jThread.stepOver());

			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);

			frame = (IJavaStackFrame) jThread.getTopStackFrame();
			assertEquals("Wrong line after step-out-of-block", 56, frame.getLineNumber());

			runAndWaitForSuspendEvent(() -> jThread.stepOver());

			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);

			frame = (IJavaStackFrame) jThread.getTopStackFrame();
			assertEquals("Wrong line after step-out-of-block", 61, frame.getLineNumber());

			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);

			frame = (IJavaStackFrame) jThread.getTopStackFrame();
			assertEquals("Wrong line after step-out-of-block", 64, frame.getLineNumber());

			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);

			frame = (IJavaStackFrame) jThread.getTopStackFrame();
			assertEquals("Wrong line after step-out-of-block", 67, frame.getLineNumber());

			runAndWaitForSuspendEvent(() -> jThread.stepOver());

			frame = (IJavaStackFrame) jThread.getTopStackFrame();
			assertEquals("Wrong line after step-out-of-block", 69, frame.getLineNumber());

			ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
			Command command = commandService.getCommand(STEP_OUT_OF_CODE_BLOCK_COMMAND);
			assertFalse("Command should be disabled", command.isEnabled());

		} finally {
			bp1.delete();
			bp2.delete();
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	private void runAndWaitForSuspendEvent(ISafeRunnable runnable) throws Exception {
		DebugEventWaiter waiter = new DebugElementKindEventWaiter(DebugEvent.SUSPEND, IJavaThread.class);
		runnable.run();
		Object event = waiter.waitForEvent();
		assertNotNull("Timed out waiting for SUSPEND event after " + runnable, event);
		TestUtil.waitForJobs(getName(), 100, DEFAULT_TIMEOUT);
	}

	private void stepOutOfTheCodeBlock() throws Exception {
		waitUntilCommandEnabled(STEP_OUT_OF_CODE_BLOCK_COMMAND);
		DebugUIPlugin.getStandardDisplay().syncExec(() -> {
			IHandlerService hs = PlatformUI.getWorkbench().getService(IHandlerService.class);
			try {
				hs.executeCommand(STEP_OUT_OF_CODE_BLOCK_COMMAND, null);
			} catch (Exception e) {
				DebugPlugin.log(e);
				fail("StepOutOfCodeBlock command execution failed: " + e);
			}
		});
	}

	private void waitUntilCommandEnabled(String commandId) throws Exception {
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand(commandId);
		long end = System.currentTimeMillis() + DEFAULT_TIMEOUT;
		boolean[] enabled = new boolean[1];
		do {
			TestUtil.waitForJobs(getName(), 50, 200);
			DebugUIPlugin.getStandardDisplay().syncExec(() -> enabled[0] = command.isEnabled());
			if (enabled[0]) {
				return;
			}
			Thread.sleep(50);
		} while (System.currentTimeMillis() < end);
		fail("Command " + commandId + " never became enabled within " + DEFAULT_TIMEOUT + "ms");
	}

	@Override
	protected boolean enableUIEventLoopProcessingInWaiter() {
		return false;
	}
}