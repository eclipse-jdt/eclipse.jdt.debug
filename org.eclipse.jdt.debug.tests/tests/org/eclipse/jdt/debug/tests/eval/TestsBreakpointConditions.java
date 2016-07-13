/*******************************************************************************
 * Copyright (c) 2015, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.debug.tests.eval;

import org.eclipse.debug.core.model.IValue;

public class TestsBreakpointConditions extends Tests {

	public TestsBreakpointConditions(String name) {
		super(name);
	}

	public void init() throws Exception {
		initializeFrame("EvalArrayTests", 37, 1, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	public void testCondition1() throws Throwable {
		try {
			init();
			IValue value = eval("System.out.println(\"test\");true");
			System.out.println(value);

		}
		finally {
			end();
		}

	}

	public void testCondition2() throws Throwable {
		try {
			init();
			IValue value = eval("System.out.println(\"test\"); (1==1)");
			System.out.println(value);

		} finally {
			end();
		}

	}

	public void testCondition3() throws Throwable {
		try {
			init();
			IValue value = eval("System.out.println(\"test\");return true");
			System.out.println(value);

		}
		finally {
			end();
		}

	}
	/*
	 * To test throw as a last statement
	 */
	public void testCondition4() throws Throwable {
		try {
			init();
			IValue value = eval("System.out.println(\"test\");throw new Exception(\"test\")");
			System.out.println(value);

		}
		finally {
			end();
		}

	}
}
