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
