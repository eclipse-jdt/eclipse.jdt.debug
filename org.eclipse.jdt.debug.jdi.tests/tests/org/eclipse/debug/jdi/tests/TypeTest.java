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

import com.sun.jdi.Type;

/**
 * Tests for JDI com.sun.jdi.Type.
 */
public class TypeTest extends AbstractJDITest {
	private Type
		fArrayType,
		fClassType,
		fInterfaceType,
		// primitive types
		fShortType,
		fByteType,
		fIntegerType,
		fLongType,
		fFloatType,
		fDoubleType,
		fCharType,
		fBooleanType,
		// One-dimensional primitive arrays
		fByteArrayType,
		fShortArrayType,
		fIntArrayType,
		fLongArrayType,
		fFloatArrayType,
		fDoubleArrayType,
		fCharArrayType,
		fBooleanArrayType,
		// Two-dimensional primitive arrays
		fByteDoubleArrayType,
		fShortDoubleArrayType,
		fIntDoubleArrayType,
		fLongDoubleArrayType,
		fFloatDoubleArrayType,
		fDoubleDoubleArrayType,
		fCharDoubleArrayType,
		fBooleanDoubleArrayType;

	/**
	 * Creates a new test.
	 */
	public TypeTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get reference types
		fArrayType = getArrayType();
		fClassType = getMainClass();
		fInterfaceType = getInterfaceType();
		// Get primitive types
		fBooleanType = (fVM.mirrorOf(true).type());
		fByteType = (fVM.mirrorOf((byte) 1).type());
		fCharType = (fVM.mirrorOf('a').type());
		fDoubleType = (fVM.mirrorOf(12345.6789).type());
		fFloatType = (fVM.mirrorOf(12345.6789f).type());
		fIntegerType = (fVM.mirrorOf(12345).type());
		fLongType = (fVM.mirrorOf(123456789l).type());
		fShortType = (fVM.mirrorOf((short) 12345).type());
		// Get one-dimensional primitive arrays
		fByteArrayType = getByteArrayType();
		fShortArrayType = getShortArrayType();
		fIntArrayType = getIntArrayType();
		fLongArrayType = getLongArrayType();
		fFloatArrayType = getFloatArrayType();
		fDoubleArrayType = getDoubleArrayType();
		fCharArrayType = getCharArrayType();
		fBooleanArrayType = getBooleanArrayType();
		//  Get two-dimensional primitive arrays
		fByteDoubleArrayType = getByteDoubleArrayType();
		fShortDoubleArrayType = getShortDoubleArrayType();
		fIntDoubleArrayType = getIntDoubleArrayType();
		fLongDoubleArrayType = getLongDoubleArrayType();
		fFloatDoubleArrayType = getFloatDoubleArrayType();
		fDoubleDoubleArrayType = getDoubleDoubleArrayType();
		fCharDoubleArrayType = getCharDoubleArrayType();
		fBooleanDoubleArrayType = getBooleanDoubleArrayType();

	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new TypeTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.Type";
	}
	/**
	 * Test JDI signature().
	 */
	public void testJDISignature() {
		assertEquals("1", "[Ljava/lang/String;", fArrayType.signature());
		assertEquals("2", "Lorg/eclipse/debug/jdi/tests/program/MainClass;", fClassType.signature());
		assertEquals("3", "Lorg/eclipse/debug/jdi/tests/program/Printable;", fInterfaceType.signature());
		// Primitive types
		assertEquals("4", "S", fShortType.signature());
		assertEquals("5", "B", fByteType.signature());
		assertEquals("6", "I", fIntegerType.signature());
		assertEquals("7", "J", fLongType.signature());
		assertEquals("8", "F", fFloatType.signature());
		assertEquals("9", "D", fDoubleType.signature());
		assertEquals("10", "C", fCharType.signature());
		assertEquals("11", "Z", fBooleanType.signature());
		// One-dimensional primitive arrays
		assertEquals("12", "[B", fByteArrayType.signature());
		assertEquals("13", "[S", fShortArrayType.signature());
		assertEquals("14", "[I", fIntArrayType.signature());
		assertEquals("15", "[J", fLongArrayType.signature());
		assertEquals("16", "[F", fFloatArrayType.signature());
		assertEquals("17", "[D", fDoubleArrayType.signature());
		assertEquals("18", "[C", fCharArrayType.signature());
		assertEquals("19", "[Z", fBooleanArrayType.signature());
		// Two-dimensional primitive arrays
		assertEquals("20", "[[B", fByteDoubleArrayType.signature());
		assertEquals("21", "[[S", fShortDoubleArrayType.signature());
		assertEquals("22", "[[I", fIntDoubleArrayType.signature());
		assertEquals("23", "[[J", fLongDoubleArrayType.signature());
		assertEquals("24", "[[F", fFloatDoubleArrayType.signature());
		assertEquals("25", "[[D", fDoubleDoubleArrayType.signature());
		assertEquals("26", "[[C", fCharDoubleArrayType.signature());
		assertEquals("27", "[[Z", fBooleanDoubleArrayType.signature());
	}
	/**
	 * Test JDI typeName().
	 */
	public void testJDITypeName() {
		assertEquals("1", "java.lang.String[]", fArrayType.name());
		assertEquals("2", "org.eclipse.debug.jdi.tests.program.MainClass", fClassType.name());
		assertEquals("3", "org.eclipse.debug.jdi.tests.program.Printable", fInterfaceType.name());
		// Primitive types
		assertEquals("4", "byte", fByteType.name());
		assertEquals("5", "short", fShortType.name());
		assertEquals("6", "int", fIntegerType.name());
		assertEquals("7", "long", fLongType.name());
		assertEquals("8", "float", fFloatType.name());
		assertEquals("9", "double", fDoubleType.name());
		assertEquals("10", "char", fCharType.name());
		assertEquals("11", "boolean", fBooleanType.name());
		// One-dimensional primitive arrays
		assertEquals("12", "byte[]", fByteArrayType.name());
		assertEquals("13", "short[]", fShortArrayType.name());
		assertEquals("14", "int[]", fIntArrayType.name());
		assertEquals("15", "long[]", fLongArrayType.name());
		assertEquals("16", "float[]", fFloatArrayType.name());
		assertEquals("17", "double[]", fDoubleArrayType.name());
		assertEquals("18", "char[]", fCharArrayType.name());
		assertEquals("19", "boolean[]", fBooleanArrayType.name());
		// Two-dimensional primitive arrays
		assertEquals("20", "byte[][]", fByteDoubleArrayType.name());
		assertEquals("21", "short[][]", fShortDoubleArrayType.name());
		assertEquals("22", "int[][]", fIntDoubleArrayType.name());
		assertEquals("23", "long[][]", fLongDoubleArrayType.name());
		assertEquals("24", "float[][]", fFloatDoubleArrayType.name());
		assertEquals("25", "double[][]", fDoubleDoubleArrayType.name());
		assertEquals("26", "char[][]", fCharDoubleArrayType.name());
		assertEquals("27", "boolean[][]", fBooleanDoubleArrayType.name());
	}
}
