/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.jdi.tests;

import java.lang.reflect.Method;
import junit.framework.TestCase;

/**
 * Wrapper to be able to use the JDI tests in a test suite without
 * starting and shuting down the VM after each test.
 */
public class JDITestCase extends TestCase {
	private AbstractJDITest fTest;
	/**
	 * Creates a new test for the given JDI test.
	 */
	public JDITestCase(AbstractJDITest test, String name) {
		super(name);
		fTest = test;
	}
	/**
	 * Override to run the test and assert its state.
	 * @exception Throwable if any exception is thrown
	 */
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
			runMethod.invoke(fTest, new Class[0]);
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
	protected void setUp() {
		// Ignore setUp since it is done once for all tests in the test suite
	}
	/**
	 * Tears down the fixture.
	 */
	protected void tearDown() {
		// Ignore tearDown since it is done once for all tests in the test suite
	}
	/**
	 * Returns a string representation of the test case
	 */
	public String toString() {
		return fTest.getClass().getName() + "." + getName() + "()";
	}
}
