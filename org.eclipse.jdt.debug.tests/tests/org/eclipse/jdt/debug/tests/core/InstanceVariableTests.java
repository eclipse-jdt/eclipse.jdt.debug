/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class InstanceVariableTests extends AbstractDebugTest {

	public InstanceVariableTests(String name) {
		super(name);
	}

	public void testGetField() throws Exception {
		String typeName = "InstanceVariablesTests";

		ILineBreakpoint bp = createLineBreakpoint(33, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable ivt = findVariable(frame, "ivt");
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

	public void testGetDeclaredFieldNames() throws Exception {
		String typeName = "InstanceVariablesTests";

		ILineBreakpoint bp = createLineBreakpoint(31, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IJavaObject object = frame.getThis();
			assertNotNull("'this' is null", object);

			String[] names = ((IJavaReferenceType)object.getJavaType()).getDeclaredFieldNames();
			assertEquals("Should be 7 declared fields", 7, names.length);
			List<String> fields = new ArrayList<>();
			for (int i = 0; i < names.length; i++) {
				String string = names[i];
				fields.add(string);
			}
			assertTrue("Missing 'pubStr'", fields.indexOf("pubStr") >= 0);
			assertTrue("Missing 'protStr'", fields.indexOf("protStr") >= 0);
			assertTrue("Missing 'defStr'", fields.indexOf("defStr") >= 0);
			assertTrue("Missing 'privStr'", fields.indexOf("privStr") >= 0);
			assertTrue("Missing 'nullStr'", fields.indexOf("nullStr") >= 0);
			assertTrue("Missing 'date'", fields.indexOf("date") >= 0);
			assertTrue("Missing 'nullDate'", fields.indexOf("nullDate") >= 0);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testGetDeclaredFieldNamesInSubclass() throws Exception {
		String typeName = "InstanceVariablesTests";

		ILineBreakpoint bp = createLineBreakpoint(33, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable ivt = findVariable(frame, "ivt");
			assertNotNull("Could not find variable 'ivt'", ivt);
			IJavaObject object = (IJavaObject)ivt.getValue();

			String[] names = ((IJavaReferenceType)object.getJavaType()).getDeclaredFieldNames();
			assertEquals("Should be 3 declared fields", 3, names.length);
			List<String> fields = new ArrayList<>();
			for (int i = 0; i < names.length; i++) {
				String string = names[i];
				fields.add(string);
			}
			assertTrue("Missing 'pubStr'", fields.indexOf("pubStr") >= 0);
			assertTrue("Missing 'protStr'", fields.indexOf("protStr") >= 0);
			assertTrue("Missing 'defStr'", fields.indexOf("defStr") >= 0);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testGetAllFieldNamesInSubclass() throws Exception {
		String typeName = "InstanceVariablesTests";

		ILineBreakpoint bp = createLineBreakpoint(33, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable ivt = findVariable(frame, "ivt");
			assertNotNull("Could not find variable 'ivt'", ivt);
			IJavaObject object = (IJavaObject)ivt.getValue();

			String[] names = ((IJavaReferenceType)object.getJavaType()).getAllFieldNames();
			assertTrue("Should be at least 10 fields", names.length >= 10);
			// note: can be > 10 if Object defines any inst vars (depends on class libs)
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testEvaluationAssignments() throws Exception {
		String typeName = "InstanceVariablesTests";

		ILineBreakpoint bp = createLineBreakpoint(31, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable pubStr = findVariable(frame, "pubStr");
			assertNotNull("Could not find variable 'pubStr'", pubStr);
			assertEquals("'pubStr' value should be 'public'", "public", pubStr.getValue().getValueString());

			evaluate("pubStr = \"hello\";", frame);
			// the value should have changed
			assertEquals("'pubStr' value should be 'hello'", "hello", pubStr.getValue().getValueString());

			evaluate("pubStr = null;", frame);
			// the value should have changed
			assertEquals("'pubStr' value should be 'null'", ((IJavaDebugTarget)frame.getDebugTarget()).nullValue(), pubStr.getValue());
			assertTrue(((IJavaValue)pubStr.getValue()).isNull());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testValueHasChanged() throws Exception {
		String typeName = "VariableChanges";

		ILineBreakpoint bp = createLineBreakpoint(24, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable count = findVariable(frame, "count");
			IVariable i = findVariable(frame, "i");

			assertNotNull("could not find variable 'count'", count);
			assertNotNull("could not find variable 'i'", i);

			// on first suspend, change should be false
			count.getValue();
			i.getValue();
			assertFalse("count should not have changed", count.hasValueChanged());
			assertFalse("i should not have changed", i.hasValueChanged());

			// after a step over "count++" it should have changed
			stepOver(frame);

			// count should have changed, and i should not
			count.getValue();
			i.getValue();
			assertTrue("count should have changed value", count.hasValueChanged());
			assertFalse("i should still be the same", i.hasValueChanged());

			stepOver(frame);

			// now count should be the same, and i should have changed
			count.getValue();
			i.getValue();
			assertFalse("count should not have changed", count.hasValueChanged());
			assertTrue("i should have changd", i.hasValueChanged());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
