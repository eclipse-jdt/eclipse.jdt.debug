/*******************************************************************************
 * Copyright (c) 2021 Gayan Perera and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gayan Perera - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.eval;

import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class ExpressionEvalTest extends AbstractDebugTest {
	private IJavaThread javaThread;

	public ExpressionEvalTest(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get14Project();
	}

	public void test547462_BooleanExpression_WithPrefixAndInfix_ExtendedOperands() throws Exception {
		debugWithBreakpoint("EvalSimpleTests", 18);
		IValue value = doEval(javaThread, "false && !(false || false || false);");

		assertNotNull("value is null", value);
		assertEquals("value is not false", "false", value.getValueString());
	}

	public void test547462_BooleanExpression_WithPrefixAndMixedInfix_ExtendedOperands() throws Exception {
		debugWithBreakpoint("EvalSimpleTests", 18);
		IValue value = doEval(javaThread, "false && !(false || false && false);");

		assertNotNull("value is null", value);
		assertEquals("value is not false", "false", value.getValueString());
	}

	public void test547462_BooleanExpression_WithPrefixAndInfix() throws Exception {
		debugWithBreakpoint("EvalSimpleTests", 18);
		IValue value = doEval(javaThread, "false && !(false || false);");

		assertNotNull("value is null", value);
		assertEquals("value is not false", "false", value.getValueString());
	}

	public void test547462_BooleanExpression_WithInfix() throws Exception {
		debugWithBreakpoint("EvalSimpleTests", 18);
		IValue value = doEval(javaThread, "true && (false || false);");

		assertNotNull("value is null", value);
		assertEquals("value is not false", "false", value.getValueString());
	}

	public void test547462_BooleanExpression() throws Exception {
		debugWithBreakpoint("EvalSimpleTests", 18);
		IValue value = doEval(javaThread, "false && false || true;");

		assertNotNull("value is null", value);
		assertEquals("value is not true", "true", value.getValueString());
	}

	public void test547462_BooleanExpression_NonShortCircuit() throws Exception {
		debugWithBreakpoint("EvalSimpleTests", 18);
		IValue value = doEval(javaThread, "false & false | true;");
		// IValue value = doEval(javaThread, "!(false && false);");

		assertNotNull("value is null", value);
		assertEquals("value is not true", "true", value.getValueString());
	}

	public void test547462_BooleanExpression_WithPrefixAndInfix_NonShortCircuit() throws Exception {
		debugWithBreakpoint("EvalSimpleTests", 18);
		IValue value = doEval(javaThread, "false & !(false | false);");

		assertNotNull("value is null", value);
		assertEquals("value is not false", "false", value.getValueString());
	}

	public void test547462_BooleanExpression_WithPrefixAndInfix_ExtendedOperands_NonShortCircuit() throws Exception {
		debugWithBreakpoint("EvalSimpleTests", 18);
		IValue value = doEval(javaThread, "false & !(false | false | false);");

		assertNotNull("value is null", value);
		assertEquals("value is not false", "false", value.getValueString());
	}

	public void test547462_BooleanExpression_WithPrefixAndMixedInfix_ExtendedOperands_NonShortCircuit() throws Exception {
		debugWithBreakpoint("EvalSimpleTests", 18);
		IValue value = doEval(javaThread, "false & !(false | false & false);");

		assertNotNull("value is null", value);
		assertEquals("value is not false", "false", value.getValueString());
	}

	public void test547462_BooleanExpression_WithInfix_NonShortCircuit() throws Exception {
		debugWithBreakpoint("EvalSimpleTests", 18);
		IValue value = doEval(javaThread, "true & (false | false);");

		assertNotNull("value is null", value);
		assertEquals("value is not false", "false", value.getValueString());
	}

	private void debugWithBreakpoint(String testClass, int lineNumber) throws Exception {
		createLineBreakpoint(lineNumber, testClass);
		javaThread = launchToBreakpoint(testClass);
		assertNotNull("The program did not suspend", javaThread);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			terminateAndRemove(javaThread);
		} finally {
			super.tearDown();
			removeAllBreakpoints();
		}
	}
}
