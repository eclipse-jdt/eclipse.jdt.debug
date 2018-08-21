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

import com.sun.jdi.ReferenceType;
import com.sun.jdi.TypeComponent;

/**
 * Tests for JDI com.sun.jdi.TypeComponent.
 */
public class TypeComponentTest extends AbstractJDITest {

	private TypeComponent fField, fMethod;
	/**
	 * Creates a new test.
	 */
	public TypeComponentTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get field fObject in org.eclipse.debug.jdi.tests.program.MainClass
		fField = getField();

		// Get method print(OutputStream)
		fMethod = getMethod();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new TypeComponentTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.TypeComponent";
	}
	/**
	 * Test JDI declaringType().
	 */
	public void testJDIDeclaringType() {
		ReferenceType mainClass = getMainClass();

		ReferenceType declaringType = fField.declaringType();
		assertEquals("1", mainClass, declaringType);

		declaringType = fMethod.declaringType();
		assertEquals("2", mainClass, declaringType);
	}
	/**
	 * Test JDI isFinal().
	 */
	public void testJDIIsFinal() {
		assertTrue("1", !fField.isFinal());
		assertTrue("2", !fMethod.isFinal());
	}
	/**
	 * Test JDI isStatic().
	 */
	public void testJDIIsStatic() {
		assertTrue("1", fField.isStatic());
		assertTrue("2", !fMethod.isStatic());
	}
	/**
	 * Test JDI isSynthetic().
	 */
	public void testJDIIsSynthetic() {
		if (!fVM.canGetSyntheticAttribute()) {
			return;
		}

		assertTrue("1", !fField.isSynthetic());
		assertTrue("2", !fMethod.isSynthetic());
	}
	/**
	 * Test JDI name().
	 */
	public void testJDIName() {
		assertEquals("1", "fObject", fField.name());
		assertEquals("2", "print", fMethod.name());
	}
	/**
	 * Test JDI signature().
	 */
	public void testJDISignature() {
		assertEquals(
			"1",
			"Lorg/eclipse/debug/jdi/tests/program/MainClass;",
			fField.signature());
		assertEquals("2", "(Ljava/io/OutputStream;)V", fMethod.signature());
	}
}
