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

import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class LocalVariableTests extends AbstractDebugTest {
	
	public LocalVariableTests(String name) {
		super(name);
	}

	public void testSimpleVisibility() throws Exception {
		String typeName = "LocalVariablesTests";
		
		ILineBreakpoint bp = createLineBreakpoint(18, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IJavaVariable[] vars = frame.getLocalVariables();
			assertEquals("Should be no visible locals", 0, vars.length);
			
			stepOver(frame);
			stepOver(frame);
			
			vars = frame.getLocalVariables();
			assertEquals("Should be one visible local", 1, vars.length);			
			assertEquals("Visible var should be 'i1'", "i1", vars[0].getName());
			
			stepOver(frame);
			stepOver(frame);
			
			vars = frame.getLocalVariables();
			assertEquals("Should be two visible locals", 2, vars.length);			
			assertEquals("Visible var 1 should be 'i1'", "i1", vars[0].getName());			
			assertEquals("Visible var 2 should be 'i2'", "i2", vars[1].getName());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	public void testEvaluationAssignments() throws Exception {
		String typeName = "LocalVariablesTests";
		
		ILineBreakpoint bp = createLineBreakpoint(22, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IJavaDebugTarget target = (IJavaDebugTarget)frame.getDebugTarget();
			IVariable i1 = frame.findVariable("i1");
			assertNotNull("Could not find variable 'i1'", i1);
			assertEquals("'i1' value should be '0'", target.newValue(0), i1.getValue());
			
			IVariable i2 = frame.findVariable("i2");
			assertNotNull("Could not find variable 'i2'", i2);
			assertEquals("'i2' value should be '1'", target.newValue(1), i2.getValue());
						
			evaluate("i1 = 73;", frame);			
			// the value should have changed
			assertEquals("'i1' value should be '73'", target.newValue(73), i1.getValue());
			
			evaluate("i2 = i1;", frame);
			// the value should have changed
			assertEquals("'i2' value should be '73'", target.newValue(73), i2.getValue());
			
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}		
}
