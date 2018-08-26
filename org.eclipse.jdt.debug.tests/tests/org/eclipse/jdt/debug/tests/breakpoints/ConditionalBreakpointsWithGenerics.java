/*******************************************************************************
 * Copyright (c) 2012, 2013 Jesper Steen Moller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper Steen Moller - initial API and implementation, adapted from
 *     Stefan Mandels contribution in bug 341232, and existing debug tests
 *     IBM Corporation - addition test for bug 341232
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests conditional breakpoints.
 */
public class ConditionalBreakpointsWithGenerics extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public ConditionalBreakpointsWithGenerics(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.tests.AbstractDebugTest#getProjectContext()
	 */
	@Override
	protected IJavaProject getProjectContext() {
		return get15Project();
	}

	/**
	 * Tests a breakpoint with a simple condition does not cause errors
	 * multiple use of the same generic type 'T'
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=341232
	 * @throws Exception
	 */
	public void testDuplicateGenericTypes() throws Exception {
		String typeName = "a.b.c.ConditionalsNearGenerics";
		String innerTypeName = "a.b.c.ConditionalsNearGenerics.ItemIterator";
		createConditionalLineBreakpoint(36, typeName, "false", true);
		createConditionalLineBreakpoint(47, typeName, "false", true);
		ILineBreakpoint bp = createConditionalLineBreakpoint(59, innerTypeName, "true", true);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp); // If compiled correctly, this will jump over bp1-bp3 !!
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a breakpoint with a simple condition does not cause errors
	 * with many inner types with generics
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=341232
	 * @throws Exception
	 */
	public void testNestedTypes1() throws Exception {
		String type = "a.b.c.StepIntoSelectionWithGenerics";
		IJavaThread thread = null;
		try {
			createConditionalLineBreakpoint(35, type, "true", true);
			thread = launchToBreakpoint(type);
			assertNotNull("Breakpoint not hit within timeout period", thread);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a breakpoint with a simple condition does not cause errors
	 * with many inner types with generics
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=341232
	 * @throws Exception
	 */
	public void testNestedTypes2() throws Exception {
		String type = "a.b.c.StepIntoSelectionWithGenerics";
		IJavaThread thread = null;
		try {
			createConditionalLineBreakpoint(24, type, "true", true);
			thread = launchToBreakpoint(type);
			assertNotNull("Breakpoint not hit within timeout period", thread);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a breakpoint with a simple condition does not cause errors
	 * with many inner types with generics
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=341232
	 * @throws Exception
	 */
	public void testNestedTypes3() throws Exception {
		String type = "a.b.c.StepIntoSelectionWithGenerics";
		IJavaThread thread = null;
		try {
			createConditionalLineBreakpoint(20, type, "true", true);
			thread = launchToBreakpoint(type);
			assertNotNull("Breakpoint not hit within timeout period", thread);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a breakpoint with a simple condition does not cause errors
	 * with multiple generic types 'T', 'E', 'K'
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=341232
	 * @throws Exception
	 */
	public void testMultipleGenericTypes1() throws Exception {
		String type = "a.b.c.MethodBreakpoints";
		IJavaThread thread = null;
		try {
			createConditionalLineBreakpoint(29, type, "true", true);
			thread = launchToBreakpoint(type);
			assertNotNull("Breakpoint not hit within timeout period", thread);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a breakpoint with a simple condition does not cause errors
	 * with multiple generic types 'T', 'E', 'K'
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=341232
	 * @throws Exception
	 */
	public void testMultipleGenericTypes2() throws Exception {
		String type = "a.b.c.MethodBreakpoints";
		IJavaThread thread = null;
		try {
			createConditionalLineBreakpoint(34, type, "true", true);
			thread = launchToBreakpoint(type);
			assertNotNull("Breakpoint not hit within timeout period", thread);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a breakpoint with a condition that includes generics
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=341232
	 * @throws Exception
	 */
	public void testBreakpointWithGenericsCondition1() throws Exception {
		String type = "a.b.c.MethodBreakpoints";
		IJavaThread thread = null;
		try {
			String condition = "MethodBreakpoints<Integer> breakpoints = new MethodBreakpoints<Integer>();\n"
						+ "breakpoints.typeParameter(10);\n"
						+ "return true;";
			createConditionalLineBreakpoint(34, type, condition, true);
			thread = launchToBreakpoint(type);
			assertNotNull("Breakpoint not hit within timeout period", thread);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a breakpoint with a generified condition in a type that includes
	 * duplicate generic declarations
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=341232
	 * @throws Exception
	 */
	public void testBreakpointWithGenericCondition2() throws Exception {
		String type = "a.b.c.ConditionalsNearGenerics";
		IJavaThread thread = null;
		try {
			String condition = "Iterator<Integer> i = tokenize(Arrays.asList(1, 2, 3), \"condition\");\n"+
								"return i.hasNext();";
			createConditionalLineBreakpoint(36, type, condition, true);
			thread = launchToBreakpoint(type);
			assertNotNull("Breakpoint not hit within timeout period", thread);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a breakpoint with a condition that includes generics from nested classes
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=341232
	 * @throws Exception
	 */
	public void testBreakpointWithGenericCondition3() throws Exception {
		String type = "a.b.c.StepIntoSelectionWithGenerics";
		IJavaThread thread = null;
		try {
			String condition = "StepIntoSelectionWithGenerics<String> ssswg = new StepIntoSelectionWithGenerics<String>();\n"
					+ "InnerCLazz<Integer> ic = new InnerClazz<Integer>();\n"
					+ "InnerClazz2<Double> ic2 = new InnerClazz2<Double>();\n"
					+ "ic2.hello();\n"
					+ "return true;";
			createConditionalLineBreakpoint(35, type, condition, true);
			thread = launchToBreakpoint(type);
			assertNotNull("Breakpoint not hit within timeout period", thread);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a breakpoint with a condition that includes generics from nested classes
	 *
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028
	 * @throws Exception
	 */
	public void testBreakpointWithGenericCondition4() throws Exception {
		String type = "a.b.c.bug403028";
		IJavaThread thread = null;
		try {
			String condition = "StringBuilder buf = new StringBuilder();"
					+ "buf.append(\"{\");"
					+ "Iterator i = this.entrySet().iterator();"
					+ "boolean hasNext = i.hasNext();"
					+ "while (hasNext) {"
					+ "    Entry e = (Entry) (i.next());"
					+ "    Object key = e.getKey();"
					+ "    Object value = e.getValue();"
					+ "    buf.append((key == this ?  \"(this Map)\" : key) + \"=\" + "
					+ "            (value == this ? \"(this Map)\": value));"
					+ "    hasNext = i.hasNext();"
					+ "    if (hasNext)"
					+ "        buf.append(\"\n,\");"
					+ "}"
					+ "buf.append(\"}\");"
					+ "buf.toString();"
					+ "return false;";
			createConditionalLineBreakpoint(10, type, condition, false);
			thread = launchToBreakpoint(type);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
}
