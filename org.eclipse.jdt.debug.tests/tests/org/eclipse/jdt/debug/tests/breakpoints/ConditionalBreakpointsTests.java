/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
	 * Tests a breakpoint condition is not evaluated when it coincides with a step end.
	 * 
	 * @throws Exception
	 */
	public void testSkipConditionOnStep() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createLineBreakpoint(16, typeName);
		IJavaLineBreakpoint bp2 = createConditionalLineBreakpoint(17, typeName, "i = 3; return true;", true);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			// step from 16 to 17, breakpoint condition should not evaluate
			thread = stepOver((IJavaStackFrame) thread.getTopStackFrame());
			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable var = findVariable(frame, "i");
			assertNotNull("Could not find variable 'i'", var);
			
			IJavaPrimitiveValue value = (IJavaPrimitiveValue)var.getValue();
			assertNotNull("variable 'i' has no value", value);
			int iValue = value.getIntValue();
			assertTrue("value of 'i' should be '3', but was " + iValue, iValue == 0);
			
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
}
