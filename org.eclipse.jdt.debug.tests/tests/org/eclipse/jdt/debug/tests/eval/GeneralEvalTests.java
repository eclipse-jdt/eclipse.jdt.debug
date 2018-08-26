/*******************************************************************************
 * Copyright (c) Mar 6, 2013 IBM Corporation and others.
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
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests that evaluations in non-generified source
 *
 * @since 3.8.100
 */
public class GeneralEvalTests extends AbstractDebugTest {


	/**
	 * @param name
	 */
	public GeneralEvalTests(String name) {
		super(name);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
	public void testInnerType1() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug329294";
			createLineBreakpoint(22, typename);
			thread = launchToBreakpoint(typename);
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
	public void testInnerType2() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug329294";
			createLineBreakpoint(26, typename);
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
	public void testInnerType3() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug329294";
			createLineBreakpoint(30, typename);
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
	public void testInnerAnonymousType() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug329294";
			createLineBreakpoint(7, typename);
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
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval1() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(16, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "(true==true==true==true==true)";
			IValue value = doEval(thread, snippet);
			assertTrue("The result of (true==true==true==true==true) should be true", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval2() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(17, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "!(true==true==true==true==true)";
			IValue value = doEval(thread, snippet);
			assertFalse("The result of !(true==true==true==true==true) should be false", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval3() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(18, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "(true&&true&&true&&true&&true)";
			IValue value = doEval(thread, snippet);
			assertTrue("The result of (true&&true&&true&&true&&true) should be true", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval4() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(19, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "!(true&&true&&true&&true&&true)";
			IValue value = doEval(thread, snippet);
			assertFalse("The result of !(true&&true&&true&&true&&true) should be false", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval5() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(20, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "true&&true||false";
			IValue value = doEval(thread, snippet);
			assertTrue("The result of true&&true||false should be true", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval6() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(21, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "(1<=2==true||false)";
			IValue value = doEval(thread, snippet);
			assertTrue("The result of (1<=2==true||false) should be true", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval7() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(22, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "!(1<=2==true||false)";
			IValue value = doEval(thread, snippet);
			assertFalse("The result of !(1<=2==true||false) should be false", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval8() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(23, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "(true != false && false)";
			IValue value = doEval(thread, snippet);
			assertFalse("The result of (true != false && false) should be false", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval9() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(24, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "!(true != false && false)";
			IValue value = doEval(thread, snippet);
			assertTrue("The result of !(true != false && false) should be true", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval10() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(22, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "(true||true||true||true||true)";
			IValue value = doEval(thread, snippet);
			assertTrue("The result of (true||true||true||true||true) should be true", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval11() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(23, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "!(true||true||true||true||true)";
			IValue value = doEval(thread, snippet);
			assertFalse("The result of !(true||true||true||true||true) should be false", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval12() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(24, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "(true==true||true!=true&&true)";
			IValue value = doEval(thread, snippet);
			assertTrue("The result of (true==true||true!=true&&true) should be true", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval13() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(25, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "!(true==true||true!=true&&true)";
			IValue value = doEval(thread, snippet);
			assertFalse("The result of !(true==true||true!=true&&true) should be false", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval14() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(25, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "(true || !(true==true||true!=true&&true))";
			IValue value = doEval(thread, snippet);
			assertTrue("The result of (true || !(true==true||true!=true&&true)) should be true", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval15() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(25, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "(true && true || false || !(true==true||true!=true&&true))";
			IValue value = doEval(thread, snippet);
			assertTrue("The result of (true && true || false || !(true==true||true!=true&&true)) should be true", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testMultipleInfixEval16() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(25, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "(true && true || !(false&&true) || !(true==true||true!=true&&true))";
			IValue value = doEval(thread, snippet);
			assertTrue("The result of (true && true || !(false&&true) || !(true==true||true!=true&&true)) should be true", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028
	 * @throws Exception
	 */
	public void testCompoundCondition() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(25, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);
			String snippet = "(true && true || !(false&&true) || !(true==true||true!=true&&true))";
			IValue value = doEval(thread, snippet);
			assertTrue("The result of (true && true || !(false&&true) || !(true==true||true!=true&&true)) should be true", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=508105
	 *
	 * @throws Exception
	 */
	public void testMultiByteCharacters() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(25, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);

			String snippet = "int äüßö€ = 1; { if(äüßö€ < 0) return false; }; return äüßö€ > 0";
			IValue value = doEval(thread, snippet);
			assertTrue("The result of 'int äüßö€ = 1; { if(äüßö€ < 0) return false; }; return äüßö€ > 0' should be true", Boolean.parseBoolean(value.getValueString()));

			snippet = "\"ทดสอบ\".length() > 0";
			value = doEval(thread, snippet);
			assertTrue("The result of '\"ทดสอบ\".length() > 0' should be true", Boolean.parseBoolean(value.getValueString()));

			snippet = "return \"ทดสอบ\".length() == 5";
			value = doEval(thread, snippet);
			assertTrue("The result of 'return \"ทดสอบ\".length() == 5' should be true", Boolean.parseBoolean(value.getValueString()));

			snippet = "{return \"ทดสอบ\".length() != 5;}";
			value = doEval(thread, snippet);
			assertFalse("The result of '{return \"ทดสอบ\".length() != 5;}' should be false", Boolean.parseBoolean(value.getValueString()));

			snippet = "{/**/};\n{return \"ทดสอบ\".charAt(0) == '\\\\';}";
			value = doEval(thread, snippet);
			assertFalse("The result of '{/**/};\\n{return \\\"ทดสอบ\\\".charAt(0) == '\\\\\\\\';}' should be false", Boolean.parseBoolean(value.getValueString()));
		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=508105
	 *
	 * @throws Exception
	 */
	public void XtestAReturn() throws Exception {
		IJavaThread thread = null;
		try {
			String typename = "bug401270";
			createLineBreakpoint(25, typename);
			thread = launchToBreakpoint(typename);
			assertNotNull("the program did not suspend", thread);

			String snippet = "int a = 1; return a > 0";
			IValue value = doEval(thread, snippet);
			assertTrue("The result of 'int a = 1; return a > 0' should be true", Boolean.parseBoolean(value.getValueString()));

			snippet = "int areturn = 1; return areturn > 0";
			value = doEval(thread, snippet);
			assertTrue("The result of 'int areturn = 1; return areturn > 0' should be true", Boolean.parseBoolean(value.getValueString()));

		}
		finally {
			removeAllBreakpoints();
			terminateAndRemove(thread);
		}
	}
}
