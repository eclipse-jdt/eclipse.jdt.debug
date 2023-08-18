/*******************************************************************************
 * Copyright (c) 2014, 2020 Jesper S. Møller and others.
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
import org.eclipse.jdt.core.ICompilationUnit;
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

	/**
	 * Evaluates a snippet containing a lambda
	 */
	public void testEvalLambda() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "EvalTest18";
			IJavaLineBreakpoint bp = createLineBreakpoint(28, type);
			assertNotNull("should have created breakpoint", bp);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "l.stream().filter(i -> i > 2).count()";
			IValue result = doEval(thread, snippet);
			assertEquals("2", result.getValueString());
		} finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * Evaluates a snippet containing a lambda referencing a variable in a loop
	 */
	public void testEvalLambdaInLoop() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "EvalTest18";
			IJavaLineBreakpoint bp = createLineBreakpoint(31, type);
			assertNotNull("should have created breakpoint", bp);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "l.stream().filter(j -> j > i+1).count()";
			IValue result = doEval(thread, snippet);
			assertEquals("2", result.getValueString());
		} finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * Evaluates a snippet containing a method reference
	 */
	public void testEvalMethodReference() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "EvalTest18";
			IJavaLineBreakpoint bp = createLineBreakpoint(28, type);
			assertNotNull("should have created breakpoint", bp);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "l.stream().mapToInt(Integer::bitCount).sum()";
			IValue result = doEval(thread, snippet);
			assertEquals("5", result.getValueString());
		} finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * Evaluates a snippet containing a method reference
	 */
	public void testContextEvaluations() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "FunctionalCaptureTest18";
			ICompilationUnit cu = getType(type).getCompilationUnit();
			String[] lines = new String(cu.getBuffer().getCharacters()).split("\n");

			int i = 0;
			for (; i < lines.length; ++i) {
				if (lines[i].contains("/* CHECK EXPRESSIONS BELOW */")) break;
			}
			assertTrue("Missing source marker", i < lines.length);
			IJavaLineBreakpoint bp = createLineBreakpoint(i, type);
			assertNotNull("should have created breakpoint", bp);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);

			for (; i < lines.length; ++i) {
				String line = lines[i];

				if (line.contains("/* END OF TESTS */")) break;
				if (line.trim().startsWith("/") || line.trim().isEmpty()) continue; // Comment, just skip it
				if (line.contains("/* SKIP */")) continue;

				int lastSemicolon = line.lastIndexOf(';');
				assertTrue(lastSemicolon > 1);
				String snippet = line.substring(0,  lastSemicolon).trim();
				//System.out.println("*******************: " + snippet);
				IValue result = doEval(thread, snippet);
				assertNotNull(result);
				//System.out.println(">>>>>>>>>>>>>>>>>>>: " + result.getReferenceTypeName());
			}

		} finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	public void testEvaluate_GH275_StaticBinaryMethod_EvaluateSnippetWithImportedTypes() throws Exception {
		IJavaThread javaThread = null;
		try {
			IJavaLineBreakpoint bp = createLineBreakpoint(getType("com.debug.test.Observation"), 25);
			javaThread = launchToLineBreakpoint("GH275", bp);
			assertNotNull("The program did not suspend", javaThread);

			String snippet = "new Observation((Object) subject, (Consumer) action)";
			IValue value = doEval(javaThread, snippet);

			assertNotNull("value is null", value);
		} finally {
			removeAllBreakpoints();
			terminateAndRemove(javaThread);
		}
	}

	public void testEvaluate_GH275_InstanceBinaryMethod_EvaluateConstructWithImportedTypes() throws Exception {
		IJavaThread javaThread = null;
		try {
			IJavaLineBreakpoint bp = createLineBreakpoint(getType("com.debug.test.Observation"), 19);
			javaThread = launchToLineBreakpoint("GH275", bp);
			assertNotNull("The program did not suspend", javaThread);

			String snippet = "new Observation((Object) subject, (Consumer) action)";
			IValue value = doEval(javaThread, snippet);

			assertNotNull("value is null", value);
		} finally {
			removeAllBreakpoints();
			terminateAndRemove(javaThread);
		}
	}

	public void testEvaluate_GH275_InstanceBinaryMethod_EvaluateSnippetWithImportedTypes() throws Exception {
		IJavaThread javaThread = null;
		try {
			IJavaLineBreakpoint bp = createLineBreakpoint(getType("com.debug.test.Observation"), 19);
			javaThread = launchToLineBreakpoint("GH275", bp);
			assertNotNull("The program did not suspend", javaThread);

			String snippet = "((Subject)this.subject).getName()";
			IValue value = doEval(javaThread, snippet);

			assertNotNull("value is null", value);
			assertEquals("Return value doesn't match", "Name 1", value.getValueString());
		} finally {
			removeAllBreakpoints();
			terminateAndRemove(javaThread);
		}
	}
}
