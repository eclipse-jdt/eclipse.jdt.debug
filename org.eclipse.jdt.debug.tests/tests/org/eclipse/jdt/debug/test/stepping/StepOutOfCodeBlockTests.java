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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
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
	private static final String TYPE_NAME = "StepOutCodeBlockPgm";

	@Override
	protected IJavaProject getProjectContext() {
		return get23Project();
	}

	private boolean fSavedSkipBreakpoints;

	public StepOutOfCodeBlockTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fSavedSkipBreakpoints = DebugUITools.getPreferenceStore().getBoolean(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE);
		DebugUITools.getPreferenceStore().setValue(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE, true);
	}

	@Override
	protected void tearDown() throws Exception {
		DebugUITools.getPreferenceStore().setValue(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE, fSavedSkipBreakpoints);
		super.tearDown();
	}

	public void testStepOutOfWhileBlock() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(27, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 27, "Breakpoint not hit inside while body");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 31, "Wrong line after step-out of while block");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfIfBlock() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(33, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 33, "Breakpoint not hit inside if body");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 41, "Wrong line after step-out of if block");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfNestedIfBlock() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(35, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 35, "Breakpoint not hit inside nested if body");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 38, "Wrong line after step-out of nested if block");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfForEachBlock() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(48, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 48, "Breakpoint not hit inside for-each body");
			runAndWaitForSuspendEvent(() -> jThread.stepOver());
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 51, "Wrong line after step-out of for-each block");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfDoWhileBlock() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(53, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 53, "Breakpoint not hit inside do-while body");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 56, "Wrong line after step-out of do-while block");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfTryBlock() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(58, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 58, "Breakpoint not hit inside try body");
			runAndWaitForSuspendEvent(() -> jThread.stepOver());
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 67, "Wrong line after step-out of try block");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfCatchBlock() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(61, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 61, "Breakpoint not hit inside catch body");
			runAndWaitForSuspendEvent(() -> jThread.stepOver());
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 67, "Wrong line after step-out of catch block");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfTrailingAnonymousBlock() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(69, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 69, "Breakpoint not hit inside trailing anonymous block");
			runAndWaitForSuspendEvent(() -> jThread.stepOver());
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 72, "Wrong line after step-out of trailing anonymous block");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfSwitchRuleBody() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(75, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 75, "Breakpoint not hit inside switch rule body");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 82, "Wrong line after step-out of switch rule body");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfSwitchExpressionRuleBody() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(85, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 85, "Breakpoint not hit inside switch expression rule body");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 92, "Wrong line after step-out of switch expression rule body");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfIfBlockFollowedByLocalClass() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(95, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 95, "Breakpoint not hit inside if body before local class");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 101, "Wrong line after step-out of if block followed by local class");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfLoopLastInIfThenBranch() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(105, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 105, "Breakpoint not hit inside loop in if-then branch");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 110, "Step-out of loop ending an if-then branch must skip the else branch");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfLoopLastInTryBody() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(113, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 113, "Breakpoint not hit inside loop in try body");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 119, "Step-out of loop ending a try body must skip the catch block");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfLoopLastInSwitchRule() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(123, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 123, "Breakpoint not hit inside loop in switch rule");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 130, "Step-out of loop ending a switch rule must skip the next rule");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfWhileBlockFollowedByLocalClass() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(133, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 133, "Breakpoint not hit inside while body before local class");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 139, "Step-out of while block must skip the following local class");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfSynchronizedBlockFollowedByLocalClass() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(141, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 141, "Breakpoint not hit inside synchronized body before local class");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 147, "Step-out of synchronized block must skip the following local class");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfForBlockFollowedByLocalClass() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(149, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 149, "Breakpoint not hit inside for body before local class");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 154, "Step-out of for block must skip the following local class");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfIfBlockFollowedByBlockWithLocalClass() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(156, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 156, "Breakpoint not hit inside if body before block with local class");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 163, "Step-out must skip a following block that only declares a local class");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfIfBlockFollowedByLabeledBlockWithLocalClass() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(165, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 165, "Breakpoint not hit inside if body before labeled block with local class");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 172, "Step-out must skip a following labeled block that only declares a local class");
		} finally {
			cleanUp(bp, thread);
		}
	}

	public void testStepOutOfIfBlockFollowedByNestedBlocksWithLocalClass() throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(174, TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(TYPE_NAME, bp);
			JDIThread jThread = (JDIThread) thread;
			assertSuspendedAt(jThread, 174, "Breakpoint not hit inside if body before nested blocks with local class");
			runAndWaitForSuspendEvent(this::stepOutOfTheCodeBlock);
			assertSuspendedAt(jThread, 186, "Step-out must skip nested blocks and a direct local class");
		} finally {
			cleanUp(bp, thread);
		}
	}

	private void assertSuspendedAt(JDIThread jThread, int expectedLine, String message) throws Exception {
		assertTrue("Thread did not suspend", jThread.isSuspended());
		IJavaStackFrame frame = (IJavaStackFrame) jThread.getTopStackFrame();
		assertEquals(message, expectedLine, frame.getLineNumber());
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

	private void cleanUp(ILineBreakpoint bp, IJavaThread thread) throws CoreException {
		bp.delete();
		terminateAndRemove(thread);
		removeAllBreakpoints();
	}

	@Override
	protected boolean enableUIEventLoopProcessingInWaiter() {
		return false;
	}
}
