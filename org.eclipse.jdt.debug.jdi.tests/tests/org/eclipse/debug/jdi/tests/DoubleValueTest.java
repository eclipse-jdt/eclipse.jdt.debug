package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import com.sun.jdi.DoubleValue;

/**
 * Tests for JDI com.sun.jdi.DoubleValue.
 */
public class DoubleValueTest extends AbstractJDITest {

	private DoubleValue fValue;
	/**
	 * Creates a new test.
	 */
	public DoubleValueTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	public void localSetUp() {
		// Get double value for 12345.6789
		fValue = fVM.mirrorOf(12345.6789);
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new DoubleValueTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 */
	public String getName() {
		return "com.sun.jdi.DoubleValue";
	}
	/**
	 * Test JDI equals() and hashCode().
	 */
	public void testJDIEquality() {
		assertTrue("1", fValue.equals(fVM.mirrorOf(12345.6789)));
		assertTrue("2", !fValue.equals(fVM.mirrorOf(98765.4321)));
		assertTrue("3", !fValue.equals(new Object()));
		assertTrue("4", !fValue.equals(null));
		assertEquals(
			"5",
			fValue.hashCode(),
			fVM.mirrorOf(12345.6789).hashCode());
		assertTrue("6", fValue.hashCode() != fVM.mirrorOf(98765.4321).hashCode());
	}
	/**
	 * Test JDI value().
	 */
	public void testJDIValue() {
		assertTrue("1", 12345.6789 == fValue.value());
	}
}
