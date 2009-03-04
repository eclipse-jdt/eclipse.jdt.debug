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
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
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
			IJavaVariable variable = findVariable(frame, "array");
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
			IJavaVariable v = findVariable(frame, "array");
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
			IJavaVariable v = findVariable(frame, "array");
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
	
	public void testSetValueRange() throws Exception {
		String typeName = "ByteArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(27, typeName);
		
		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaVariable v = findVariable(frame, "bytes");
			assertNotNull(v);
			IJavaArray array = (IJavaArray) v.getValue();
			assertNotNull(array);
			IJavaValue[] replacements = new IJavaValue[5000];
			IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
			for (int i = 0; i < replacements.length; i++) {
				replacements[i] = target.newValue((byte)-1);
			}
			array.setValues(2500, 5000, replacements, 0);
			// verify new values
			IJavaValue[] values = array.getValues();
			for (int i = 0; i < values.length; i++) {
				byte byteValue = ((IJavaPrimitiveValue)values[i]).getByteValue();
				if (i < 2500) {
					assertFalse((byte)-1 == byteValue);
				} else if (i >= 7500) {
					assertFalse((byte)-1 == byteValue);
				} else {
					assertEquals((byte)-1, byteValue);
				}
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}	
	}
	
	public void testCreateArray() throws Exception {
		String typeName = "ByteArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(32, typeName);
		
		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
			IJavaVariable v = findVariable(frame, "bytes");
			assertNotNull(v);
			IJavaArrayType type = (IJavaArrayType) v.getJavaType();
			IJavaValue value = (IJavaValue) v.getValue();
			assertNotNull(value);
			// array should contain null value
			assertEquals(target.nullValue(), value);
			assertTrue(value.isNull());
			// assign a new array
			IJavaArray javaArray = type.newInstance(6000);
			v.setValue(javaArray);
			IJavaArray  array = (IJavaArray) v.getValue();
			
			IJavaValue[] replacements = new IJavaValue[6000];
			for (int i = 0; i < replacements.length; i++) {
				replacements[i] = target.newValue((byte)23);
			}
			array.setValues(replacements);
			// verify new values
			IJavaValue[] values = array.getValues();
			assertEquals(6000, array.getLength());
			for (int i = 0; i < values.length; i++) {
				byte byteValue = ((IJavaPrimitiveValue)values[i]).getByteValue();
				assertEquals((byte)23, byteValue);
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}	
	}	
	
}
