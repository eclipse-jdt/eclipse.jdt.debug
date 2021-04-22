/*******************************************************************************
 * Copyright (c) 2020 Gayan Perera and others.
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
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class GenericsEval17Test extends AbstractDebugTest {
	private IJavaThread javaThread;

	@Override
	protected IJavaProject getProjectContext() {
		return get17Project();
	}

	public GenericsEval17Test(String name) {
		super(name);
	}

	public void testEvaluate_Bug567801_UnionType_ExpectValueType() throws Exception {
		debugWithBreakpoint("Bug567801", 13);
		String snippet = "e instanceof java.lang.Exception";
		IValue value = doEval(javaThread, snippet);

		assertNotNull("value is null", value);
		assertEquals("value is not true", "true", value.getValueString());
	}

	public void testEvaluate_Bug572782_RecursiveGeneric_ExpectedEvalVarValue() throws Exception {
		debugWithBreakpoint("Bug572782", 15);
		String snippet = "this";
		IValue value = doEval(javaThread, snippet);

		assertNotNull("value is null", value);
		assertTrue("No a IJavaObjectValue", value instanceof IJavaObject);
		assertEquals("value don't has the correct generic signature", "<T:LBug572782$Generic<LBug572782$ExtendedGeneric<TT;>;>;>Ljava/lang/Object;",
				((IJavaObject) value).getGenericSignature());
	}

	public void testEvaluate_Bug572782_RecursiveGeneric_ExpectedEvalExpressionValue() throws Exception {
		debugWithBreakpoint("Bug572782", 15);
		String snippet = "1 + 2";
		IValue value = doEval(javaThread, snippet);

		assertNotNull("value is null", value);
		assertEquals("value is not 3", "3", value.getValueString());
	}

	public void testEvaluate_Bug572782_RecursiveGenericSimple_ExpectedEvalExpressionValue() throws Exception {
		debugWithBreakpoint("Bug572782", 22);
		String snippet = "1 + 2";
		IValue value = doEval(javaThread, snippet);

		assertNotNull("value is null", value);
		assertEquals("value is not 3", "3", value.getValueString());
	}

	public void testEvaluate_Bug572782_RecursiveNDeepGeneric_ExpectedEvalExpressionValue() throws Exception {
		debugWithBreakpoint("Bug572782", 31);
		String snippet = "1 + 2";
		IValue value = doEval(javaThread, snippet);

		assertNotNull("value is null", value);
		assertEquals("value is not 3", "3", value.getValueString());
	}

	public void testEvaluate_Bug572782_InSideStaticMethod_ExpectedEvalExpressionValue() throws Exception {
		debugWithBreakpoint("Bug572782", 41);
		String snippet = "list.get(0)";
		IValue value = doEval(javaThread, snippet);

		assertNotNull("value is null", value);
		assertEquals("value is not 1", "1", value.getValueString());
	}

	public void testEvaluate_Bug572782_RecursiveNDeepGeneric_InSideStaticMethod_ExpectedEvalExpressionValue() throws Exception {
		debugWithBreakpoint("Bug572782", 36);
		String snippet = "1 + 2";
		IValue value = doEval(javaThread, snippet);

		assertNotNull("value is null", value);
		assertEquals("value is not 3", "3", value.getValueString());
	}

	public void testEvaluate_Bug572782_InstanceMethodWithGenerics_ExpectedEvalExpressionValue() throws Exception {
		debugWithBreakpoint("Bug572782", 45);
		String snippet = "1 + 2";
		IValue value = doEval(javaThread, snippet);

		assertNotNull("value is null", value);
		assertEquals("value is not 3", "3", value.getValueString());
	}

	public void testEvaluate_Bug572782_FieldWithGenerics_ExpectedEvalExpressionValue() throws Exception {
		createWatchpoint("Bug572782.NthGeneric", "items", false, true);
		javaThread = launchToBreakpoint("Bug572782");
		assertNotNull("The program did not suspend", javaThread);

		String snippet = "1 + 2";
		IValue value = doEval(javaThread, snippet);

		assertNotNull("value is null", value);
		assertEquals("value is not 3", "3", value.getValueString());
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
