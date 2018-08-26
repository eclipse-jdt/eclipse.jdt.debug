/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
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

import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;

public class TestsAnonymousClassVariable extends Tests {
	/**
	 * Constructor for TestsAnonymousClassVariable.
	 *
	 * @param name
	 */
	public TestsAnonymousClassVariable(String name) {
		super(name);
	}

	public void init(int breakPointLineNumber, int frameNo) throws Exception {
		initializeFrame("EvalAnonymousClassVariableTests", breakPointLineNumber, frameNo, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	public void testEvalAnonymousClassVariable1() throws Throwable {
		try {
			init(20, 2);
			IValue value = eval("innerClassField");
			String typeName = value.getReferenceTypeName();
			assertEquals("T_T_this_e : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue) value).getIntValue();
			assertEquals("T_T_this_e : wrong result : ", 0, intValue);

		} finally {
		end();
		}
	}

	public void testEvalAnonymousClassVariable2() throws Throwable {
		try {
			init(33, 1);
			IValue value = eval("latch");
			String typeName = value.getReferenceTypeName();
			assertEquals("T_T_this_e : wrong type : ", "java.util.concurrent.CountDownLatch", typeName);
		}
		finally {
			end();
		}
	}

}
