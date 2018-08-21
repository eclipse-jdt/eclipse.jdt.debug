/*******************************************************************************
 * Copyright (c) 2015, 2017 IBM Corporation and others.
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

public class TestsBreakpointConditions extends Tests {

	public TestsBreakpointConditions(String name) {
		super(name);
	}

	public void init() throws Exception {
		initializeFrame("EvalSimpleTests", 37, 1, 1);
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

	/*
	 * To test xVarInt < (xVarInt + 100)
	 */
	public void testCondition5() throws Throwable {
		try {
			init();
			IValue value = eval("xVarInt < (xVarInt + 100)");
			System.out.println(value);

		}
		finally {
			end();
		}

	}
	/*
	 * To test xVarInt < 100
	 */
	public void testCondition6() throws Throwable {
		try {
			init();
			IValue value = eval("xVarInt < 100");
			System.out.println(value);

		}
		finally {
			end();
		}

	}

	/*
	 * To test xVarInt > 100
	 */
	public void testCondition7() throws Throwable {
		try {
			init();
			IValue value = eval("xVarInt > -7");
			System.out.println(value);

		}
		finally {
			end();
		}

	}

	/*
	 * To test xVarInt == 100
	 */
	public void testCondition8() throws Throwable {
		try {
			init();
			IValue value = eval("xVarInt == -7");
			System.out.println(value);

		}
		finally {
			end();
		}

	}

	/*
	 * To test return xVarInt < 100
	 */
	public void testCondition9() throws Throwable {
		try {
			init();
			IValue value = eval("return xVarInt < 100");
			System.out.println(value);

		}
		finally {
			end();
		}

	}

	/*
	 * To test return xVarInt > 100
	 */
	public void testCondition10() throws Throwable {
		try {
			init();
			IValue value = eval("return xVarInt > -7");
			System.out.println(value);

		}
		finally {
			end();
		}

	}

	/*
	 * To test if as last statement
	 */
	public void testCondition11() throws Throwable {
		try {
			init();
			IValue value = eval("if (xVarInt > 3) { System.out.println(\"test if\");} ");
			System.out.println(value);

		}
		finally {
			end();
		}

	}

	/*
	 * To test while as last statement
	 */
	public void testCondition12() throws Throwable {
		try {
			init();
			IValue value = eval("while (xVarInt < 3) { System.out.println(\"test while\");xVarInt++; } ");
			System.out.println(value);

		}
		finally {
			end();
		}

	}
}
