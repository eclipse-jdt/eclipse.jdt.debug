/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.debug.tests.performance.BreakpointManagerPerfTests;
import org.eclipse.jdt.debug.tests.performance.PerfBreakpointTests;
import org.eclipse.jdt.debug.tests.performance.PerfConditionalBreakpointsTests;
import org.eclipse.jdt.debug.tests.performance.PerfConsoleTests;
import org.eclipse.jdt.debug.tests.performance.PerfContextualLaunchMenu;
import org.eclipse.jdt.debug.tests.performance.PerfSteppingTests;
import org.eclipse.jdt.debug.tests.performance.PerfVariableDetailTests;

/**
 * Tests for integration and nightly builds.
 */
public class PerformanceSuite extends DebugSuite {

    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     * @return the test suite
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
        addTest(new TestSuite(PerfBreakpointTests.class));
        addTest(new TestSuite(PerfConditionalBreakpointsTests.class));
        addTest(new TestSuite(PerfSteppingTests.class));
        addTest(new TestSuite(PerfConsoleTests.class));
        addTest(new TestSuite(PerfVariableDetailTests.class));
        addTest(new TestSuite(BreakpointManagerPerfTests.class));
    }
}
