/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;

/**
 * Tests conditional breakpoints.
 */
public class ConditionalBreakpointsTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public ConditionalBreakpointsTests(String name) {
		super(name);
	}

	/**
	 * Tests a breakpoint with a simple condition
	 * @throws Exception
	 */
	public void testSimpleConditionalBreakpoint() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createConditionalLineBreakpoint(19, typeName, "i == 3", true);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable var = findVariable(frame, "i");
			assertNotNull("Could not find variable 'i'", var);

			IJavaPrimitiveValue value = (IJavaPrimitiveValue)var.getValue();
			assertNotNull("variable 'i' has no value", value);
			int iValue = value.getIntValue();
			assertTrue("value of 'i' should be '3', but was " + iValue, iValue == 3);

			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a static method call that has a conditional breakpoint on it
	 * @throws Exception
	 */
	public void testStaticMethodCallConditionalBreakpoint() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createConditionalLineBreakpoint(19, typeName, "ArgumentsTests.fact(i) == 24", true);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable var = findVariable(frame, "i");
			assertNotNull("Could not find variable 'i'", var);

			IJavaPrimitiveValue value = (IJavaPrimitiveValue)var.getValue();
			assertNotNull("variable 'i' has no value", value);
			int iValue = value.getIntValue();
			assertTrue("value of 'i' should be '4', but was " + iValue, iValue == 4);

			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a simple conditional breakpoint that gets hit when a change is made
	 * @throws Exception
	 */
	public void testSimpleConditionalBreakpointSuspendOnChange() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createConditionalLineBreakpoint(19, typeName, "i != 9", false);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable var = findVariable(frame, "i");
			assertNotNull("Could not find variable 'i'", var);

			IJavaPrimitiveValue value = (IJavaPrimitiveValue)var.getValue();
			assertNotNull("variable 'i' has no value", value);
			int iValue = value.getIntValue();
			assertEquals(0, iValue);

			resumeToLineBreakpoint(thread, bp);

			frame = (IJavaStackFrame)thread.getTopStackFrame();
			var = findVariable(frame, "i");
			assertNotNull("Could not find variable 'i'", var);

			value = (IJavaPrimitiveValue)var.getValue();
			assertNotNull("variable 'i' has no value", value);
			iValue = value.getIntValue();
			assertEquals(9, iValue);

			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a conditional step return
	 * @throws Exception
	 */
	public void testConditionalStepReturn() throws Exception {
		String typeName = "ConditionalStepReturn";
		IJavaLineBreakpoint lineBreakpoint = createLineBreakpoint(20, typeName);
		createConditionalLineBreakpoint(18, typeName, "!bool", true);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, lineBreakpoint);
			thread = stepReturn((IJavaStackFrame)thread.getTopStackFrame());
			// should not have suspended at breakpoint
			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			assertEquals("Should be in main", "main", frame.getMethodName());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a breakpoint condition *is* evaluated when it coincides with a step end.
	 * See bug 265714.
	 *
	 * @throws Exception
	 */
	public void testEvalConditionOnStep() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createLineBreakpoint(19, typeName);
		IJavaLineBreakpoint bp2 = createConditionalLineBreakpoint(20, typeName, "i = 3; return true;", true);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			// step from 16 to 17, breakpoint condition *should* evaluate
			thread = stepOver((IJavaStackFrame) thread.getTopStackFrame());
			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable var = findVariable(frame, "i");
			assertNotNull("Could not find variable 'i'", var);

			IJavaPrimitiveValue value = (IJavaPrimitiveValue)var.getValue();
			assertNotNull("variable 'i' has no value", value);
			int iValue = value.getIntValue();
			assertEquals("'i' has wrong value", 3, iValue);

			// breakpoint should still be available from thread, even though not eval'd
			IBreakpoint[] breakpoints = thread.getBreakpoints();
			assertEquals("Wrong number of breakpoints", 1, breakpoints.length);
			assertEquals("Wrong breakpoint", bp2, breakpoints[0]);

			bp.delete();
			bp2.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that a thread can be suspended when executing a long-running condition.
	 *
	 * @throws Exception
	 */
	public void testSuspendLongRunningCondition() throws Exception {
		String typeName = "MethodCall";
		IJavaLineBreakpoint first = createLineBreakpoint(22, typeName);
		createConditionalLineBreakpoint(30, typeName, "for (int x = 0; x < 1000; x++) { System.out.println(x);} Thread.sleep(1000); return true;", true);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, first);
			IStackFrame top = thread.getTopStackFrame();
			assertNotNull("Missing top frame", top);
			thread.resume();
			// wait for evaluation to start
			long start = System.currentTimeMillis();
			while ((System.currentTimeMillis() - start) <= DEFAULT_TIMEOUT && !thread.isPerformingEvaluation()) {
				Thread.sleep(10);
			}
			assertTrue("Expected evaluation for second breakpoint", thread.isPerformingEvaluation());
			/*
			 * Check that we can suspend during the breakpoint condition evaluation. The suspend will interrupt the evaluation, meaning the
			 * conditional breakpoint won't actually be hit. Suspending will however result in stopping the thread at the breakpoint location.
			 */
			thread.suspend();
			TestUtil.waitForJobs(getName(), 100, DEFAULT_TIMEOUT, ProcessConsole.class);
			assertTrue("Thread should be suspended", thread.isSuspended());
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull("Missing top frame", frame);
			assertEquals("Wrong location", "calculateSum", frame.getName());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that a conditional breakpoint with an expression that will hit a breakpoint
	 * will complete the conditional expression evaluation (bug 269231).
	 *
	 * @throws Exception
	 */
	public void testConditionalExpressionIgnoresBreakpoint() throws Exception {
		String typeName = "BreakpointListenerTest";
		createConditionalLineBreakpoint(15, typeName, "foo(); return false;", true);
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(20, typeName);
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, breakpoint);
			IStackFrame top = thread.getTopStackFrame();
			assertNotNull("Missing top frame", top);
			assertTrue("Thread should be suspended", thread.isSuspended());
			assertEquals("Wrong location", breakpoint.getLineNumber(), top.getLineNumber());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testConditionalMultiInfix1() throws Exception {
		String typeName = "ConditionalStepReturn";
		createConditionalLineBreakpoint(17, typeName, "(true==true==true==true==true)", true);

		IJavaThread thread= null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("The program should have suspended on the coniditional breakpoint", thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testConditionalMultiInfix2() throws Exception {
		String typeName = "ConditionalStepReturn";
		createConditionalLineBreakpoint(17, typeName, "!(true==true==true==true==true)", false);

		IJavaThread thread= null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("The program should have suspended on the coniditional breakpoint", thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testConditionalMultiInfix3() throws Exception {
		String typeName = "ConditionalStepReturn";
		createConditionalLineBreakpoint(17, typeName, "(true&&true&&true&&true&&true)", true);

		IJavaThread thread= null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("The program should have suspended on the coniditional breakpoint", thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testConditionalMultiInfix4() throws Exception {
		String typeName = "ConditionalStepReturn";
		createConditionalLineBreakpoint(17, typeName, "!(true&&true&&true&&true&&true)", false);

		IJavaThread thread= null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("The program should have suspended on the coniditional breakpoint", thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testConditionalMultiInfix5() throws Exception {
		String typeName = "ConditionalStepReturn";
		createConditionalLineBreakpoint(17, typeName, "true&&true||false", true);

		IJavaThread thread= null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("The program should have suspended on the coniditional breakpoint", thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testConditionalMultiInfix6() throws Exception {
		String typeName = "ConditionalStepReturn";
		createConditionalLineBreakpoint(17, typeName, "(1<=2==true||false)", true);

		IJavaThread thread= null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("The program should have suspended on the coniditional breakpoint", thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testConditionalMultiInfix7() throws Exception {
		String typeName = "ConditionalStepReturn";
		createConditionalLineBreakpoint(17, typeName, "!(1<=2==true||false)", false);

		IJavaThread thread= null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("The program should have suspended on the coniditional breakpoint", thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testConditionalMultiInfix8() throws Exception {
		String typeName = "ConditionalStepReturn";
		createConditionalLineBreakpoint(17, typeName, "(true != false && false)", false);

		IJavaThread thread= null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("The program should have suspended on the coniditional breakpoint", thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testConditionalMultiInfix9() throws Exception {
		String typeName = "ConditionalStepReturn";
		createConditionalLineBreakpoint(17, typeName, "!(true != false && false)", true);

		IJavaThread thread= null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("The program should have suspended on the coniditional breakpoint", thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testConditionalMultiInfix10() throws Exception {
		String typeName = "ConditionalStepReturn";
		createConditionalLineBreakpoint(17, typeName, "(true||true||true||true||true)", true);

		IJavaThread thread= null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("The program should have suspended on the coniditional breakpoint", thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testConditionalMultiInfix11() throws Exception {
		String typeName = "ConditionalStepReturn";
		createConditionalLineBreakpoint(17, typeName, "!(true||true||true||true||true)", false);

		IJavaThread thread= null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("The program should have suspended on the coniditional breakpoint", thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testConditionalMultiInfix12() throws Exception {
		String typeName = "ConditionalStepReturn";
		createConditionalLineBreakpoint(17, typeName, "(true==true||true!=true&&true)", true);

		IJavaThread thread= null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("The program should have suspended on the coniditional breakpoint", thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
	public void testConditionalMultiInfix13() throws Exception {
		String typeName = "ConditionalStepReturn";
		createConditionalLineBreakpoint(17, typeName, "!(true==true||true!=true&&true)", false);

		IJavaThread thread= null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("The program should have suspended on the coniditional breakpoint", thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a breakpoint with a simple systrace Launch should don't suspend for simple systrace
	 *
	 * @throws Exception
	 */
	public void testSystracelBreakpoint() throws Exception {
		String typeName = "HitCountLooper";
		createConditionalLineBreakpoint(16, typeName, "System.out.println(\"enclosing_type.enclosing_method()\");", true);
		IJavaLineBreakpoint bp1 = createConditionalLineBreakpoint(17, typeName, "return true", true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp1);

		}
		finally {
			assertNotNull(thread);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a breakpoint with a simple code which returns Integer Object, Launch should don't suspend for non true boolean returns
	 *
	 * @throws Exception
	 */
	public void testConditionBreakpointReturnNonBooleanObject() throws Exception {
		String typeName = "HitCountLooper";
		createConditionalLineBreakpoint(16, typeName, "return new Integer(1)", true);
		IJavaLineBreakpoint bp1 = createConditionalLineBreakpoint(17, typeName, "return true", true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp1);
			assertNotNull(thread);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a breakpoint with a simple code which returns Boolean Object with true, Launch should suspend for true Boolean returns
	 *
	 * @throws Exception
	 */
	public void testConditionBreakpointReturnBooleanObjectTrue() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createConditionalLineBreakpoint(19, typeName, "return new Boolean(true)", true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp);

		}
		finally {
			assertNotNull(thread);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a breakpoint with a simple code which returns Boolean Object with false, Launch should not suspend for false Boolean returns
	 *
	 * @throws Exception
	 */
	public void testConditionBreakpointReturnBooleanObjectFalse() throws Exception {
		String typeName = "HitCountLooper";
		createConditionalLineBreakpoint(16, typeName, "return new Boolean(false)", true);
		IJavaLineBreakpoint bp1 = createConditionalLineBreakpoint(17, typeName, "return true", true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp1);

		}
		finally {
			assertNotNull(thread);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
