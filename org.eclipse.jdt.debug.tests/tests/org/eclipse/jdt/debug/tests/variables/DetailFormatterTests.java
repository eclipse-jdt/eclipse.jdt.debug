/*******************************************************************************
 * Copyright (c) Mar 12, 2016 IBM Corporation and others.
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

	class TestListener implements IValueDetailListener {
		IValue value;
		String result;

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

	/**
	 * @param name
	 */
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
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028
	 * @throws Exception
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
			long timeout = System.currentTimeMillis()+5000;
			while(fListener.value == null && System.currentTimeMillis() < timeout) {
				Thread.sleep(100);
			}
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertTrue("The map should be an instance of java.util.LinkedHashMap",
					Signature.getTypeErasure(fListener.value.getReferenceTypeName()).equals("java.util.LinkedHashMap"));
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
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028
	 * @throws Exception
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
			long timeout = System.currentTimeMillis()+5000;
			while(fListener.value == null && System.currentTimeMillis() < timeout) {
				Thread.sleep(100);
			}
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertTrue("The map should be an instance of java.util.LinkedHashMap",
					Signature.getTypeErasure(fListener.value.getReferenceTypeName()).equals("java.util.LinkedHashMap"));
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
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028
	 * @throws Exception
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
			long timeout = System.currentTimeMillis()+5000;
			while(fListener.value == null && System.currentTimeMillis() < timeout) {
				Thread.sleep(100);
			}
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertTrue("The map should be an instance of java.util.LinkedHashMap",
					Signature.getTypeErasure(fListener.value.getReferenceTypeName()).equals("java.util.LinkedHashMap"));
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
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028
	 * @throws Exception
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
			long timeout = System.currentTimeMillis()+5000;
			while(fListener.value == null && System.currentTimeMillis() < timeout) {
				Thread.sleep(100);
			}
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertTrue("The map should be an instance of java.util.LinkedHashMap",
					Signature.getTypeErasure(fListener.value.getReferenceTypeName()).equals("java.util.LinkedHashMap"));
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
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028
	 * @throws Exception
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
			long timeout = System.currentTimeMillis()+5000;
			while(fListener.value == null && System.currentTimeMillis() < timeout) {
				Thread.sleep(100);
			}
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertTrue("The map should be an instance of java.util.LinkedHashMap",
					Signature.getTypeErasure(fListener.value.getReferenceTypeName()).equals("java.util.LinkedHashMap"));
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

	/**
	 * Tests a detail formatter made from an infix expression
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028
	 * @throws Exception
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
			long timeout = System.currentTimeMillis()+5000;
			while(fListener.value == null && System.currentTimeMillis() < timeout) {
				Thread.sleep(100);
			}
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertTrue("The map should be an instance of java.util.LinkedHashMap",
					Signature.getTypeErasure(fListener.value.getReferenceTypeName()).equals("java.util.LinkedHashMap"));
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
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028
	 * @throws Exception
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
			long timeout = System.currentTimeMillis()+5000;
			while(fListener.value == null && System.currentTimeMillis() < timeout) {
				Thread.sleep(100);
			}
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertTrue("The map should be an instance of java.util.LinkedHashMap",
					Signature.getTypeErasure(fListener.value.getReferenceTypeName()).equals("java.util.LinkedHashMap"));
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
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=484686
	 * @throws Exception
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
			long timeout = System.currentTimeMillis() + 5000;
			while (fListener.value == null && System.currentTimeMillis() < timeout) {
				Thread.sleep(100);
			}
			assertNotNull("The IValue of the detailComputed callback cannot be null", fListener.value);
			assertNotNull("The computed value of the detail should not be null", fListener.result);
		}
		finally {
			jdfm.removeAssociatedDetailFormatter(formatter);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
