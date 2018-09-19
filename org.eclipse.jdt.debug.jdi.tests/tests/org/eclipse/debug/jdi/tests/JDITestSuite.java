/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

package org.eclipse.debug.jdi.tests;

import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * A JDI test suite runs all tests defined in a JDI test case class.
 * It runs the setUp method once before running the tests and the
 * tearDown method once after.
 */
public class JDITestSuite extends TestSuite {
	private AbstractJDITest fTest;
	/**
	 * Creates a new test suite for the given JDI test.
	 * @param test
	 */
	public JDITestSuite(AbstractJDITest test) {
		super();
		fTest = test;
	}
	/**
	 * Runs the tests and collects their result in a TestResult.
	 * @see junit.framework.TestSuite#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		fTest.setUp();
		super.run(result);
		fTest.tearDown();
	}
}
