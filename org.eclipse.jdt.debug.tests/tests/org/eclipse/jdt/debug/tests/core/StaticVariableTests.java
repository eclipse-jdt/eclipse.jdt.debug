package org.eclipse.jdt.debug.tests.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class StaticVariableTests extends AbstractDebugTest {
	
	public StaticVariableTests(String name) {
		super(name);
	}

	public void testSetValue() throws Exception {
		String typeName = "StaticVariablesTests";
		
		createLineBreakpoint(29, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launch(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable pubStr = frame.findVariable("pubStr");
			assertNotNull("Could not find variable 'pubStr'", pubStr);
			
			assertEquals("Value should be 'public'","public", pubStr.getValue().getValueString());
			pubStr.setValue(((IJavaDebugTarget)frame.getDebugTarget()).newValue("test"));
			assertEquals("Value should be 'test'","test", pubStr.getValue().getValueString());
			
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
}
