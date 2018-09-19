/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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

import java.util.List;

import com.sun.jdi.Type;

/**
 * Tests for JDI com.sun.jdi.VirtualMachine.classesByName
 */
public class ClassesByNameTest extends AbstractJDITest {

	/**
	 * Creates a new test.
	 */
	public ClassesByNameTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {

	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new ClassesByNameTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.VirtualMachine.classesByName";
	}
	/**
	 * Test that there is a class object for 'int[]'
	 */
	public void testJDIIntArray() {
		List<?> classes = fVM.classesByName("int[]");
		assertTrue(
			"Should be a class for int[]",
			classes.size() == 1
				&& ((Type) classes.get(0)).signature().equals("[I"));
	}
	/**
	 * Test that there is a class object for 'int[][]'
	 */
	public void testJDIIntDoubleArray() {
		List<?> classes = fVM.classesByName("int[][]");
		assertTrue(
			"Should be a class for int[][]",
			classes.size() == 1
				&& ((Type) classes.get(0)).signature().equals("[[I"));
	}

	/**
	 * tests signature for an array of long values
	 */
	public void testJDILongArray() {
		List<?> classes = fVM.classesByName("long[]");
		assertTrue(
			"Should be a class for long[]",
			classes.size() == 1
				&& ((Type) classes.get(0)).signature().equals("[J"));
	}

	/**
	 * tests signature of a two dimensional array of long values
	 */
	public void testJDILongDoubleArray() {
		List<?> classes = fVM.classesByName("long[][]");
		assertTrue(
			"Should be a class for long[][]",
			classes.size() == 1
				&& ((Type) classes.get(0)).signature().equals("[[J"));
	}

	/**
	 * Test that there is a class object for 'java.lang.String[]'
	 */
	public void testJDIStringArray() {
		List<?> classes = fVM.classesByName("java.lang.String[]");
		assertTrue(
			"Should be a class for java.lang.String[]",
			classes.size() == 1
				&& ((Type) classes.get(0)).signature().equals(
					"[Ljava/lang/String;"));
	}

	/**
	 * Test that there is a class object for 'java.lang.String'
	 */
	public void testJDIString() {
		List<?> classes = fVM.classesByName("java.lang.String");
		assertTrue(
			"Should be a class for java.lang.String",
			classes.size() == 1
				&& ((Type) classes.get(0)).signature().equals(
					"Ljava/lang/String;"));
	}
}
