/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.internal.console.IOConsolePartitioner;

/**
 * Tests console lifecycle and output handling.
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
		assertFalse("Console was initialized", console.fInit);
		assertFalse("Console was disposed", console.fDispose);
	    console.dispose();
	}

	/**
	 * Test synchronization of standard and error output stream of started process.
	 * <p>
	 * This variant tests output on multiple lines.
	 * </p>
	 *
	 * @throws Exception
	 *             if test failed
	 */
	public void testConsoleOutputSynchronization() throws Exception {
		String typeName = "OutSync";
		ILaunch launch = null;
		try {
			ILaunchConfiguration launchConfig = getLaunchConfiguration(typeName);
			ILaunchConfigurationWorkingCopy launchCopy = launchConfig.getWorkingCopy();
			launchCopy.setAttribute(DebugPlugin.ATTR_MERGE_OUTPUT, true);
			launch = launchCopy.launch(ILaunchManager.RUN_MODE, null);
			TestUtil.waitForJobs(getName(), 0, DEFAULT_TIMEOUT);
			String content = getConsoleContent(launch.getProcesses()[0]);
			// normalize new lines to unix style
			content = content.replace("\r\n", "\n").replace('\r', '\n');
			String expectedOutput = String.join("", Collections.nCopies(content.length() / 4, "o\ne\n"));
			assertEquals("Received wrong output. Probably not synchronized.", expectedOutput, content);
		} finally {
			if (launch != null) {
				getLaunchManager().removeLaunch(launch);
			}
		}
	}

	/**
	 * Test synchronization of standard and error output stream of started process.
	 * <p>
	 * This variant tests output on single line.
	 * </p>
	 *
	 * @throws Exception
	 *             if test failed
	 */
	public void testConsoleOutputSynchronization2() throws Exception {
		String typeName = "OutSync2";
		ILaunch launch = null;
		try {
			ILaunchConfiguration launchConfig = getLaunchConfiguration(typeName);
			ILaunchConfigurationWorkingCopy launchCopy = launchConfig.getWorkingCopy();
			launchCopy.setAttribute(DebugPlugin.ATTR_MERGE_OUTPUT, true);
			launch = launchCopy.launch(ILaunchManager.RUN_MODE, null);
			TestUtil.waitForJobs(getName(), 0, DEFAULT_TIMEOUT);
			String content = getConsoleContent(launch.getProcesses()[0]);
			String expectedOutput = String.join("", Collections.nCopies(content.length() / 2, "oe"));
			assertEquals("Received wrong output. Probably not synchronized.", expectedOutput, content);
		} finally {
			if (launch != null) {
				getLaunchManager().removeLaunch(launch);
			}
		}
	}

	/**
	 * Test if process error output has another color in console than standard output.
	 *
	 * @throws Exception
	 *             if test failed
	 */
	public void testConsoleErrorColoring() throws Exception {
		String typeName = "OutSync";
		IJavaDebugTarget target = null;
		try {
			target = launchAndTerminate(typeName);
			final IProcess process = target.getProcess();
			assertNotNull("Missing VM process.", process);
			final IConsole console = DebugUITools.getConsole(process);
			assertTrue("Console is not a TextConsole.", console instanceof TextConsole);
			final TextConsole textConsole = (TextConsole) console;
			final IDocumentPartitioner partitioner = textConsole.getDocument().getDocumentPartitioner();
			assertTrue("Partitioner is not a IOConsolePartitioner.", partitioner instanceof IOConsolePartitioner);
			final IOConsolePartitioner ioPartitioner = (IOConsolePartitioner) partitioner;
			TestUtil.waitForJobs(getName(), 100, DEFAULT_TIMEOUT); // wait for output appending

			final long numStyleTypes = Arrays.stream(ioPartitioner.getStyleRanges(0, textConsole.getDocument().getLength())).map((s) -> s.foreground).distinct().count();
			assertTrue("Console partitioner did not distinct standard and error output.", numStyleTypes > 1);
		} finally {
			if (target != null) {
				terminateAndRemove(target);
			}
		}
	}

	/**
	 * Try to get the console content associated with given process.
	 *
	 * @param process
	 *            the process on whose output we are interested
	 * @return the raw content in console probably written by given process. Note: result may be affected by user input and console trimming.
	 * @throws Exception
	 *             if content retrieval failed
	 */
	private String getConsoleContent(IProcess process) throws Exception {
		assertNotNull("Missing VM process.", process);
		final IConsole console = DebugUITools.getConsole(process);
		assertNotNull("Missing console", console);
		assertTrue("Console is not a TextConsole.", console instanceof TextConsole);
		final TextConsole textConsole = (TextConsole) console;
		TestUtil.waitForJobs(getName(), 100, DEFAULT_TIMEOUT); // wait for output appending
		assertEquals("Test program failed with error.", 0, process.getExitValue());
		final IDocument consoleDocument = textConsole.getDocument();
		return consoleDocument.get();
	}
	/**
	 * Test console receiving UTF-8 output from process where two-byte UTF-8 characters start at even offsets.
	 *
	 * @throws Exception
	 *             if the test gets in trouble
	 */
	public void testBug545769_UTF8OutEven() throws Exception {
		// 4200 umlaute results in 8400 byte of output which should be more than most common buffer sizes.
		utf8OutputTest(0, 4200, 5);
	}

	/**
	 * Test console receiving UTF-8 output from process where two-byte UTF-8 characters start at odd offsets.
	 *
	 * @throws Exception
	 *             if the test gets in trouble
	 */
	public void testBug545769_UTF8OutOdd() throws Exception {
		// 4200 umlaute results in 8400 byte of output which should be more than most common buffer sizes.
		utf8OutputTest(1, 4200, 5);
	}

	/**
	 * Shared test code for possible UTF-8 process output corruption.
	 *
	 * @param numAscii
	 *            number of one byte UTF-8 characters the process prints first
	 * @param numUmlaut
	 *            number of two byte UTF-8 character the process prints second
	 * @param repetitions
	 *            number of output repetitions. This test requires the process can write its output faster than the console can read it.
	 * @throws Exception
	 *             if the test gets in trouble
	 */
	private void utf8OutputTest(int numAscii, int numUmlaut, int repetitions) throws Exception {
		final String typeName = "ConsoleOutputUmlaut";

		final IPreferenceStore debugPrefStore = DebugUIPlugin.getDefault().getPreferenceStore();
		debugPrefStore.setValue(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT, false);
		debugPrefStore.setValue(IDebugPreferenceConstants.CONSOLE_WRAP, true);
		debugPrefStore.setValue(IDebugPreferenceConstants.CONSOLE_WIDTH, 100);

		final ILaunchConfiguration launchConfig = getLaunchConfiguration(typeName);
		final ILaunchConfigurationWorkingCopy launchCopy = launchConfig.getWorkingCopy();
		String arg = String.join(" ", Integer.toString(numAscii), Integer.toString(numUmlaut), Integer.toString(repetitions));
		launchCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, arg);
		launchCopy.setAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, StandardCharsets.UTF_8.name());

		ILaunch launch = null;
		try {
			launch = launchCopy.launch(ILaunchManager.RUN_MODE, null);
			final IProcess process = launch.getProcesses()[0];
			assertNotNull("Missing VM process", process);
			TestUtil.waitForJobs(getName(), 25, DEFAULT_TIMEOUT); // wait for console creation
			final IConsole console = DebugUITools.getConsole(process);
			assertNotNull("Missing console", console);
			assertTrue("Console is not a TextConsole", console instanceof TextConsole);
			final TextConsole textConsole = (TextConsole) console;
			TestUtil.waitForJobs(getName(), 100, DEFAULT_TIMEOUT); // wait for output appending
			assertEquals("Test program failed with error.", 0, process.getExitValue());
			final IDocument consoleDocument = textConsole.getDocument();
			final int expectedLength = (numAscii + numUmlaut + 2) * repetitions;
			if (consoleDocument.getLength() > expectedLength) {
				// debug code for test failures
				int offset = expectedLength - 20;
				String trail = consoleDocument.get(offset, consoleDocument.getLength() - offset);
				System.out.println(trail);
			}
			assertEquals("Wrong number of characters in console.", expectedLength, consoleDocument.getLength());
		} finally {
			debugPrefStore.setValue(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT, true);
			debugPrefStore.setValue(IDebugPreferenceConstants.CONSOLE_WRAP, false);
			if (launch != null) {
				if (launch.getProcesses() != null) {
					for (IProcess process : launch.getProcesses()) {
						process.terminate();
					}
				}
				getLaunchManager().removeLaunch(launch);
			}
		}
	}
}
