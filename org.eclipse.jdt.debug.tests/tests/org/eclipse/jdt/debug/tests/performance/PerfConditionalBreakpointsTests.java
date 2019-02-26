/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest;
import org.eclipse.test.performance.Dimension;

/**
 * Tests performance of conditional breakpoints.
 */
public class PerfConditionalBreakpointsTests extends AbstractDebugPerformanceTest {
    private String fTypeName = "PerfLoop";

    private int fHitCount = 0;

    private IJavaLineBreakpoint fBP;

    private Exception fException;

    private boolean fConditionalBreakpointSet = false;

    private boolean fWarmUpComplete = false;

    private int fWarmUpRuns = 2;

    private int fMeasuredRuns = 10;

    private class BreakpointListener implements IDebugEventSetListener {
        /**
         * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
         */
        @Override
		public void handleDebugEvents(DebugEvent[] events) {
            for (int i = 0; i < events.length; i++) {
                DebugEvent event = events[i];
                if (event.getKind() == DebugEvent.SUSPEND && event.getDetail() == DebugEvent.BREAKPOINT) {
                    IJavaThread source = (IJavaThread) event.getSource();
                    breakpointHit(source);
                }
            }
        }
    }

    /**
     * Constructor
     * @param name
     */
    public PerfConditionalBreakpointsTests(String name) {
        super(name);
    }

    /**
     * Tests the performance of launching to conditional breakpoints
     * @throws Exception
     */
    public void testConditionalBreakpoints() throws Exception {
        tagAsSummary("Conditional Breakpoint Test", Dimension.ELAPSED_PROCESS);
        // just in case
        removeAllBreakpoints();

        fBP = createLineBreakpoint(25, fTypeName);

        BreakpointListener listener = new BreakpointListener();
        DebugPlugin.getDefault().addDebugEventListener(listener);
        ILaunchConfiguration config = getLaunchConfiguration(fTypeName);
        try {
            launchAndTerminate(config, 5 * 60 * 1000, false);

            if (fException != null) {
                throw fException;
            }

            commitMeasurements();
            assertPerformance();

            removeAllBreakpoints();
        } finally {
            DebugPlugin.getDefault().removeDebugEventListener(listener);
        }
    }

    private synchronized void breakpointHit(IJavaThread thread) {
        try {
            if (!fConditionalBreakpointSet) {
                fBP.delete();
                fBP = createConditionalLineBreakpoint(25, fTypeName, "i%100==0", true);
                fConditionalBreakpointSet = true;
            } else if (!fWarmUpComplete) {
                fHitCount++;
                if (fHitCount == fWarmUpRuns) {
                    fWarmUpComplete = true;
                    fHitCount = 0;
                }
                return;
            } else {
                if (fHitCount > 0) {
                    stopMeasuring();
                }
                fHitCount++;
                if (fHitCount <= fMeasuredRuns) {
                    System.gc();
                    startMeasuring();
                } else {
                    fBP.delete();
                }
            }
        } catch (Exception e) {
            fException = e;
            removeAllBreakpoints();
        } finally {
            try {
                thread.resume();
            } catch (DebugException e) {
                fException = e;
            }
        }
    }
}
