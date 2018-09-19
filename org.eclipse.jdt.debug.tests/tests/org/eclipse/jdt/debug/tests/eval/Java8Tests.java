/*******************************************************************************
 * Copyright (c) 2014, 2015 Jesper S. Møller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper S. Møller - initial API and implementation
 *     Jesper Steen Møller - bug 426903: [1.8] Cannot evaluate super call to default method
 *     Jesper S. Møller - bug 430839: [1.8] Cannot inspect static method of interface
 *******************************************************************************/

package org.eclipse.jdt.debug.tests.eval;

import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Group of tests that evaluate operations involving generics
 *
 * @since 3.8
 */
public class Java8Tests extends AbstractDebugTest {

	/**
	 * @param name
	 */
	public Java8Tests(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get18Project();
	}

	/**
	 * Evaluates a generified snippet with a simple single
	 * generic statement
	 *
	 * @throws Exception
	 */
	public void testEvalDefaultMethod() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "EvalTest18";
			createLineBreakpoint(22, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "strings.stream()";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * Evaluates a snippet in the context of interface method generic statement
	 *
	 * @throws Exception
	 */
	public void testEvalInterfaceMethod() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "EvalTestIntf18";
			IJavaLineBreakpoint bp = createLineBreakpoint(26, "", "EvalTestIntf18.java", "Intf18");
			assertNotNull("should have created breakpoint", bp);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "a + 2";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * Evaluates a snippet in the context of interface method generic statement
	 *
	 * @throws Exception
	 */
	public void testBugEvalIntfSuperDefault() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "EvalIntfSuperDefault";
			IJavaLineBreakpoint bp = createLineBreakpoint(29, "", "EvalIntfSuperDefault.java", "EvalIntfSuperDefault");
			assertNotNull("should have created breakpoint", bp);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "B.super.getOne()";
			String result = doEval(thread, snippet).getValueString();
			assertEquals("2", result);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * Evaluates a static method on an object generic statement
	 *
	 * @throws Exception
	 */
	public void testEvalStatictMethod() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "EvalTest18";
			createLineBreakpoint(22, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "java.util.stream.Stream.of(1,2,3).count()";
			IValue three = doEval(thread, snippet);
			assertEquals("3", three.getValueString());
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
}
