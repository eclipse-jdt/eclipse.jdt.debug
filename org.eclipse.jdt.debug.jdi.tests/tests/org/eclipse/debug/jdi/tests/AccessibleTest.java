package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

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
	 */
	public static void main(java.lang.String[] args) {
		new AccessibleTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 */
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
