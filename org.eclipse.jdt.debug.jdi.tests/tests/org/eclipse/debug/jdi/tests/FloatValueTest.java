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

	public FloatValueTest(String name) {
		super(name);
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
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
	 * Test JDI equals() and hashCode().
	 */
	public void testJDIEquality() {
		assertTrue("1", fValue.equals(fVM.mirrorOf(123.45f)));
		assertFalse("2", fValue.equals(fVM.mirrorOf(54.321f)));
		assertFalse("3", fValue.equals(new Object()));
		assertFalse("4", fValue.equals(null));
		assertEquals("5", fValue.hashCode(), fVM.mirrorOf(123.45f).hashCode());
		assertNotEquals("6", fValue.hashCode(), fVM.mirrorOf(54.321f).hashCode());
	}
	/**
	 * Test JDI value().
	 */
	public void testJDIValue() {
		assertEquals("1", 123.45f, fValue.value(), .0F);
	}
}
