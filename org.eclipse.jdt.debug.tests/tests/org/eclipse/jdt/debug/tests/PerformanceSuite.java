/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.debug.tests.performance.PerfBreakpointTests;
import org.eclipse.jdt.debug.tests.performance.PerfConditionalBreakpointsTests;
import org.eclipse.jdt.debug.tests.performance.PerfConsoleTests;
import org.eclipse.jdt.debug.tests.performance.PerfContextualLaunchMenu;
import org.eclipse.jdt.debug.tests.performance.PerfDebugBaselineTest;
import org.eclipse.jdt.debug.tests.performance.PerfSteppingTests;

/**
 * Tests for integration and nightly builds.
 */
public class PerformanceSuite extends DebugSuite {
	
	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new PerformanceSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public PerformanceSuite() {
		addTest(new TestSuite(ProjectCreationDecorator.class));
		
		addTest(new TestSuite(PerfContextualLaunchMenu.class));
		addTest(new TestSuite(PerfDebugBaselineTest.class));
		addTest(new TestSuite(PerfBreakpointTests.class));
		addTest(new TestSuite(PerfConditionalBreakpointsTests.class));
		addTest(new TestSuite(PerfSteppingTests.class));
		addTest(new TestSuite(PerfConsoleTests.class));
		
	}
}

