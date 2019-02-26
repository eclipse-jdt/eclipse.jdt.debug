/*******************************************************************************
 *  Copyright (c) 2000, 2019 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.performance;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTrackerExtension;
import org.eclipse.jdt.debug.testplugin.ConsoleLineTracker;
import org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IRegion;
import org.eclipse.test.performance.Dimension;

/**
 * Tests performance of the console.
 */
public class PerfConsoleTests extends AbstractDebugPerformanceTest implements IConsoleLineTrackerExtension {

    protected boolean fWarmingUp = false;

    protected boolean fStarted = false;

    protected boolean fStopped = false;

    protected Object fLock = new Object();

    /**
     * Constructor
     * @param name
     */
    public PerfConsoleTests(String name) {
        super(name);
    }


    /**
     * Tests the performance of 1000 lines of plain output to the console
     * @throws Exception
     */
    public void testProcessConsolePlainOutput10000Lines() throws Exception {
        tagAsSummary("Process Console 10,000 lines: plain output", Dimension.ELAPSED_PROCESS);
        runConsole80CharsTest(10000, 75);
    }

    /**
     * Tests the performance of 10000 lines of stack trace output to the console
     * @throws Exception
     */
    public void testProcessConsoleStackTraceOutput10000Lines() throws Exception {
        tagAsSummary("Process Console 10,000 lines: stack trace output", Dimension.ELAPSED_PROCESS);
        runStackTrace(5000, 75); // 2 lines * 5000 repeats = 10000 lines
    }

    /**
     * Tests the performance of 10000 lines of wrapped process console output to the console
     * @throws Exception
     */
    public void testProcessConsoleWrappedOutput10000Lines() throws Exception {
        tagAsSummary("Process Console 10,000 lines: wrapped output", Dimension.ELAPSED_PROCESS);
        runVariableLength(2500, 75); // 4 lines * 2500 repeats = 10000 lines
    }

    /**
     * @see org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        fStarted = false;
        fStopped = false;
        ConsoleLineTracker.setDelegate(this);
    }

    /**
     * @see org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest#tearDown()
     */
    @Override
	protected void tearDown() throws Exception {
        super.tearDown();
        ConsoleLineTracker.setDelegate(null);
    }

    /**
     * Runs the
     * @param lines
     * @param repeatTest
     * @throws Exception
     */
    protected void runConsole80CharsTest(int lines, int repeatTest) throws Exception {
        String typeName = "Console80Chars";
        ILaunchConfiguration configuration = getLaunchConfiguration(typeName);
        ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
        workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, Integer.toString(lines));

        warmupRun(workingCopy);

        for (int i = 0; i < repeatTest; i++) {
            launchWorkingCopyAndWait(workingCopy);
            assertTrue("Never received 'start' notification", fStarted);
            assertTrue("Never received 'stopped' notification", fStopped);
            fStopped = false;
        }
        commitMeasurements();
        assertPerformance();
    }

    /*
     * prints (2*prints)+2 lines
     */
    protected void runStackTrace(int prints, int repeatTest) throws Exception {
        String typeName = "ConsoleStackTrace";
        ILaunchConfiguration configuration = getLaunchConfiguration(typeName);
        ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
        workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, Integer.toString(prints));

        warmupRun(workingCopy);

        for (int i = 0; i < repeatTest; i++) {
            launchWorkingCopyAndWait(workingCopy);
            assertTrue("Never received 'start' notification", fStarted);
            assertTrue("Never received 'stopped' notification", fStopped);
            fStopped = false;

        }
        commitMeasurements();
        assertPerformance();
    }

    /*
     * prints (4*prints)+2 lines
     */
    protected void runVariableLength(int prints, int repeatTest) throws Exception {
        String typeName = "ConsoleVariableLineLength";
        ILaunchConfiguration configuration = getLaunchConfiguration(typeName);
        ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
        workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, Integer.toString(prints));
        IPreferenceStore debugUIPreferences = DebugUIPlugin.getDefault().getPreferenceStore();
        try {
            debugUIPreferences.setValue(IDebugPreferenceConstants.CONSOLE_WRAP, true);
            workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, Integer.toString(prints));

            warmupRun(workingCopy);

            for (int i = 0; i < repeatTest; i++) {
                launchWorkingCopyAndWait(workingCopy);
                assertTrue("Never received 'start' notification", fStarted);
                assertTrue("Never received 'stopped' notification", fStopped);
                fStopped = false;
            }
            commitMeasurements();
            assertPerformance();
        } finally {
            debugUIPreferences.setValue(IDebugPreferenceConstants.CONSOLE_WRAP, false);
        }
    }

    /**
     * A warmup run of launching and having output piped to the console
     * @param workingCopy
     * @throws Exception
     */
    protected void warmupRun(ILaunchConfigurationWorkingCopy workingCopy) throws Exception {
        fWarmingUp = true;
        for (int i = 0; i < 5; i++) {
            launchWorkingCopyAndWait(workingCopy);
            fStopped = false;
        }
        fWarmingUp = false;
    }

    /**
     * Launches the specified working copy and then waits
     * @param workingCopy
     * @throws Exception
     */
    protected void launchWorkingCopyAndWait(final ILaunchConfigurationWorkingCopy workingCopy) throws Exception {
		ILaunch launch = null;
		try {
			launch = workingCopy.launch(ILaunchManager.RUN_MODE, null);
			synchronized (fLock) {
				if (!fStopped) {
					fLock.wait(360000);
				}
			}
		} finally {
			assertTrue("Test program took to long.", launch.isTerminated());
			getLaunchManager().removeLaunch(launch);
		}
    }

    /**
     * @see org.eclipse.debug.ui.console.IConsoleLineTrackerExtension#consoleClosed()
     */
    @Override
	public void consoleClosed() {
        if (fStarted) {
            stopMeasuring();
        }
        synchronized (fLock) {
            fStopped = true;
            fLock.notifyAll();
        }
    }

    /**
     * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
     */
    @Override
	public void init(IConsole console) {
        if (!fWarmingUp) {
            fStarted = true;
            startMeasuring();
        }
    }

    /**
     * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
     */
    @Override
	public void lineAppended(IRegion line) {
    }

    /**
     * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
     */
    @Override
	public void dispose() {
    }
}
