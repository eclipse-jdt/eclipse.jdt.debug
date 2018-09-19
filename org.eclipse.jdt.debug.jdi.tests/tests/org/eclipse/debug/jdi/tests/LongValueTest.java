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
	@Override
	public void localSetUp() {
		// Get long value for 123456789l
		fValue = fVM.mirrorOf(123456789l);
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new LongValueTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
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
