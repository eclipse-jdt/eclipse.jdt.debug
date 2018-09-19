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

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;

/**
 * Tests for JDI com.sun.jdi.Field.
 */
public class FieldTest extends AbstractJDITest {

	private Field fField;
	/**
	 * Creates a new test.
	 */
	public FieldTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get static field "fObject"
		fField = getField();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new FieldTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.Field";
	}
	/**
	 * Test JDI equals() and hashCode().
	 */
	public void testJDIEquality() {
		assertTrue("1", fField.equals(fField));
		Field other = getField("fString");
		assertTrue("2", !fField.equals(other));
		assertTrue("3", !fField.equals(new Object()));
		assertTrue("4", !fField.equals(null));
	}
	/**
	 * Test JDI isTransient().
	 */
	public void testJDIIsTransient() {
		assertTrue("1", !fField.isTransient());
	}
	/**
	 * Test JDI isVolatile().
	 */
	public void testJDIIsVolatile() {
		assertTrue("1", !fField.isVolatile());
	}
	/**
	 * Test JDI type().
	 */
	public void testJDIType() {
		try {
			assertEquals("1", getMainClass(), fField.type());
		} catch (ClassNotLoadedException e) {
			assertTrue("2", false);
		}
	}
	/**
	 * Test JDI typeName().
	 */
	public void testJDITypeName() {
		assertEquals(
			"1",
			"org.eclipse.debug.jdi.tests.program.MainClass",
			fField.typeName());
	}
}
