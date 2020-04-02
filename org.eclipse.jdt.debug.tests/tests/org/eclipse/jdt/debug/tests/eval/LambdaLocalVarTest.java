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
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;

public class LambdaLocalVarTest extends AbstractDebugTest {
	private IJavaThread javaThread;

	@Override
	protected IJavaProject getProjectContext() {
		return get18Project();
	}

	public LambdaLocalVarTest(String name) {
		super(name);
	}

	public void testEvaluate_LambdaFieldVariable() throws Exception {
		debugWithBreakpoint("Bug561715", 7);
		javaThread.resume();
		TestUtil.waitForJobs(getName(), 1000, DEFAULT_TIMEOUT, ProcessConsole.class);

		String snippet = "a";
		IValue value = doEval(javaThread, snippet);

		assertEquals("wrong type : ", "java.lang.String", value.getReferenceTypeName());
		assertEquals("wrong result : ", "111", value.getValueString());
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
