package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import com.sun.jdi.ByteValue;

/**
 * Tests for JDI com.sun.jdi.ByteValue.
 */
public class ByteValueTest extends AbstractJDITest {

	private ByteValue fValue;
	/**
	 * Creates a new test.
	 */
	public ByteValueTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	public void localSetUp() {
		// Get byte value for 1
		fValue = fVM.mirrorOf((byte) 1);
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new ByteValueTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 */
	public String getName() {
		return "com.sun.jdi.ByteValue";
	}
	/**
	 * Test JDI equals() and hashCode().
	 */
	public void testJDIEquality() {
		assertTrue("1", fValue.equals(fVM.mirrorOf((byte) 1)));
		assertTrue("2", !fValue.equals(fVM.mirrorOf((byte) 2)));
		assertTrue("3", !fValue.equals(new Object()));
		assertTrue("4", !fValue.equals(null));
		assertEquals("5", fValue.hashCode(), fVM.mirrorOf((byte) 1).hashCode());
		assertTrue("6", fValue.hashCode() != fVM.mirrorOf((byte) 2).hashCode());
	}
	/**
	 * Test JDI value().
	 */
	public void testJDIValue() {
		assertTrue("1", 1 == fValue.value());
	}
}
