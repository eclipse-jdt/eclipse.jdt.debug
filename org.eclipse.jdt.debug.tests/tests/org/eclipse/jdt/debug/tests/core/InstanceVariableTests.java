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
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class InstanceVariableTests extends AbstractDebugTest {
	
	public InstanceVariableTests(String name) {
		super(name);
	}

	public void testGetField() throws Exception {
		String typeName = "InstanceVariablesTests";
		
		ILineBreakpoint bp = createLineBreakpoint(19, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable ivt = frame.findVariable("ivt");
			assertNotNull("Could not find variable 'ivt'", ivt);
			
			// retrieve an instance var
			IJavaObject value = (IJavaObject)ivt.getValue();
			assertNotNull(value);
			IJavaVariable pubStr = value.getField("pubStr", false);
			assertNotNull(pubStr);
			assertEquals("value should be 'redefined public'", pubStr.getValue().getValueString(), "redefined public");
			
			// retrieve an instance var in superclass
			IJavaVariable privStr = value.getField("privStr", false);
			assertNotNull(privStr);
			assertEquals("value should be 'private'", privStr.getValue().getValueString(), "private");			
			
			// retrieve an instance var in super class with same name
			pubStr = value.getField("pubStr", true);
			assertNotNull(pubStr);
			assertEquals("value should be 'public'", pubStr.getValue().getValueString(), "public");			

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	public void testEvaluationAssignments() throws Exception {
		String typeName = "InstanceVariablesTests";
		
		ILineBreakpoint bp = createLineBreakpoint(17, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable pubStr = frame.findVariable("pubStr");
			assertNotNull("Could not find variable 'pubStr'", pubStr);
			assertEquals("'pubStr' value should be 'public'", "public", pubStr.getValue().getValueString());
			
			evaluate("pubStr = \"hello\";", frame);			
			// the value should have changed
			assertEquals("'pubStr' value should be 'hello'", "hello", pubStr.getValue().getValueString());
			
			evaluate("pubStr = null;", frame);
			// the value should have changed
			assertEquals("'pubStr' value should be 'null'", ((IJavaDebugTarget)frame.getDebugTarget()).nullValue(), pubStr.getValue());
			
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	
}
