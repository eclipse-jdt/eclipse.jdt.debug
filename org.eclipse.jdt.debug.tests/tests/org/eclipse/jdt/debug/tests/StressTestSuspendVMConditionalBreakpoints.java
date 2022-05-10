/*******************************************************************************
 *  Copyright (c) 2022 Simeon Andreev and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests;

import org.eclipse.jdt.debug.tests.breakpoints.SuspendVMConditionalBreakpointsTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Stress test suite for bug 575131. Not executed in automated tests.
 */
public class StressTestSuspendVMConditionalBreakpoints extends DebugSuite {

	private static final int STRESS_TEST_REPEAT_COUNT = 500;

	public static Test suite() {
		return new StressTestSuspendVMConditionalBreakpoints();
	}

	public StressTestSuspendVMConditionalBreakpoints() {
		for (int i = 0; i < STRESS_TEST_REPEAT_COUNT; ++i) {
			addTest(new TestSuite(SuspendVMConditionalBreakpointsTests.class));
		}
	}
}
