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

import com.sun.jdi.Value;

/**
 * Tests for JDI com.sun.jdi.Value.
 */
public class ValueTest extends AbstractJDITest {
	private Value fBoolean,
		fByte,
		fChar,
		fDouble,
		fFloat,
		fInteger,
		fLong,
		fShort,
		fArray,
		fObject;
	/**
	 * Creates a new test.
	 */
	public ValueTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get primitive values
		fBoolean = fVM.mirrorOf(true);
		fByte = fVM.mirrorOf((byte) 1);
		fChar = fVM.mirrorOf('a');
		fDouble = fVM.mirrorOf(12345.6789);
		fFloat = fVM.mirrorOf(12345.6789f);
		fInteger = fVM.mirrorOf(12345);
		fLong = fVM.mirrorOf(123456789l);
		fShort = fVM.mirrorOf((short) 12345);

		// Get object references
		fArray = getObjectArrayReference();
		fObject = getObjectReference();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new ValueTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.Value";
	}
	/**
	 * Test JDI type().
	 */
	public void testJDIType() {
		assertEquals("1", "boolean", fBoolean.type().name());
		assertEquals("2", "byte", fByte.type().name());
		assertEquals("3", "char", fChar.type().name());
		assertEquals("4", "double", fDouble.type().name());
		assertEquals("5", "float", fFloat.type().name());
		assertEquals("6", "int", fInteger.type().name());
		assertEquals("7", "long", fLong.type().name());
		assertEquals("8", "short", fShort.type().name());
		assertEquals("9", "java.lang.String[]", fArray.type().name());
		assertEquals(
			"10",
			"org.eclipse.debug.jdi.tests.program.MainClass",
			fObject.type().name());
	}
}
