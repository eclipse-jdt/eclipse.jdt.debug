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
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;

public class AnonClassEvalTests extends AbstractDebugTest {
	private IJavaThread javaThread;

	@Override
	protected IJavaProject getProjectContext() {
		return get18Project();
	}

	public AnonClassEvalTests(String name) {
		super(name);
	}

	public void testEvaluate_OuterVariable_InsideLambda() throws Exception {
		debugWithBreakpoint("Bug564486", 9);
		String snippet = "args";
		IValue value = doEval(javaThread, snippet);

		assertEquals("wrong type : ", "java.lang.String[]", value.getReferenceTypeName());
		assertEquals("wrong result : ", 0, ((JDIObjectValue) value).getArrayLength());
	}

	public void testEvaluate_LambdaVariable_InsideLambda() throws Exception {
		debugWithBreakpoint("Bug564486", 9);
		String snippet = "i";
		IValue value = doEval(javaThread, snippet);

		assertEquals("wrong type : ", "java.lang.Integer", value.getReferenceTypeName());
		assertEquals("wrong result : ", "1", ((JDIObjectValue) value).getField("value", false).getValue().toString());
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
