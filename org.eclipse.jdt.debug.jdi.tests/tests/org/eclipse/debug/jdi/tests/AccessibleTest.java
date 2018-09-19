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

import com.sun.jdi.Accessible;

/**
 * Tests for JDI com.sun.jdi.Accessible.
 */
public class AccessibleTest extends AbstractJDITest {

	private Accessible fArrayType, fClassType, fInterfaceType, fField, fMethod;
	/**
	 * Creates a new test.
	 */
	public AccessibleTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get the all kinds of accessible

		// ReferenceType
		fArrayType = getArrayType();
		fClassType = getMainClass();
		fInterfaceType = getInterfaceType();

		// TypeComponent
		fField = getField();
		fMethod = getMethod();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new AccessibleTest().runSuite(args);
	}

	/**
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.Accessible";
	}
	/**
	 * Test JDI isPackagePrivate().
	 */
	public void testJDIIsPackagePrivate() {
		assertTrue("1", !fArrayType.isPackagePrivate());
		assertTrue("2", !fClassType.isPackagePrivate());
		assertTrue("3", !fInterfaceType.isPackagePrivate());
		assertTrue("4", !fField.isPackagePrivate());
		assertTrue("5", !fMethod.isPackagePrivate());
	}
	/**
	 * Test JDI isPrivate().
	 */
	public void testJDIIsPrivate() {
		assertTrue("1", !fField.isPrivate());
		assertTrue("2", !fMethod.isPrivate());

		// NB: isPrivate() is undefined for a type
	}
	/**
	 * Test JDI isProtected().
	 */
	public void testJDIIsProtected() {
		assertTrue("1", !fField.isProtected());
		assertTrue("2", !fMethod.isProtected());

		// NB: isProtected() is undefined for a type
	}
	/**
	 * Test JDI isPublic().
	 */
	public void testJDIIsPublic() {
		assertTrue("1", fArrayType.isPublic());
		assertTrue("2", fClassType.isPublic());
		assertTrue("3", fInterfaceType.isPublic());
		assertTrue("4", fField.isPublic());
		assertTrue("5", fMethod.isPublic());
	}
}
