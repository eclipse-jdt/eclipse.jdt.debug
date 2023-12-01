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

	public AccessibleTest(String name) {
		super(name);
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
	 */
	public static void main(java.lang.String[] args) {
		new AccessibleTest().runSuite(args);
	}

	/**
	 * Test JDI isPackagePrivate().
	 */
	public void testJDIIsPackagePrivate() {
		assertFalse("1", fArrayType.isPackagePrivate());
		assertFalse("2", fClassType.isPackagePrivate());
		assertFalse("3", fInterfaceType.isPackagePrivate());
		assertFalse("4", fField.isPackagePrivate());
		assertFalse("5", fMethod.isPackagePrivate());
	}
	/**
	 * Test JDI isPrivate().
	 */
	public void testJDIIsPrivate() {
		assertFalse("1", fField.isPrivate());
		assertFalse("2", fMethod.isPrivate());

		// NB: isPrivate() is undefined for a type
	}
	/**
	 * Test JDI isProtected().
	 */
	public void testJDIIsProtected() {
		assertFalse("1", fField.isProtected());
		assertFalse("2", fMethod.isProtected());

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
