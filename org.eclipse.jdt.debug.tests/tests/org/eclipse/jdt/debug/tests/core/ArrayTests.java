/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests indexed collection API
 */
public class ArrayTests extends AbstractDebugTest {

	public ArrayTests(String name) {
		super(name);
	}

	public void testGetSize() throws Exception {
		String typeName = "ArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(19, typeName);
		
		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaVariable variable = frame.findVariable("array");
			assertNotNull(variable);
			IJavaArray array = (IJavaArray) variable.getValue();
			assertEquals("Array has wrong size", 100, array.getSize());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}				
	}
	
	
	public void testGetVariable() throws Exception {
		String typeName = "ArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(19, typeName);
		
		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaVariable v = frame.findVariable("array");
			assertNotNull(v);
			IJavaArray array = (IJavaArray) v.getValue();
			assertNotNull(array);
			IVariable variable = array.getVariable(99);
			assertNotNull(variable);
			assertEquals("Wrong value", ((IJavaDebugTarget)frame.getDebugTarget()).newValue(99), variable.getValue());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}				
	}	
	
	public void testGetVariableRange() throws Exception {
		String typeName = "ArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(19, typeName);
		
		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaVariable v = frame.findVariable("array");
			assertNotNull(v);
			IJavaArray array = (IJavaArray) v.getValue();
			assertNotNull(array);
			IVariable[] variables = array.getVariables(50, 15);
			assertNotNull(variables);
			for (int i = 0; i < 15; i++) {
				assertEquals("Wrong value", ((IJavaDebugTarget)frame.getDebugTarget()).newValue(50 + i), variables[i].getValue());
			}
			
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}				
	}		
	
}
