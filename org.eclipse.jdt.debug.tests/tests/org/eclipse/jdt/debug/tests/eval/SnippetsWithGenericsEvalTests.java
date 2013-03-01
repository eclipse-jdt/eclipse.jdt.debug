/*******************************************************************************
 * Copyright (c) Mar 1, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.eval;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Group of tests that evaluate operations involving generics
 * 
 * @since 3.8
 */
public class SnippetsWithGenericsEvalTests extends AbstractDebugTest {

	/**
	 * @param name
	 */
	public SnippetsWithGenericsEvalTests(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get15Project();
	}
	
	/**
	 * Evaluates a generified snippet with a simple single 
	 * generic statement
	 * 
	 * @throws Exception
	 */
	public void testEvalGenerics1() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.MethodBreakpoints";
			createLineBreakpoint(31, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "methodTypeParameter(1);";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
	
	/**
	 * Evaluates a generified snippet with a simple single 
	 * generic statement
	 * 
	 * @throws Exception
	 */
	public void testEvalGenerics2() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.MethodBreakpoints";
			createLineBreakpoint(31, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "new MethodBreakpoints<String>().typeParameter(\"test\")";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
	
	/**
	 * Evaluates a generified snippet with a simple single 
	 * generic statement
	 * 
	 * @throws Exception
	 */
	public void testEvalGenerics3() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.MethodBreakpoints";
			createLineBreakpoint(31, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "MethodBreakpoints.staticTypeParameter(new ArrayList<Long>())";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
	
	public void testEvalGenerics4() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.StepIntoSelectionWithGenerics";
			createLineBreakpoint(21, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "new java.util.ArrayList<String>().isEmpty()";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
	
	public void testEvalGenerics5() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.StepIntoSelectionWithGenerics";
			createLineBreakpoint(17, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "new java.util.ArrayList<String>().isEmpty()";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
	
	public void testEvalGenerics6() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.StepIntoSelectionWithGenerics";
			createLineBreakpoint(32, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "new StepIntoSelectionWithGenerics<String>().hello()";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
	
	public void testEvalGenerics7() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.StepIntoSelectionWithGenerics";
			createLineBreakpoint(32, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "new StepIntoSelectionWithGenerics<String>().new InnerClazz<Integer>().hello()";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
	
	public void testEvalGenerics8() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.StepIntoSelectionWithGenerics";
			createLineBreakpoint(32, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "new StepIntoSelectionWithGenerics<String>().new InnerClazz<Integer>().new InnerClazz2<Double>().hello()";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
}
