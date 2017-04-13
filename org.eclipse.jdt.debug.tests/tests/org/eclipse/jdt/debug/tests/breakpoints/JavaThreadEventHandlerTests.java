/*******************************************************************************
 *  Copyright (c) 2017 Andrey Loskutov and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
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

}
