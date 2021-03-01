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

public class BlockStatementEvaluationTests extends AbstractDebugTest {
	private IJavaThread javaThread;

	@Override
	protected IJavaProject getProjectContext() {
		return get18Project();
	}

	public BlockStatementEvaluationTests(String name) {
		super(name);
	}

	public void testEvaluate_ForLoopBlock_ShouldEvaluate() throws Exception {
		debugWithBreakpoint("Bug571230", 7);
		String snippet = "for(int i = 0; i < 5; i++){\n" + "	list.add(i);\n" + "}";

		doEval(javaThread, snippet);

		snippet = "list.size()";
		IValue value = doEval(javaThread, snippet);
		assertEquals("wrong result : " + value.getValueString(), "5", value.getValueString());
	}

	public void testEvaluate_ForLoopBlockWithReturn_ShouldEvaluate() throws Exception {
		debugWithBreakpoint("Bug571230", 7);
		String snippet = "for(int i = 0; i < 5; i++){\n" + "	list.add(i);\n" + "	return list.size();\n" + "}";

		IValue value = doEval(javaThread, snippet);
		assertEquals("wrong result : " + value.getValueString(), "1", value.getValueString());
	}

	public void testEvaluate_ForLoopBlockWithReturnWithExpressionAtEnd_ShouldEvaluate() throws Exception {
		debugWithBreakpoint("Bug571230", 7);
		String snippet = "for(int i = 0; i < 5; i++){\n" + "	list.add(i);\n" + "	return list;\n" + "}\n" + "list.size()";

		IValue value = doEval(javaThread, snippet);
		assertEquals("wrong result : " + value.getReferenceTypeName(), "java.util.ArrayList<E>", value.getReferenceTypeName());
	}

	public void testEvaluate_IfBlock_ShouldEvaluate() throws Exception {
		debugWithBreakpoint("Bug571230", 7);
		String snippet = "if(list.isEmpty()){\n" + "	list.add(10);\n" + "}";

		doEval(javaThread, snippet);

		snippet = "list.size() > 0";
		IValue value = doEval(javaThread, snippet);
		assertEquals("wrong result : " + value.getValueString(), "true", value.getValueString());
	}

	public void testEvaluate_IfElseWithReturnBlock_ShouldEvaluate() throws Exception {
		debugWithBreakpoint("Bug571230", 7);
		String snippet = "if(list.isEmpty()){\n" + "	list.add(10);\n" + "} else {\n" + "	return 10;" + "}";

		doEval(javaThread, snippet);

		snippet = "list.size() > 0";
		IValue value = doEval(javaThread, snippet);
		assertEquals("wrong result : " + value.getValueString(), "true", value.getValueString());
	}

	public void testEvaluate_IfElseWithReturnBlock_WithExpressionAtEnd_ShouldEvaluateExpression() throws Exception {
		debugWithBreakpoint("Bug571230", 7);
		String snippet = "if(list.isEmpty()){\n" + "	list.add(10);\n" + "} else {\n" + "	return 10;" + "}\n" + "list.size() > 0";

		IValue value = doEval(javaThread, snippet);
		assertEquals("wrong result : " + value.getValueString(), "true", value.getValueString());
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
