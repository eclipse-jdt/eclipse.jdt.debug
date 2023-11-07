/*******************************************************************************
 * Copyright (c) 2011, 2020 IBM Corporation and others.
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.swt.graphics.Color;

/**
 * Tests for some of the methods of the model presentation
 *
 * @see JDIModelPresentation
 */
public class ModelPresentationTests extends AbstractDebugTest {

	private final Map<String, Color> colors = new HashMap<>();
	/**
	 * Constructor
	 */
	public ModelPresentationTests() {
		super("Model Presentation tests");
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		colors.clear();
		colors.put("org.eclipse.jdt.debug.ui.LabeledObject", new Color(255, 0, 0));
	}

	private JDIModelPresentation mock() {
		return new JDIModelPresentation() {
			@Override
			protected Color getColorFromRegistry(String symbolicName) {
				return colors.get(symbolicName);
			}
		};
	}

	/**
	 * Tests that the type signature + value signatures do not cause problems when the values are "&lt;unknown&gt;" - this
	 * case arises when you manually suspend a VM and try to inspect / view object values
	 */
	public void testUnknownValueText() throws Exception {
		JDIModelPresentation pres = mock();
		try {
			TestIJavaType type = new TestIJavaType("foo", "<unknown>");
			TestIJavaValue value = new TestIJavaValue(type, "<unknown>", null, "<unknown>", null);
			String val = pres.getValueText(value);
			assertNotNull("the value should have been computed", val);
			assertEquals("The value text should be '<unknown>'", "<unknown>", val);
		}
		finally {
			pres.dispose();
		}
	}

