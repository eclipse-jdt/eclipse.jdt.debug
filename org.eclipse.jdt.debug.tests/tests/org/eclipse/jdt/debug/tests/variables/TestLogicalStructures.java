/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.variables;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILogicalStructureType;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests for logical structures
 */
public class TestLogicalStructures extends AbstractDebugTest {

	/**
	 * Constructs test.
	 *
	 * @param name test name
	 */
	public TestLogicalStructures(String name) {
		super(name);
	}

	/**
	 * Test the logical structure for a list.
	 *
	 * @throws Exception
	 */
	public void testListLogicalStructure() throws Exception {
		String typeName = "LogicalStructures";
		createLineBreakpoint(36, typeName);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);

			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull("missing top frame", frame);

			IJavaVariable variable = frame.findVariable("list");
			assertNotNull("Missing variable 'list'", variable);

			IValue value = variable.getValue();
			ILogicalStructureType[] types = DebugPlugin.getLogicalStructureTypes(value);
			assertEquals("Should be one logical structure type", 1, types.length);

			IJavaObject logicalValue = (IJavaObject) types[0].getLogicalStructure(value);
			assertEquals("Logical value should be an array", "java.lang.Object[]", logicalValue.getJavaType().getName());

			IJavaArray array = (IJavaArray) logicalValue;
			assertEquals("Should be two elements in the structure", 2, array.getLength());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Test the logical structure for a map.
	 *
	 * @throws Exception
	 */
	public void testMapLogicalStructure() throws Exception {
		String typeName = "LogicalStructures";
		createLineBreakpoint(33, typeName);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);

			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull("missing top frame", frame);

			IJavaVariable variable = frame.findVariable("map");
			assertNotNull("Missing variable 'map'", variable);

			IValue value = variable.getValue();
			ILogicalStructureType[] types = DebugPlugin.getLogicalStructureTypes(value);
			assertEquals("Should be one logical structure type", 1, types.length);

			IJavaObject logicalValue = (IJavaObject) types[0].getLogicalStructure(value);
			assertEquals("Logical value should be an array", "java.lang.Object[]", logicalValue.getJavaType().getName());

			IJavaArray array = (IJavaArray) logicalValue;
			assertEquals("Should be two elements in the structure", 2, array.getLength());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Test the logical structure for a map entry.
	 *
	 * @throws Exception
	 */
	public void testEntryLogicalStructure() throws Exception {
		String typeName = "LogicalStructures";
		createLineBreakpoint(36, typeName);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);

			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull("missing top frame", frame);

			IJavaVariable variable = frame.findVariable("entry");
			assertNotNull("Missing variable 'entry'", variable);

			IValue value = variable.getValue();
			ILogicalStructureType[] types = DebugPlugin.getLogicalStructureTypes(value);
			assertEquals("Should be one logical structure type", 1, types.length);

			IJavaObject logicalValue = (IJavaObject) types[0].getLogicalStructure(value);
			IVariable[] children = logicalValue.getVariables();
			assertEquals("Should be two elements in the structure", 2, children.length);
			assertEquals("First entry should be key", "key", children[0].getName());
			assertEquals("Second entry should be value", "value", children[1].getName());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
