package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import com.sun.jdi.StringReference;

/**
 * Tests for JDI com.sun.jdi.StringReference
 * and JDWP String command set.
 */
public class StringReferenceTest extends AbstractJDITest {

	private StringReference fString;
	/**
	 * Creates a new test.
	 */
	public StringReferenceTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	public void localSetUp() {
		// Get static field "fString"
		fString = getStringReference();
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new StringReferenceTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 */
	public String getName() {
		return "com.sun.jdi.StringReference";
	}
	/**
	 * Test JDI value() and JDWP 'String - Get value'.
	 */
	public void testJDIValue() {
		String value = fString.value();
		assertEquals("1", "Hello World", value);
	}
}
