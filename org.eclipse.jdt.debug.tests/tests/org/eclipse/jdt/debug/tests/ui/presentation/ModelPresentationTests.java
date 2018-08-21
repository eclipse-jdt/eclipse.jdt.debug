/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.ui.presentation;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;

/**
 * Tests for some of the methods of the model presentation
 *
 * @see JDIModelPresentation
 */
public class ModelPresentationTests extends AbstractDebugTest {

	/**
	 * Constructor
	 */
	public ModelPresentationTests() {
		super("Model Presentation tests");
	}

	/**
	 * Tests that the type signature + value signatures do not cause problems when the values are "&lt;unknown&gt;" - this
	 * case arises when you manually suspend a VM and try to inspect / view object values
	 *
	 * @throws Exception
	 */
	public void testUnknownValueText() throws Exception {
		JDIModelPresentation pres = new JDIModelPresentation();
		try {
			TestIJavaType type = new TestIJavaType("foo", "<unknown>");
			TestIJavaValue value = new TestIJavaValue(type, "<unknown>", null, "<unknown>", null);
			String val = pres.getValueText(value);
			assertNotNull("the value should have been computed", val);
			assertEquals("The value text should be '<unknown>'", val, "<unknown>");
		}
		finally {
			pres.dispose();
		}
	}

	/**
	 * Tests passing all <code>null</code>s in for type information - should get an NPE
	 * from {@link JDIModelPresentation#getValueText(org.eclipse.jdt.debug.core.IJavaValue)}
	 *
	 * @throws Exception
	 */
	public void testAllNullValueText() throws Exception {
		JDIModelPresentation pres = new JDIModelPresentation();
		try {
			TestIJavaType type = new TestIJavaType(null, null);
			TestIJavaValue value = new TestIJavaValue(type, null, null, null, null);
			pres.getValueText(value);
			fail("did not get expected NullPointerException from passing in a null ReferenceType name");
		}
		catch(NullPointerException npe) {
			//catch expected NPE
		}
		finally {
			pres.dispose();
		}
	}

	/**
	 * Tests getting the value text for a simple String type
	 *
	 * @throws Exception
	 */
	public void testSimpleStringValueText() throws Exception {
		JDIModelPresentation pres = new JDIModelPresentation();
		try {
			String sig = Signature.createTypeSignature("java.lang.String", false);
			TestIJavaType type = new TestIJavaType("foobar", sig);
			TestIJavaValue value = new TestIJavaValue(type, sig, null, "org.test.MyClass", "test Java value");
			String val = pres.getValueText(value);
			assertNotNull("the value should have been computed", val);
			assertEquals("The value text should be '\"MyClass test Java value\"'", val, "MyClass test Java value");
		}
		finally {
			pres.dispose();
		}
	}

	/**
	 * Tests getting the value text for a simple String type
	 *
	 * @throws Exception
	 */
	public void testResolvedStringValueText() throws Exception {
		JDIModelPresentation pres = new JDIModelPresentation();
		try {
			String sig = Signature.createTypeSignature("java.lang.String", true);
			TestIJavaType type = new TestIJavaType("foobar", sig);
			TestIJavaValue value = new TestIJavaValue(type, sig, null, "org.test.MyClass", "test Java value");
			String val = pres.getValueText(value);
			assertNotNull("the value should have been computed", val);
			assertEquals("The value text should be '\"MyClass test Java value\"'", val, "MyClass test Java value");
		}
		finally {
			pres.dispose();
		}
	}

	/**
	 * Tests a simple array value text
	 *
	 * @throws Exception
	 */
	public void testSimpleArrayValueText() throws Exception {
		JDIModelPresentation pres = new JDIModelPresentation();
		try {
			String sig = Signature.createTypeSignature("org.test.MyClass", false);
			TestIJavaType type = new TestIJavaType("barfoo", sig);
			TestIJavaArrayValue value = new TestIJavaArrayValue(type, "org.test.MyArrayClass[]", null, "org.test.MyClass", "My Array", 3);
			value.setValues(new IJavaValue[] {
					new TestIJavaValue(type, "I", null, "org.test.MyArrayClass", "Array Value 1")
			});
			String val = pres.getValueText(value);
			assertNotNull("the value should have been computed", val);
			assertEquals("The value text should be 'MyClass My Array'", val, "MyClass My Array");
		}
		finally {
			pres.dispose();
		}
	}
}
