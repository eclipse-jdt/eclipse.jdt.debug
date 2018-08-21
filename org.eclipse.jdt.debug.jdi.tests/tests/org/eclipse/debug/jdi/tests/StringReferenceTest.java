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
	@Override
	public void localSetUp() {
		// Get static field "fString"
		fString = getStringReference();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new StringReferenceTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
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
