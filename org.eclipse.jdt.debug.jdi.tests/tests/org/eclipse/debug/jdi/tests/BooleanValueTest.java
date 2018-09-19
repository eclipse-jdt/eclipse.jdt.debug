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

import com.sun.jdi.BooleanValue;

/**
 * Tests for JDI com.sun.jdi.BooleanValue.
 */
public class BooleanValueTest extends AbstractJDITest {

	private BooleanValue fValue;
	/**
	 * Creates a new test.
	 */
	public BooleanValueTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get boolean value for "true"
		fValue = fVM.mirrorOf(true);
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new BooleanValueTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.BooleanValue";
	}
	/**
	 * Test JDI equals() and hashCode().
	 */
	public void testJDIEquality() {
		assertTrue("1", fValue.equals(fVM.mirrorOf(true)));
		assertTrue("2", !fValue.equals(fVM.mirrorOf(false)));
		assertTrue("3", !fValue.equals(new Object()));
		assertTrue("4", !fValue.equals(null));
		assertEquals("5", fValue.hashCode(), fVM.mirrorOf(true).hashCode());
		assertTrue("6", fValue.hashCode() != fVM.mirrorOf(false).hashCode());
	}
	/**
	 * Test JDI value().
	 */
	public void testJDIValue() {
		assertTrue("1", true == fValue.value());
	}
}
