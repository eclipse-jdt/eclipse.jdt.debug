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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;

/**
 * Tests for JDI com.sun.jdi.Location.
 */
public class LocationTest extends AbstractJDITest {

	private Location fLocation;
	/**
	 * Creates a new test.
	 */
	public LocationTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Ensure we're in a good state
		fVM.resume();
		waitUntilReady();

		// Get the location of the stack frame running the method MainClass.run()
		fLocation = getLocation();

	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new LocationTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.Location";
	}
	/**
	 * Test JDI codeIndex().
	 */
	public void testJDICodeIndex() {
		fLocation.codeIndex();
	}
	/**
	 * Test JDI declaringType().
	 */
	public void testJDIDeclaringType() {
		ReferenceType expected = getMainClass();
		ReferenceType declaringType = fLocation.declaringType();
		assertEquals("1", expected.name(), declaringType.name());
		// Use name to work around a pb in Sun's VM
	}
	/**
	 * Test JDI equals() and hashCode().
	 */
	public void testJDIEquality() {
		assertTrue("1", fLocation.equals(fLocation));
		Location other = getFrame(0).location();
		assertTrue("2", !fLocation.equals(other));
		assertTrue("3", !fLocation.equals(new Object()));
		assertTrue("4", !fLocation.equals(null));
		assertTrue("5", fLocation.hashCode() != other.hashCode());
	}
	/**
	 * Test JDI lineNumber().
	 */
	public void testJDILineNumber() {
		assertEquals("1", 185, fLocation.lineNumber());
	}
	/**
	 * Test JDI method().
	 */
	public void testJDIMethod() {
		Method method = fLocation.method();
		assertEquals("1", "print", method.name());
	}
	/**
	 * Test JDI sourceName().
	 */
	public void testJDISourceName() {
		String sourceName = null;
		try {
			sourceName = fLocation.sourceName();
		} catch (AbsentInformationException e) {
			assertTrue("1", false);
		}
		assertEquals("2", "MainClass.java", sourceName);
	}
}
