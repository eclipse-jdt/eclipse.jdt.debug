/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests;

import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;

public class AbstractDebugPerformanceTest extends AbstractDebugTest {

    protected PerformanceMeter fPerformanceMeter;

    public AbstractDebugPerformanceTest(String name) {
        super(name);
    }
    
    /**
	 * Overidden to create a default performance meter for this test case.
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		Performance performance= Performance.getDefault();
		fPerformanceMeter= performance.createPerformanceMeter(performance.getDefaultScenarioId(this));
	}

	/**
	 * Overidden to disposee of the performance meter.
	 * @throws Exception
	 */
	protected void tearDown() throws Exception {
		fPerformanceMeter.dispose();
	}
	
	/**
	 * Called from within a test case immediately before the code to measure is run.
	 * It starts capturing of performance data.
	 * Must be followed by a call to {@link PerformanceTestCase#stopMeasuring()} before subsequent calls
	 * to this method or {@link PerformanceTestCase#commitMeasurements()}.
	 */
	protected void startMeasuring() {
		fPerformanceMeter.start();
	}
	
	protected void stopMeasuring() {
		fPerformanceMeter.stop();
	}
	
	protected void commitMeasurements() {
		fPerformanceMeter.commit(); 
	}

	/**
	 * Asserts default properties of the measurements captured for this test case.
	 * 
	 * @throws RuntimeException if the properties do not hold
	 */
	protected void assertPerformance() {
		Performance.getDefault().assertPerformance(fPerformanceMeter);
	}

    protected void finishMeasuring() {
        stopMeasuring();
        commitMeasurements();
        assertPerformance();
    }
}
