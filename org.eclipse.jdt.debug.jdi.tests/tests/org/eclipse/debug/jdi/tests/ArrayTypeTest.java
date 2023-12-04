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

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.StringReference;
import com.sun.jdi.Type;

/**
 * Tests for JDI com.sun.jdi.ArrayType
 * and JDWP Array command set.
 */
public class ArrayTypeTest extends AbstractJDITest {

	private ArrayType fType;
	/**
	 * Creates a new test.
	 */
	public ArrayTypeTest() {
		super();
	}

	public ArrayTypeTest(String name) {
		super(name);
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get array type
		fType = getArrayType();
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new ArrayTypeTest().runSuite(args);
	}

	/**
	 * Test JDI componentSignature().
	 */
	public void testJDIComponentSignature() {
		String signature = fType.componentSignature();
		assertEquals("1", "Ljava/lang/String;", signature);
	}
	/**
	 * Test JDI componentType().
	 */
	public void testJDIComponentType() {
		Type expected = fVM.classesByName("java.lang.String").get(0);
		Type type = null;
		try {
			type = fType.componentType();
		} catch (ClassNotLoadedException e) {
			fail("1");
		}
		assertEquals("2", expected, type);
	}
	/**
	 * Test JDI componentTypeName().
	 */
	public void testJDIComponentTypeName() {
		String typeName = fType.componentTypeName();
		assertEquals("1", "java.lang.String", typeName);
	}
	/**
	 * Test JDI newInstance(long).
	 */
	public void testJDINewInstance() {
		ArrayReference instance = fType.newInstance(1);
		assertEquals("1", instance.type(), fType);
		assertEquals("2", 1, instance.length());
		assertNull("3", instance.getValue(0));

		ArrayReference instance2 = fType.newInstance(5);
		try {
			instance2.setValue(3, fVM.mirrorOf("Yo"));
		} catch (InvalidTypeException exc) {
		} catch (ClassNotLoadedException exc) {
		}
		assertNull("4", instance2.getValue(2));
		assertEquals("5", "Yo", ((StringReference) (instance2.getValue(3))).value());
	}
}
