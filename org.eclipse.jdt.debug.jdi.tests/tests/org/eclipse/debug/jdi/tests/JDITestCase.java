/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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

import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * Wrapper to be able to use the JDI tests in a test suite without
 * starting and shutting down the VM after each test.
 */
public class JDITestCase extends TestCase {
	private AbstractJDITest fTest;
	/**
	 * Creates a new test for the given JDI test.
	 * @param test
	 * @param name
	 */
	public JDITestCase(AbstractJDITest test, String name) {
		super(name);
		fTest = test;
	}
	/**
	 * Override to run the test and assert its state.
	 * @exception Throwable if any exception is thrown
	 */
	@Override
	protected void runTest() throws Throwable {
		Method runMethod = null;
		try {
			runMethod = fTest.getClass().getMethod(getName(), new Class[0]);
		} catch (NoSuchMethodException e) {
			e.fillInStackTrace();
			throw e;
		}
		try {
			fTest.verbose("Running " + getName());
			runMethod.invoke(fTest, new Object[0]);
		} catch (java.lang.reflect.InvocationTargetException e) {
			if (e.getTargetException() instanceof NotYetImplementedException)
				System.out.println("\n" + getName() + " is not yet implemented.");
			else {
				e.fillInStackTrace();
				throw e.getTargetException();
			}
		} catch (IllegalAccessException e) {
			e.fillInStackTrace();
			throw e;
		}
	}
	/**
	 * Init tests
	 */
	@Override
	protected void setUp() {
		// Ignore setUp since it is done once for all tests in the test suite
	}
	/**
	 * Tears down the fixture.
	 */
	@Override
	protected void tearDown() {
		// Ignore tearDown since it is done once for all tests in the test suite
	}
	/**
	 * Returns a string representation of the test case
	 * @see junit.framework.TestCase#toString()
	 */
	@Override
	public String toString() {
		return fTest.getClass().getName() + "." + getName() + "()";
	}
}
