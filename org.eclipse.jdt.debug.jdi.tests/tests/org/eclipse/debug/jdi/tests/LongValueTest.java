package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import com.sun.jdi.LongValue;

/**
 * Tests for JDI com.sun.jdi.LongValue.
 */
public class LongValueTest extends AbstractJDITest {

	private LongValue fValue;
	/**
	 * Creates a new test.
	 */
	public LongValueTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	public void localSetUp() {
		// Get long value for 123456789l
		fValue = fVM.mirrorOf(123456789l);
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new LongValueTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 */
	public String getName() {
		return "com.sun.jdi.LongValue";
	}
	/**
	 * Test JDI equals() and hashCode().
	 */
	public void testJDIEquality() {
		assertTrue("1", fValue.equals(fVM.mirrorOf(123456789l)));
		assertTrue("2", !fValue.equals(fVM.mirrorOf(987654321l)));
		assertTrue("3", !fValue.equals(new Object()));
		assertTrue("4", !fValue.equals(null));
		assertEquals(
			"5",
			fValue.hashCode(),
			fVM.mirrorOf(123456789l).hashCode());
		assertTrue("6", fValue.hashCode() != fVM.mirrorOf(987654321l).hashCode());
	}
	/**
	 * Test JDI value().
	 */
	public void testJDIValue() {
		assertTrue("1", 123456789l == fValue.value());
	}
}
