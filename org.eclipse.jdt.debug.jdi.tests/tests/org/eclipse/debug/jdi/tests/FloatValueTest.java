package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import com.sun.jdi.FloatValue;

/**
 * Tests for JDI com.sun.jdi.FloatValue.
 */
public class FloatValueTest extends AbstractJDITest {

	private FloatValue fValue;
	/**
	 * Creates a new test.
	 */
	public FloatValueTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	public void localSetUp() {
		// Get float value for 123.45f
		fValue = fVM.mirrorOf(123.45f);
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new FloatValueTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 */
	public String getName() {
		return "com.sun.jdi.FloatType";
	}
	/**
	 * Test JDI equals() and hashCode().
	 */
	public void testJDIEquality() {
		assertTrue("1", fValue.equals(fVM.mirrorOf(123.45f)));
		assertTrue("2", !fValue.equals(fVM.mirrorOf(54.321f)));
		assertTrue("3", !fValue.equals(new Object()));
		assertTrue("4", !fValue.equals(null));
		assertEquals("5", fValue.hashCode(), fVM.mirrorOf(123.45f).hashCode());
		assertTrue("6", fValue.hashCode() != fVM.mirrorOf(54.321f).hashCode());
	}
	/**
	 * Test JDI value().
	 */
	public void testJDIValue() {
		assertTrue("1", 123.45f == fValue.value());
	}
}
