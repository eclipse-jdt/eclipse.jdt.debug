package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

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
	 */
	public JDITestSuite(AbstractJDITest test) {
		super();
		fTest = test;
	}
	/**
	 * Runs the tests and collects their result in a TestResult.
	 */
	public void run(TestResult result) {
		fTest.setUp();
		super.run(result);
		fTest.tearDown();
	}
}
