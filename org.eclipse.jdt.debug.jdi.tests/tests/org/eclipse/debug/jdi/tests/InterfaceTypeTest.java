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

import java.util.Iterator;
import java.util.List;

import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Method;

/**
 * Tests for JDI com.sun.jdi.InterfaceType
 * and JDWP Interface command set.
 */
public class InterfaceTypeTest extends AbstractJDITest {

	private InterfaceType fType;
	/**
	 * Creates a new test.
	 */
	public InterfaceTypeTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get interface type "org.eclipse.debug.jdi.tests.program.Printable"
		fType = getInterfaceType();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new InterfaceTypeTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.InterfaceType";
	}
	/**
	 * Test JDI allFields().
	 */
	public void testJDIAllFields() {
		assertEquals("1", 1, fType.allFields().size());
	}
	/**
	 * Test JDI allMethods().
	 */
	public void testJDIAllMethods() {
		boolean found = false;
		Iterator<?> it = fType.allMethods().iterator();
		while (it.hasNext()) {
			Method mth = (Method) it.next();
			if (mth.name().equals("print")) {
				found = true;
			}
		}
		assertTrue("1", fType.allMethods().size() == 1);
		assertTrue("2", found);
	}
	/**
	 * Test JDI implementors().
	 */
	public void testJDIImplementors() {
		List<?> implementors = fType.implementors();
		assertEquals("1", 1, implementors.size());
		ClassType implementor = (ClassType) implementors.get(0);
		assertEquals("2", getMainClass(), implementor);
	}
	/**
	 * Test JDI subinterfaces().
	 */
	public void testJDISubinterfaces() {
		List<?> subinterfaces = fType.subinterfaces();
		assertEquals("1", 0, subinterfaces.size());
	}
	/**
	 * Test JDI superinterfaces().
	 */
	public void testJDISuperinterfaces() {
		List<?> superinterfaces = fType.superinterfaces();
		assertEquals("1", 1, superinterfaces.size());
		InterfaceType superinterface = (InterfaceType) superinterfaces.get(0);
		InterfaceType expected =
			(InterfaceType) fVM.classesByName("java.lang.Cloneable").get(0);
		assertEquals("2", expected, superinterface);
	}
}
