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
		
		createLineBreakpoint(19, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launch(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

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
}
