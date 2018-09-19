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

import com.sun.jdi.PrimitiveValue;

/**
 * Tests for JDI com.sun.jdi.PrimitiveValue.
 */
public class PrimitiveValueTest extends AbstractJDITest {

	private PrimitiveValue fBoolean,
		fByte,
		fChar,
		fDouble,
		fFloat,
		fInt,
		fLong,
		fShort;
	/**
	 * Creates a new test.
	 */
	public PrimitiveValueTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get all kinds of prinitive values
		fBoolean = fVM.mirrorOf(true);
		fByte = fVM.mirrorOf((byte) 1);
		fChar = fVM.mirrorOf('a');
		fDouble = fVM.mirrorOf(12345.6789);
		fFloat = fVM.mirrorOf(12345.6789f);
		fInt = fVM.mirrorOf(12345);
		fLong = fVM.mirrorOf(123456789l);
		fShort = fVM.mirrorOf((short) 12345);
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new PrimitiveValueTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.PrimitiveValue";
	}
	/**
	 * Test JDI booleanValue().
	 */
	public void testJDIBooleanValue() {
		assertTrue("1", fBoolean.booleanValue());
		assertTrue("2", fByte.booleanValue());
		assertTrue("3", fChar.booleanValue());
		assertTrue("4", fDouble.booleanValue());
		assertTrue("5", fFloat.booleanValue());
		assertTrue("6", fInt.booleanValue());
		assertTrue("7", fLong.booleanValue());
		assertTrue("8", fShort.booleanValue());
	}
	/**
	 * Test JDI byteValue().
	 */
	public void testJDIByteValue() {
		assertEquals("1", (byte) 1, fBoolean.byteValue());
		assertEquals("2", (byte) 1, fByte.byteValue());
		assertEquals("3", (byte) 97, fChar.byteValue());
		assertEquals("4", (byte) 57, fDouble.byteValue());
		assertEquals("5", (byte) 57, fFloat.byteValue());
		assertEquals("6", (byte) 57, fInt.byteValue());
		assertEquals("7", (byte) 21, fLong.byteValue());
		assertEquals("8", (byte) 57, fShort.byteValue());
	}
	/**
	 * Test JDI charValue().
	 */
	public void testJDICharValue() {
		assertEquals("1", (char) 1, fBoolean.charValue());
		assertEquals("2", (char) 1, fByte.charValue());
		assertEquals("3", 'a', fChar.charValue());
		assertEquals("4", (char) 12345, fDouble.charValue());
		assertEquals("5", (char) 12345, fFloat.charValue());
		assertEquals("6", (char) 12345, fInt.charValue());
		assertEquals("7", (char) 52501, fLong.charValue());
		assertEquals("8", (char) 12345, fShort.charValue());
	}
	/**
	 * Test JDI doubleValue().
	 */
	public void testJDIDoubleValue() {
		assertEquals("1", 1, fBoolean.doubleValue(), 0);
		assertEquals("2", 1, fByte.doubleValue(), 0);
		assertEquals("3", 97, fChar.doubleValue(), 0);
		assertEquals("4", 12345.6789, fDouble.doubleValue(), 0);
		assertEquals("5", 12345.6789, fFloat.doubleValue(), 0.001);
		assertEquals("6", 12345, fInt.doubleValue(), 0);
		assertEquals("7", 123456789, fLong.doubleValue(), 0);
		assertEquals("8", 12345, fShort.doubleValue(), 0);
	}
	/**
	 * Test JDI floatValue().
	 */
	public void testJDIFloatValue() {
		assertEquals("1", 1, fBoolean.floatValue(), 0);
		assertEquals("2", 1, fByte.floatValue(), 0);
		assertEquals("3", 97, fChar.floatValue(), 0);
		assertEquals("4", 12345.6789f, fDouble.floatValue(), 0);
		assertEquals("5", 12345.6789f, fFloat.floatValue(), 0.001);
		assertEquals("6", 12345, fInt.floatValue(), 0);
		assertEquals("7", 123456789, fLong.floatValue(), 100);
		assertEquals("8", 12345, fShort.floatValue(), 0);
	}
	/**
	 * Test JDI intValue().
	 */
	public void testJDIIntValue() {
		assertEquals("1", 1, fBoolean.intValue());
		assertEquals("2", 1, fByte.intValue());
		assertEquals("3", 97, fChar.intValue());
		assertEquals("4", 12345, fDouble.intValue());
		assertEquals("5", 12345, fFloat.intValue());
		assertEquals("6", 12345, fInt.intValue());
		assertEquals("7", 123456789, fLong.intValue());
		assertEquals("8", 12345, fShort.intValue());
	}
	/**
	 * Test JDI longValue().
	 */
	public void testJDILongValue() {
		assertEquals("1", 1l, fBoolean.longValue());
		assertEquals("2", 1l, fByte.longValue());
		assertEquals("3", 97l, fChar.longValue());
		assertEquals("4", 12345l, fDouble.longValue());
		assertEquals("5", 12345l, fFloat.longValue());
		assertEquals("6", 12345l, fInt.longValue());
		assertEquals("7", 123456789l, fLong.longValue());
		assertEquals("8", 12345l, fShort.longValue());
	}
	/**
	 * Test JDI shortValue().
	 */
	public void testJDIShortValue() {
		assertEquals("1", 1, fBoolean.shortValue());
		assertEquals("2", 1, fByte.shortValue());
		assertEquals("3", 97, fChar.shortValue());
		assertEquals("4", 12345, fDouble.shortValue());
		assertEquals("5", 12345, fFloat.shortValue());
		assertEquals("6", 12345, fInt.shortValue());
		assertEquals("7", -13035, fLong.shortValue());
		assertEquals("8", 12345, fShort.shortValue());
	}
}
