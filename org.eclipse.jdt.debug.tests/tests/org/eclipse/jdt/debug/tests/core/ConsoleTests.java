/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

/**
 * Tests console line tracker.
 */
public class ConsoleTests extends AbstractDebugTest {

	public ConsoleTests(String name) {
		super(name);
	}

	class TestConsole extends MessageConsole {

	    public boolean fInit = false;
	    public boolean fDispose = false;

        public TestConsole(boolean autoLifecycle) {
            super("Life's like that", null, autoLifecycle);
        }

        @Override
		protected void init() {
            super.init();
            fInit = true;
        }

        @Override
		protected void dispose() {
            super.dispose();
            fDispose = true;
        }

	}

	/**
	 * Test that when a process is removed from a launch, the associated
	 * console is closed.
	 *
	 * @throws Exception
	 */
	public void testRemoveProcess() throws Exception {
		String typeName = "Breakpoints";
		IJavaDebugTarget target = null;
		try {
			final IJavaDebugTarget otherTarget = launchAndTerminate(typeName);
			target = otherTarget;
			IProcess process = target.getProcess();
			assertNotNull("Missing VM process", process);
			ILaunch launch = target.getLaunch();
			// make sure the console exists
			DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					IConsole console = DebugUITools.getConsole(otherTarget);
					assertNotNull("Missing console", console);
				}
			});
			launch.removeProcess(process);
			// make sure the console is gone
			DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					IConsole console = DebugUITools.getConsole(otherTarget);
					assertNull("Console should no longer exist", console);
				}
			});
		} finally {
			terminateAndRemove(target);
		}
	}

	public void testAutoLifecycle() {
	    TestConsole console = new TestConsole(true);
	    IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
	    consoleManager.addConsoles(new IConsole[]{console});
	    consoleManager.removeConsoles(new IConsole[]{console});
	    assertTrue("Console was not initialized", console.fInit);
	    assertTrue("Console was not disposed", console.fDispose);
	}

	public void testManualLifecycle() {
	    TestConsole console = new TestConsole(false);
	    IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
	    consoleManager.addConsoles(new IConsole[]{console});
	    consoleManager.removeConsoles(new IConsole[]{console});
	    assertTrue("Console was initialized", !console.fInit);
	    assertTrue("Console was disposed", !console.fDispose);
	    console.dispose();
	}

}
