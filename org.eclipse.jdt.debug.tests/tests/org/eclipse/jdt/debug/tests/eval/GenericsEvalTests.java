/*******************************************************************************
 * Copyright (c) Mar 1, 2013 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.eval;

import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Group of tests that evaluate operations involving generics
 *
 * @since 3.8
 */
public class GenericsEvalTests extends AbstractDebugTest {

	/**
	 * @param name
	 */
	public GenericsEvalTests(String name) {
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
			createLineBreakpoint(34, type);
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
			createLineBreakpoint(34, type);
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
			createLineBreakpoint(34, type);
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
			createLineBreakpoint(24, type);
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
			createLineBreakpoint(20, type);
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
			createLineBreakpoint(35, type);
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
			createLineBreakpoint(35, type);
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
			createLineBreakpoint(35, type);
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

	public void testEvalGenerics9() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.ConditionalsNearGenerics";
			createLineBreakpoint(35, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "char[] chars = name.toCharArray();";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	public void testEvalGeneric10() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.ConditionalsNearGenerics";
			createLineBreakpoint(36, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "tokenize(Arrays.asList(1,2,3), name)";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	public void testEvalGeneric11() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.ConditionalsNearGenerics";
			createLineBreakpoint(47, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "list.iterator()";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	public void testEvalGeneric12() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.ConditionalsNearGenerics";
			ILineBreakpoint bp = createLineBreakpoint(59, type);
			assertTrue("The breakpoint on line 59 must exist", bp.getMarker().exists());
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "this.input";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	public void testEvalGeneric13() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.ConditionalsNearGenerics";
			ILineBreakpoint bp = createLineBreakpoint(67, type);
			assertTrue("The breakpoint on line 67 must exist", bp.getMarker().exists());
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "this.input";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
	public void testLineCommentEvalGenerics1() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.bug329294WithGenerics";
			createLineBreakpoint(41, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "fInner1.innerBool";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
	public void testLineCommentEvalGenerics2() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.bug329294WithGenerics";
			createLineBreakpoint(61, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "inner";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
	public void testLineCommentEvalGenerics3() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.bug329294WithGenerics";
			createLineBreakpoint(65, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "fInner1.innerBool";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
	public void testLineCommentEvalGenerics4() throws Exception {
		IJavaThread thread = null;
		try {
			String type = "a.b.c.bug329294WithGenerics";
			createLineBreakpoint(69, type);
			thread = launchToBreakpoint(type);
			assertNotNull("The program did not suspend", thread);
			String snippet = "!fInner1.innerBool";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
	public void testInnerEnumType1() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "a.b.c.bug329294WithGenerics";
			createLineBreakpoint(14, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "fInner1.innerBool";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
	public void testInnerEnumType2() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "a.b.c.bug329294WithGenerics";
			createLineBreakpoint(14, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "!fInner1.innerBool";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
	public void testInnerEnumType3() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "a.b.c.bug329294WithGenerics";
			createLineBreakpoint(23, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "fInner1.innerBool";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
	public void testInnerEnumType4() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "a.b.c.bug329294WithGenerics";
			createLineBreakpoint(28, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "i2.fInner1.innerBool";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
	public void testInnerEnumType5() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "a.b.c.bug329294WithGenerics";
			createLineBreakpoint(33, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("The program did not suspend", thread);
			String snippet = "!ei2.fInner1.innerBool";
			doEval(thread, snippet);
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
}
