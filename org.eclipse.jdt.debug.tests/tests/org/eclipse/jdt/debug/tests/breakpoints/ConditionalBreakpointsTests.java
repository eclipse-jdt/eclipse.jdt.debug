/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

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
		IJavaLineBreakpoint bp = createConditionalLineBreakpoint(16, typeName, "i == 3", true);
		
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
		IJavaLineBreakpoint bp = createConditionalLineBreakpoint(16, typeName, "ArgumentsTests.fact(i) == 24", true);
		
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
		IJavaLineBreakpoint bp = createConditionalLineBreakpoint(16, typeName, "i != 9", false);
		
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
		IJavaLineBreakpoint lineBreakpoint = createLineBreakpoint(17, typeName);
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
		IJavaLineBreakpoint bp = createLineBreakpoint(16, typeName);
		IJavaLineBreakpoint bp2 = createConditionalLineBreakpoint(17, typeName, "i = 3; return true;", true);
		
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
		String typeName = "MethodLoop";
		IJavaLineBreakpoint first = createLineBreakpoint(19, typeName);
		createConditionalLineBreakpoint(29, typeName, "for (int x = 0; x < 1000; x++) { System.out.println(x);} Thread.sleep(200); return true;", true);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, first);
			IStackFrame top = thread.getTopStackFrame();
			assertNotNull("Missing top frame", top);
			thread.resume();
			Thread.sleep(100);
			thread.suspend();
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
		IJavaLineBreakpoint bp = createConditionalLineBreakpoint(16, typeName, "System.out.println(\"enclosing_type.enclosing_method()\");", true);
		IJavaLineBreakpoint bp1 = createConditionalLineBreakpoint(17, typeName, "return true", true);

		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp1);

		}
		finally {
			bp.delete();
			assertNotNull(thread);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
