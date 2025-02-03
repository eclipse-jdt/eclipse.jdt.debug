/*******************************************************************************
 * Copyright (c) 2016, 2025 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.variables;

import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.DetailFormatter;
import org.eclipse.jdt.internal.debug.ui.JavaDetailFormattersManager;

/**
 * Tests detail formatters
 *
 * @since 3.8.100
 */
public class DetailFormatterTests extends AbstractDebugTest {

	static class TestListener implements IValueDetailListener {
		volatile IValue value;
		volatile String result;

		@Override
		public void detailComputed(IValue value, String result) {
			this.value = value;
			this.result = result;
		}

		void reset() {
			value = null;
			result = null;
		}
	}

	TestListener fListener = new TestListener();

	public DetailFormatterTests(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get15Project();
	}

	@Override
	protected void tearDown() throws Exception {
		fListener.reset();
		super.tearDown();
	}

	/**
	 * Tests a detail formatter made from a large compound expression
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028"
	 */
	public void testCompoundDetails1() throws Exception {
		IJavaThread thread = null;
		DetailFormatter formatter = null;
		JavaDetailFormattersManager jdfm = JavaDetailFormattersManager.getDefault();
		try {
			String typename = "a.b.c.bug403028";
			createLineBreakpoint(10, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "StringBuilder buf = new StringBuilder();\n"
					+ "buf.append(\"{\");\n"
					+ "Iterator i = this.entrySet().iterator();\n"
					+ "boolean hasNext = i.hasNext();\n"
					+ "while (hasNext) {\n"
					+ "    Entry e = (Entry) (i.next());\n"
					+ "    Object key = e.getKey();\n"
					+ "    Object value = e.getValue();\n"
					+ "    buf.append((key == this ?  \"(this Map)\" : key) + \"=\" + \n"
					+ "            (value == this ? \"(this Map)\": value));\n"
					+ "    hasNext = i.hasNext();\n"
					+ "    if (hasNext)\n"
					+ "        buf.append(\"\n,\");\n"
					+ "}\n"
					+ "buf.append(\"}\");\n"
					+ "return buf.toString();";
			formatter = new DetailFormatter("java.util.HashMap", snippet, true);
			jdfm.setAssociatedDetailFormatter(formatter);
			IJavaVariable var = thread.findVariable("map");
			assertNotNull("the variable 'map' must exist in the frame", var);
			jdfm.computeValueDetail((IJavaValue) var.getValue(), thread, fListener);
			waitForListenerValue();
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertEquals("The map should be an instance of java.util.LinkedHashMap", "java.util.LinkedHashMap", Signature.getTypeErasure(fListener.value.getReferenceTypeName()));
			assertNotNull("The computed value of the detail should not be null", fListener.result);
		}
		finally {
			jdfm.removeAssociatedDetailFormatter(formatter);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a detail formatter made from a small compound expression
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028"
	 */
	public void testCompoundDetails2() throws Exception {
		IJavaThread thread = null;
		DetailFormatter formatter = null;
		JavaDetailFormattersManager jdfm = JavaDetailFormattersManager.getDefault();
		try {
			String typename = "a.b.c.bug403028";
			createLineBreakpoint(10, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "StringBuilder buf = new StringBuilder();\n"
					+ "buf.append(this);\n"
					+ "return buf.toString();";
			formatter = new DetailFormatter("java.util.HashMap", snippet, true);
			jdfm.setAssociatedDetailFormatter(formatter);
			IJavaVariable var = thread.findVariable("map");
			assertNotNull("the variable 'map' must exist in the frame", var);
			jdfm.computeValueDetail((IJavaValue) var.getValue(), thread, fListener);
			waitForListenerValue();
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertEquals("The map should be an instance of java.util.LinkedHashMap", "java.util.LinkedHashMap", Signature.getTypeErasure(fListener.value.getReferenceTypeName()));
			assertNotNull("The computed value of the detail should not be null", fListener.result);
		}
		finally {
			jdfm.removeAssociatedDetailFormatter(formatter);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a detail formatter made from a small compound expression
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028"
	 */
	public void testSimpleDetails1() throws Exception {
		IJavaThread thread = null;
		DetailFormatter formatter = null;
		JavaDetailFormattersManager jdfm = JavaDetailFormattersManager.getDefault();
		try {
			String typename = "a.b.c.bug403028";
			createLineBreakpoint(10, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "return toString();";
			formatter = new DetailFormatter("java.util.HashMap", snippet, true);
			jdfm.setAssociatedDetailFormatter(formatter);
			IJavaVariable var = thread.findVariable("map");
			assertNotNull("the variable 'map' must exist in the frame", var);
			jdfm.computeValueDetail((IJavaValue) var.getValue(), thread, fListener);
			waitForListenerValue();
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertEquals("The map should be an instance of java.util.LinkedHashMap", "java.util.LinkedHashMap", Signature.getTypeErasure(fListener.value.getReferenceTypeName()));
			assertNotNull("The computed value of the detail should not be null", fListener.result);
		}
		finally {
			jdfm.removeAssociatedDetailFormatter(formatter);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a detail formatter made from a small compound expression
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028"
	 */
	public void testSimpleDetails2() throws Exception {
		IJavaThread thread = null;
		DetailFormatter formatter = null;
		JavaDetailFormattersManager jdfm = JavaDetailFormattersManager.getDefault();
		try {
			String typename = "a.b.c.bug403028";
			createLineBreakpoint(10, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "return \"map test detail formatter [\" + toString() + \"]\";";
			formatter = new DetailFormatter("java.util.HashMap", snippet, true);
			jdfm.setAssociatedDetailFormatter(formatter);
			IJavaVariable var = thread.findVariable("map");
			assertNotNull("the variable 'map' must exist in the frame", var);
			jdfm.computeValueDetail((IJavaValue) var.getValue(), thread, fListener);
			waitForListenerValue();
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertEquals("The map should be an instance of java.util.LinkedHashMap", "java.util.LinkedHashMap", Signature.getTypeErasure(fListener.value.getReferenceTypeName()));
			assertNotNull("The computed value of the detail should not be null", fListener.result);
		}
		finally {
			jdfm.removeAssociatedDetailFormatter(formatter);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a detail formatter made from an infix expression
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028"
	 */
	public void testInfixDetails1() throws Exception {
		IJavaThread thread = null;
		DetailFormatter formatter = null;
		JavaDetailFormattersManager jdfm = JavaDetailFormattersManager.getDefault();
		try {
			String typename = "a.b.c.bug403028";
			createLineBreakpoint(10, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "return (true && true || !(false&&true) || !(true==true||true!=true&&true));";
			formatter = new DetailFormatter("java.util.HashMap", snippet, true);
			jdfm.setAssociatedDetailFormatter(formatter);
			IJavaVariable var = thread.findVariable("map");
			assertNotNull("the variable 'map' must exist in the frame", var);
			jdfm.computeValueDetail((IJavaValue) var.getValue(), thread, fListener);
			waitForListenerValue();
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertEquals("The map should be an instance of java.util.LinkedHashMap", "java.util.LinkedHashMap", Signature.getTypeErasure(fListener.value.getReferenceTypeName()));
			assertNotNull("The computed value of the detail should not be null", fListener.result);
			assertTrue("The returned value from (true && true || !(false&&true) || !(true==true||true!=true&&true)) should be true",
					Boolean.parseBoolean(fListener.result));
		}
		finally {
			jdfm.removeAssociatedDetailFormatter(formatter);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	private void waitForListenerValue() throws InterruptedException {
		long timeoutNanos = System.nanoTime() + 5000 * 1_000_000L;
		while (fListener.value == null && System.nanoTime() < timeoutNanos) {
			Thread.sleep(1);
		}
	}

	/**
	 * Tests a detail formatter made from an infix expression
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028"
	 */
	public void testInfixDetails2() throws Exception {
		IJavaThread thread = null;
		DetailFormatter formatter = null;
		JavaDetailFormattersManager jdfm = JavaDetailFormattersManager.getDefault();
		try {
			String typename = "a.b.c.bug403028";
			createLineBreakpoint(10, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "return !true;";
			formatter = new DetailFormatter("java.util.HashMap", snippet, true);
			jdfm.setAssociatedDetailFormatter(formatter);
			IJavaVariable var = thread.findVariable("map");
			assertNotNull("the variable 'map' must exist in the frame", var);
			jdfm.computeValueDetail((IJavaValue) var.getValue(), thread, fListener);
			waitForListenerValue();
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertEquals("The map should be an instance of java.util.LinkedHashMap", "java.util.LinkedHashMap", Signature.getTypeErasure(fListener.value.getReferenceTypeName()));
			assertNotNull("The computed value of the detail should not be null", fListener.result);
			assertFalse("The returned value from !true should be false", Boolean.parseBoolean(fListener.result));
		}
		finally {
			jdfm.removeAssociatedDetailFormatter(formatter);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a detail formatter made from an infix expression
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028"
	 */
	public void testInfixDetails3() throws Exception {
		IJavaThread thread = null;
		DetailFormatter formatter = null;
		JavaDetailFormattersManager jdfm = JavaDetailFormattersManager.getDefault();
		try {
			String typename = "a.b.c.bug403028";
			createLineBreakpoint(10, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "return !(true==true||true!=true&&true);";
			formatter = new DetailFormatter("java.util.HashMap", snippet, true);
			jdfm.setAssociatedDetailFormatter(formatter);
			IJavaVariable var = thread.findVariable("map");
			assertNotNull("the variable 'map' must exist in the frame", var);
			jdfm.computeValueDetail((IJavaValue) var.getValue(), thread, fListener);
			waitForListenerValue();
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertEquals("The map should be an instance of java.util.LinkedHashMap", "java.util.LinkedHashMap", Signature.getTypeErasure(fListener.value.getReferenceTypeName()));
			assertNotNull("The computed value of the detail should not be null", fListener.result);
			assertFalse("The returned value from !(true==true||true!=true&&true) should be false", Boolean.parseBoolean(fListener.result));
		}
		finally {
			jdfm.removeAssociatedDetailFormatter(formatter);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a detail formatter made from an collection with no type arguments
	 *
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=484686"
	 */
	public void testHoverWithNoTypeArguments() throws Exception {
		IJavaThread thread = null;
		DetailFormatter formatter = null;
		JavaDetailFormattersManager jdfm = JavaDetailFormattersManager.getDefault();
		try {
			String typename = "a.b.c.bug484686";
			createLineBreakpoint(8, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "StringBuilder sb = new StringBuilder();\n" + "for (Object obj : this) { \n" + "sb.append(obj).append(\"\\n\"); }\n"
					+ "return sb.toString();";
			formatter = new DetailFormatter("java.util.Collection", snippet, true);
			jdfm.setAssociatedDetailFormatter(formatter);
			IJavaVariable var = thread.findVariable("coll");
			assertNotNull("the variable 'coll' must exist in the frame", var);
			jdfm.computeValueDetail((IJavaValue) var.getValue(), thread, fListener);
			waitForListenerValue();
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertNotNull("The computed value of the detail should not be null", fListener.result);
		}
		finally {
			jdfm.removeAssociatedDetailFormatter(formatter);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testFormatterForPrimitivesInt() throws Exception {
		IJavaThread thread = null;
		DetailFormatter formatter = null;
		JavaDetailFormattersManager jdfm = JavaDetailFormattersManager.getDefault();
		try {
			String typename = "a.b.c.PrimitivesTest";
			createLineBreakpoint(26, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "this + 12";
			formatter = new DetailFormatter("int", snippet, true);
			jdfm.setAssociatedDetailFormatter(formatter);
			IJavaVariable var = thread.findVariable("x");
			assertNotNull("the variable 'x' must exist in the frame", var);
			jdfm.computeValueDetail((IJavaValue) var.getValue(), thread, fListener);
			waitForListenerValue();
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertEquals("24", fListener.result.toString());

		} finally {
			jdfm.removeAssociatedDetailFormatter(formatter);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testFormatterForPrimitivesfloat() throws Exception {
		IJavaThread thread = null;
		DetailFormatter formatter = null;
		JavaDetailFormattersManager jdfm = JavaDetailFormattersManager.getDefault();
		try {
			String typename = "a.b.c.PrimitivesTest";
			createLineBreakpoint(26, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "this + 10";
			formatter = new DetailFormatter("float", snippet, true);
			jdfm.setAssociatedDetailFormatter(formatter);
			IJavaVariable var = thread.findVariable("f");
			assertNotNull("the variable 'f' must exist in the frame", var);
			jdfm.computeValueDetail((IJavaValue) var.getValue(), thread, fListener);
			waitForListenerValue();
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertEquals("20.0", fListener.result.toString());
		} finally {
			jdfm.removeAssociatedDetailFormatter(formatter);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testFormatterForPrimitivesIntArrays() throws Exception {
		IJavaThread thread = null;
		DetailFormatter formatter = null;
		JavaDetailFormattersManager jdfm = JavaDetailFormattersManager.getDefault();
		try {
			String typename = "a.b.c.PrimitivesTest";
			createLineBreakpoint(26, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "this[1]";
			formatter = new DetailFormatter("int[]", snippet, true);
			jdfm.setAssociatedDetailFormatter(formatter);
			IJavaVariable var = thread.findVariable("arInt");
			assertNotNull("the variable 'arInt' must exist in the frame", var);
			jdfm.computeValueDetail((IJavaValue) var.getValue(), thread, fListener);
			waitForListenerValue();
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertEquals("2", fListener.result.toString());
		} finally {
			jdfm.removeAssociatedDetailFormatter(formatter);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testFormatterForPrimitivesIntArraysMulti() throws Exception {
		IJavaThread thread = null;
		DetailFormatter formatter = null;
		JavaDetailFormattersManager jdfm = JavaDetailFormattersManager.getDefault();
		try {
			String typename = "a.b.c.PrimitivesTest";
			createLineBreakpoint(26, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "this[0][0]";
			formatter = new DetailFormatter("int[][]", snippet, true);
			jdfm.setAssociatedDetailFormatter(formatter);
			IJavaVariable var = thread.findVariable("mul");
			assertNotNull("the variable 'mul' must exist in the frame", var);
			jdfm.computeValueDetail((IJavaValue) var.getValue(), thread, fListener);
			waitForListenerValue();
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertEquals("1", fListener.result.toString());
		} finally {
			jdfm.removeAssociatedDetailFormatter(formatter);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testFormatterForPrimitivesCharArray() throws Exception {
		IJavaThread thread = null;
		DetailFormatter formatter = null;
		JavaDetailFormattersManager jdfm = JavaDetailFormattersManager.getDefault();
		try {
			String typename = "a.b.c.PrimitivesTest";
			createLineBreakpoint(26, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "new String(this)";
			formatter = new DetailFormatter("char[]", snippet, true);
			jdfm.setAssociatedDetailFormatter(formatter);
			IJavaVariable var = thread.findVariable("aCh");
			assertNotNull("the variable 'aCh' must exist in the frame", var);
			jdfm.computeValueDetail((IJavaValue) var.getValue(), thread, fListener);
			waitForListenerValue();
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertEquals("ab", fListener.result.toString());
		} finally {
			jdfm.removeAssociatedDetailFormatter(formatter);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testFormatterForExceptionObjectsWithoutFormatter() throws Exception {
		IJavaThread thread = null;
		JavaDetailFormattersManager jdfm = JavaDetailFormattersManager.getDefault();
		try {
			String typename = "a.b.c.ExceptionDefaultTest";
			createLineBreakpoint(19, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			IJavaVariable var = thread.findVariable("e");
			assertNotNull("the variable 'e' must exist in the frame", var);
			jdfm.computeValueDetail((IJavaValue) var.getValue(), thread, fListener);
			waitForListenerValue();
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			String value = fListener.result.toString().trim();
			value = value.split("\n")[1].trim();
			assertEquals("at a.b.c.ExceptionDefaultTest.main(ExceptionDefaultTest.java:18)", value);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testFormatterForExceptionObjectsWithFormatter() throws Exception {
		IJavaThread thread = null;
		DetailFormatter formatter = null;
		JavaDetailFormattersManager jdfm = JavaDetailFormattersManager.getDefault();
		try {
			String typename = "a.b.c.ExceptionDefaultTest";
			createLineBreakpoint(19, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "this.toString()";
			formatter = new DetailFormatter("java.lang.Exception", snippet, true);
			jdfm.setAssociatedDetailFormatter(formatter);
			IJavaVariable var2 = thread.findVariable("e");
			assertNotNull("the variable 'e' must exist in the frame", var2);
			jdfm.computeValueDetail((IJavaValue) var2.getValue(), thread, fListener);
			waitForListenerValue();
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertEquals("java.lang.Exception", fListener.result.toString());
		} finally {
			jdfm.removeAssociatedDetailFormatter(formatter);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

}
