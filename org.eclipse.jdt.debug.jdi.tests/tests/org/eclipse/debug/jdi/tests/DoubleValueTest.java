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

import static org.junit.Assert.assertNotEquals;

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

	public DoubleValueTest(String name) {
		super(name);
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
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
	 * Test JDI equals() and hashCode().
	 */
	public void testJDIEquality() {
		assertTrue("1", fValue.equals(fVM.mirrorOf(12345.6789)));
		assertFalse("2", fValue.equals(fVM.mirrorOf(98765.4321)));
		assertFalse("3", fValue.equals(new Object()));
		assertFalse("4", fValue.equals(null));
		assertEquals(
			"5",
			fValue.hashCode(),
			fVM.mirrorOf(12345.6789).hashCode());
		assertNotEquals("6", fValue.hashCode(), fVM.mirrorOf(98765.4321).hashCode());
	}
	/**
	 * Test JDI value().
	 */
	public void testJDIValue() {
		assertEquals("1", 12345.6789, fValue.value(), .0);
	}
}
