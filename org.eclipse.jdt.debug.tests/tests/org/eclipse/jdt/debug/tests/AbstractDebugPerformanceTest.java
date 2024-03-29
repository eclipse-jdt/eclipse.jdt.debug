/*******************************************************************************
 *  Copyright (c) 2004, 2011 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests;

import org.eclipse.jdt.debug.tests.ui.AbstractDebugUiTests;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.test.performance.PerformanceTestCase;

/**
 * An abstract implementation of a debug performance test
 */
public class AbstractDebugPerformanceTest extends AbstractDebugUiTests {

	protected PerformanceMeter fPerformanceMeter;

	/**
	 * Constructs a performance test case with the given name.
	 * @param name the name of the performance test case
	 */
	public AbstractDebugPerformanceTest(String name) {
		super(name);
	}

	/**
	 * Overridden to create a default performance meter for this test case.
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Performance performance= Performance.getDefault();
		fPerformanceMeter= performance.createPerformanceMeter(performance.getDefaultScenarioId(this));
	}

	/**
	 * Overridden to dispose of the performance meter.
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		fPerformanceMeter.dispose();
	}

	/**
	 * Mark the scenario of this test case
	 * to be included into the global performance summary. The summary shows
	 * the given dimension of the scenario and labels the scenario with the short name.
	 *
	 * @param shortName a short (shorter than 40 characters) descriptive name of the scenario
	 * @param dimension the dimension to show in the summary
	 */
	public void tagAsGlobalSummary(String shortName, Dimension dimension) {
		Performance performance= Performance.getDefault();
		performance.tagAsGlobalSummary(fPerformanceMeter, shortName, new Dimension[] { dimension } );
	}

	/**
	 * Sets a degradation comment on this test.
	 *
	 * @param comment the reason for degradation.
	 */
	public void setDegradationComment(String comment) {
		Performance performance= Performance.getDefault();
		performance.setComment(fPerformanceMeter, Performance.EXPLAINS_DEGRADATION_COMMENT, comment);
	}

	/**
	 * Mark the scenario represented by the given PerformanceMeter
	 * to be included into the global performance summary. The summary shows
	 * the given dimensions of the scenario and labels the scenario with the short name.
	 *
	 * @param shortName a short (shorter than 40 characters) descritive name of the scenario
	 * @param dimensions an array of dimensions to show in the summary
	 */
	public void tagAsGlobalSummary(String shortName, Dimension[] dimensions) {
		Performance performance= Performance.getDefault();
		performance.tagAsGlobalSummary(fPerformanceMeter, shortName, dimensions );
	}

	/**
	 * Mark the scenario of this test case
	 * to be included into the component performance summary. The summary shows
	 * the given dimension of the scenario and labels the scenario with the short name.
	 *
	 * @param shortName a short (shorter than 40 characters) descritive name of the scenario
	 * @param dimension the dimension to show in the summary
	 */
	public void tagAsSummary(String shortName, Dimension dimension) {
		tagAsSummary(shortName, new Dimension[]{dimension});
	}

	/**
	 * Mark the scenario of this test case
	 * to be included into the component performance summary. The summary shows
	 * the given dimension of the scenario and labels the scenario with the short name.
	 *
	 * @param shortName a short (shorter than 40 characters) descritive name of the scenario
	 * @param dimensions an array of dimensions to show in the summary
	 */
	public void tagAsSummary(String shortName, Dimension[] dimensions) {
		Performance performance= Performance.getDefault();
		performance.tagAsSummary(fPerformanceMeter, shortName, dimensions);
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

	/**
	 * Asserts that the measurement specified by the given dimension
	 * is within a certain range with respect to some reference value.
	 * If the specified dimension isn't available, the call has no effect.
	 *
	 * @param dim the Dimension to check
	 * @param lowerPercentage a negative number indicating the percentage the measured value is allowed to be smaller than some reference value
	 * @param upperPercentage a positive number indicating the percentage the measured value is allowed to be greater than some reference value
	 * @throws RuntimeException if the properties do not hold
	 */
	protected void assertPerformanceInRelativeBand(Dimension dim, int lowerPercentage, int upperPercentage) {
		Performance.getDefault().assertPerformanceInRelativeBand(fPerformanceMeter, dim, lowerPercentage, upperPercentage);
	}

    /**
     * Sets a comment to explain performance degradation.
     * @param comment the explanation for a performance degradation.
     */
    protected void setComment(String comment) {
        Performance performance = Performance.getDefault();
        performance.setComment(fPerformanceMeter, Performance.EXPLAINS_DEGRADATION_COMMENT, comment);
    }
}
