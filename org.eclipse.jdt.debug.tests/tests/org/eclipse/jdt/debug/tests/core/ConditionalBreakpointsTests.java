package org.eclipse.jdt.debug.tests.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
*********************************************************************/

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
	
	public ConditionalBreakpointsTests(String name) {
		super(name);
	}

	public void testSimpleConditionalBreakpoint() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createConditionalLineBreakpoint(16, typeName, "i == 3", true);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable var = frame.findVariable("i");
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

	public void testStaticMethodCallConditionalBreakpoint() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createConditionalLineBreakpoint(16, typeName, "ArgumentsTests.fact(i) == 24", true);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable var = frame.findVariable("i");
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

	public void testSimpleConditionalBreakpointSuspendOnChange() throws Exception {
		String typeName = "HitCountLooper";
		IJavaLineBreakpoint bp = createConditionalLineBreakpoint(16, typeName, "i != 9", false);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable var = frame.findVariable("i");
			assertNotNull("Could not find variable 'i'", var);
			
			IJavaPrimitiveValue value = (IJavaPrimitiveValue)var.getValue();
			assertNotNull("variable 'i' has no value", value);
			int iValue = value.getIntValue();
			assertEquals(0, iValue);
			
			resumeToLineBreakpoint(thread, bp);
			
			frame = (IJavaStackFrame)thread.getTopStackFrame();
			var = frame.findVariable("i");
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

	public void testConditionalStepReturn() throws Exception {
		String typeName = "ConditionalStepReturn";
		IJavaLineBreakpoint lineBreakpoint = createLineBreakpoint(15, typeName);
		createConditionalLineBreakpoint(15, typeName, "!bool", true);
		
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

}