	/**
	 * Tests passing all <code>null</code>s in for type information - should get an NPE
	 * from {@link JDIModelPresentation#getValueText(org.eclipse.jdt.debug.core.IJavaValue)}
	 */
	public void testAllNullValueText() throws Exception {
		JDIModelPresentation pres = mock();
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
	 */
	public void testSimpleStringValueText() throws Exception {
		JDIModelPresentation pres = mock();
		try {
			var value = createJavaObject();
			String val = pres.getValueText(value);
			assertNotNull("the value should have been computed", val);
			assertEquals("The value text should be '\"MyClass test Java value\"'", "MyClass test Java value", val);
		}
		finally {
			pres.dispose();
		}
	}

	/**
	 * Tests getting the value text for a simple String type
	 */
	public void testResolvedStringValueText() throws Exception {
		JDIModelPresentation pres = mock();
		try {
			var value = createJavaObject();
			String val = pres.getValueText(value);
			assertNotNull("the value should have been computed", val);
			assertEquals("The value text should be '\"MyClass test Java value\"'", "MyClass test Java value", val);
		}
		finally {
			pres.dispose();
		}
	}

	/**
	 * Tests getting the value text for a simple String type with a label
	 */
	public void testStringValueTextWithLabel() throws Exception {
		var pres = mock();
		try {
			var value = createJavaObject();
			value.setLabel("myLabel");
			var valTxt = pres.getValueText(value);
			assertNotNull("the value should have been computed", valTxt);
			assertEquals("The value text should be '\"(myLabel) MyClass test Java value\"'", "(myLabel) MyClass test Java value", valTxt);
		} finally {
			pres.dispose();
		}
	}

	/**
	 * Tests getting the value text for a simple String type
	 */
	public void testStringVariableWithValueText() throws Exception {
		JDIModelPresentation pres = mock();
		try {
			var value = createJavaObject();
			var variable = new TestIJavaVariable("myVariable", value);
			String val = pres.getText(variable);
			assertNotNull("the value should have been computed", val);
			assertEquals("The value text should be '\"myVariable= MyClass test Java value\"'", "myVariable= MyClass test Java value", val);
			var foreground = pres.getForeground(variable);
			assertNull("the foreground color should have been null", foreground);
		} finally {
			pres.dispose();
		}
	}

	/**
	 * Tests getting the value text for a simple String type with a label
	 */
	public void testStringVariableWithValueTextWithLabel() throws Exception {
		var pres = mock();
		try {
			var value = createJavaObject();
			value.setLabel("myLabel");
			var variable = new TestIJavaVariable("myVariable", value);
			var valTxt = pres.getText(variable);
			assertNotNull("the value should have been computed", valTxt);
			assertEquals("The value text should be '\"myVariable= (myLabel) MyClass test Java value\"'", "myVariable= (myLabel) MyClass test Java value", valTxt);
			var foreground = pres.getForeground(variable);
			assertNotNull("the foreground should have been computed", foreground);
		} finally {
			pres.dispose();
		}
	}

	/**
	 * Tests for handling IWatchExpression
	 */
	public void testWatchExpression() throws Exception {
		JDIModelPresentation pres = mock();
		try {
			var value = createJavaObject();
			var variable = new TestIWatchExpression("myVariable", value);
			String val = pres.getText(variable);
			assertNotNull("the value should have been computed", val);
			assertEquals("The value text should be '\"\\\"myVariable\\\"= MyClass test Java value\"'", "\"myVariable\"= MyClass test Java value", val);
			var foreground = pres.getForeground(variable);
			assertNull("the foreground color should have been null", foreground);
		} finally {
			pres.dispose();
		}
	}

	/**
	 * Tests for handling IWatchExpression with a label
	 */
	public void testWatchExpressionWithLabel() throws Exception {
		var pres = mock();
		try {
			var value = createJavaObject();
			value.setLabel("myLabel");
			var variable = new TestIWatchExpression("myVariable", value);
			var valTxt = pres.getText(variable);
			assertNotNull("the value should have been computed", valTxt);
			assertEquals("The value text should be '\"\\\"myVariable\\\"= (myLabel) MyClass test Java value\"'", "\"myVariable\"= (myLabel) MyClass test Java value", valTxt);
			var foreground = pres.getForeground(variable);
			assertNotNull("the foreground should have been computed", foreground);
		} finally {
			pres.dispose();
		}
	}

	/**
	 * Tests for handling JavaInspectExpression
	 */
	public void testJavaInspectExpression() throws Exception {
		JDIModelPresentation pres = mock();
		try {
			var value = createJavaObject();
			var variable = new JavaInspectExpression("myVariable", value);
			String val = pres.getText(variable);
			assertNotNull("the value should have been computed", val);
			assertEquals("The value text should be '\"\\\"myVariable\\\"= MyClass test Java value\"'", "\"myVariable\"= MyClass test Java value", val);
			var foreground = pres.getForeground(variable);
			assertNull("the foreground color should have been null", foreground);
		} finally {
			pres.dispose();
		}
	}

	/**
	 * Tests for handling JavaInspectExpression with a label
	 */
	public void testJavaInspectExpressionWithLabel() throws Exception {
		var pres = mock();
		try {
			var value = createJavaObject();
			value.setLabel("myLabel");
			var variable = new JavaInspectExpression("myVariable", value);
			var valTxt = pres.getText(variable);
			assertNotNull("the value should have been computed", valTxt);
			assertEquals("The value text should be '\"\\\"myVariable\\\"= (myLabel) MyClass test Java value\"'", "\"myVariable\"= (myLabel) MyClass test Java value", valTxt);
			var foreground = pres.getForeground(variable);
			assertNotNull("the foreground should have been computed", foreground);
		} finally {
			pres.dispose();
		}
	}

	private TestIJavaObjectValue createJavaObject() {
		var sig = Signature.createTypeSignature("java.lang.String", true);
		var type = new TestIJavaType("foobar", sig);
		var value = new TestIJavaObjectValue(type, sig, null, "org.test.MyClass", "test Java value");
		return value;
	}

	/**
	 * Tests a simple array value text
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
			assertEquals("The value text should be 'MyClass My Array'", "MyClass My Array", val);
		}
		finally {
			pres.dispose();
		}
	}

	/**
	 * Tests displayVariableTypeNames option
	 */
	public void testShowTypeTest() throws Exception {
		String typeName = "ModelPresentationTests";
		IJavaLineBreakpoint bp = createLineBreakpoint(19, typeName);
		JDIModelPresentation pres = mock();

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			IJavaVariable stringArrayVariable = findVariable(frame, "stringArray");
			long id = ((IJavaObject) stringArrayVariable.getValue()).getUniqueId();

			assertEquals("stringArray= String[0]  (id=" + id + ")", pres.getText(stringArrayVariable));

			pres.setAttribute(IDebugModelPresentation.DISPLAY_VARIABLE_TYPE_NAMES, Boolean.TRUE);

			assertEquals("String[] stringArray= String[0]  (id=" + id + ")", pres.getText(stringArrayVariable));

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			pres.dispose();
		}
	}
}
