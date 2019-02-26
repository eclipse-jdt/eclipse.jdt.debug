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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest;
import org.eclipse.test.performance.Dimension;

/**
 * Tests performance of conditional breakpoints.
 */
public class PerfVariableDetailTests extends AbstractDebugPerformanceTest implements IValueDetailListener {

    private Object fLock = new Object();

    /**
     * Constructor
     * @param name
     */
    public PerfVariableDetailTests(String name) {
        super(name);
    }

    /**
     * @see org.eclipse.debug.ui.IValueDetailListener#detailComputed(org.eclipse.debug.core.model.IValue, java.lang.String)
     */
    @Override
	public void detailComputed(IValue value, String result) {
    	synchronized (fLock) {
    		fLock.notifyAll();
		}
	}

    /**
     * Tests the performance of calculating the 'toString' method
     * @throws Exception
     */
    public void testToStringDetails() throws Exception {
        // just in case
    	tagAsSummary("Computing variable toString() details iteratively", Dimension.ELAPSED_PROCESS);
        removeAllBreakpoints();
        String typeName = "VariableDetails";
        createLineBreakpoint(27, typeName);
        IJavaThread thread = null;
        try {
        	ILaunchConfiguration configuration = getLaunchConfiguration(typeName);
        	thread = launchToBreakpoint(configuration, false);
        	IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
        	assertNotNull("Missing top stack frame", frame);
        	IJavaVariable variable = frame.findVariable("v");
        	assertNotNull("Missing variable 'v'", variable);
        	IDebugModelPresentation presentation = DebugUITools.newDebugModelPresentation("org.eclipse.jdt.debug");
        	IValue value = variable.getValue();

        	// warm up
        	for (int i = 0; i < 100; i++) {
        		synchronized (fLock) {
        			presentation.computeDetail(value, this);
        			fLock.wait(30000);
        		}
        	}

        	// test
        	for (int i = 0; i < 300; i++) {
        		startMeasuring();
        		for (int j = 0; j < 150; j++) {
	        		synchronized (fLock) {
	        			presentation.computeDetail(value, this);
	        			fLock.wait(30000);
	        		}
        		}
        		stopMeasuring();
        	}

        	commitMeasurements();
        	assertPerformance();

        } finally {
        	removeAllBreakpoints();
        	terminateAndRemove(thread);
        }
    }
}
