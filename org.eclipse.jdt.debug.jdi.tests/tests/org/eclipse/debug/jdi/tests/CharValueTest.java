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

import com.sun.jdi.CharValue;

/**
 * Tests for JDI com.sun.jdi.CharValue.
 */
public class CharValueTest extends AbstractJDITest {

	private CharValue fValue;
	/**
	 * Creates a new test.
	 */
	public CharValueTest() {
		super();
	}

	public CharValueTest(String name) {
		super(name);
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get char value for 'a'
		fValue = fVM.mirrorOf('a');
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new CharValueTest().runSuite(args);
	}

	/**
	 * Test JDI equals() and hashCode().
	 */
	public void testJDIEquality() {
		assertTrue("1", fValue.equals(fVM.mirrorOf('a')));
		assertFalse("2", fValue.equals(fVM.mirrorOf('b')));
		assertFalse("3", fValue.equals(new Object()));
		assertFalse("4", fValue.equals(null));
		assertEquals("5", fValue.hashCode(), fVM.mirrorOf('a').hashCode());
		assertNotEquals("6", fValue.hashCode(), fVM.mirrorOf('b').hashCode());
	}
	/**
	 * Test JDI value().
	 */
	public void testJDIValue() {
		assertEquals("1", 'a', fValue.value());
	}
}
