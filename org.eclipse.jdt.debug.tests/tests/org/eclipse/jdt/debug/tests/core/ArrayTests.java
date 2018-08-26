/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
		ILineBreakpoint bp = createLineBreakpoint(22, typeName);

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
		ILineBreakpoint bp = createLineBreakpoint(22, typeName);

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
		ILineBreakpoint bp = createLineBreakpoint(22, typeName);

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
		ILineBreakpoint bp = createLineBreakpoint(30, typeName);

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
		ILineBreakpoint bp = createLineBreakpoint(35, typeName);

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

	/**
	 * Sets a zero-length array as the new values
	 * @throws Exception
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=359450
	 */
	public void testSetZeroLengthArray() throws Exception {
		String typeName = "ByteArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(35, typeName);

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaVariable v = findVariable(frame, "bytes");
			assertNotNull(v);
			IJavaArrayType type = (IJavaArrayType) v.getJavaType();
			IJavaValue value = (IJavaValue) v.getValue();
			assertNotNull(value);
			IJavaArray javaArray = type.newInstance(1);
			v.setValue(javaArray);
			IJavaArray  array = (IJavaArray) v.getValue();

			IJavaValue[] replacements = new IJavaValue[0];
			array.setValues(replacements);
			// the overall size of the array will never change size, and trying to set no values has not effect
			assertEquals(1, array.getLength());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tries to set a new array with a starting index of -1
	 *
	 * @throws Exception
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=359450
	 */
	public void testSetBadLowerIndexArray() throws Exception {
		String typeName = "ByteArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(35, typeName);

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
			IJavaArray javaArray = type.newInstance(1);
			v.setValue(javaArray);
			IJavaArray  array = (IJavaArray) v.getValue();

			IJavaValue[] replacements = {target.nullValue()};
			try {
				array.setValues(-1, 0, replacements, 0);
			}
			catch(IndexOutOfBoundsException ioobe) {
				//got the expected exception
				return;
			}
			fail("Should have gotten an IndexOutOfBoundsException trying to use a offset of -1");
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tries to set an array with an index greater than the total length of the source array
	 * @throws Exception
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=359450
	 */
	public void testSetBadUpperIndexArray() throws Exception {
		String typeName = "ByteArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(35, typeName);

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaVariable v = findVariable(frame, "bytes");
			assertNotNull(v);
			IJavaArrayType type = (IJavaArrayType) v.getJavaType();
			IJavaValue value = (IJavaValue) v.getValue();
			assertNotNull(value);
			IJavaArray javaArray = type.newInstance(1);
			v.setValue(javaArray);
			IJavaArray  array = (IJavaArray) v.getValue();

			IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
			IJavaValue bite = target.newValue((byte)-1);
			IJavaValue[] replacements = {bite};
			try {
				array.setValues(3, 0, replacements, 0);
			}
			catch(IndexOutOfBoundsException ioobe) {
				//got the expected exception
				return;
			}
			fail("Should have gotten an IndexOutOfBoundsException trying to use an offset of 3");
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tries to set an array with an index greater than the total length of the soure array
	 * @throws Exception
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=359450
	 */
	public void testSetExactUpperIndexArray() throws Exception {
		String typeName = "ByteArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(35, typeName);

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaVariable v = findVariable(frame, "bytes");
			assertNotNull(v);
			IJavaArrayType type = (IJavaArrayType) v.getJavaType();
			IJavaValue value = (IJavaValue) v.getValue();
			assertNotNull(value);
			// assign a new array
			IJavaArray javaArray = type.newInstance(3);
			v.setValue(javaArray);
			IJavaArray  array = (IJavaArray) v.getValue();
			IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
			IJavaValue bite = target.newValue((byte)-1);
			IJavaValue[] replacements = {bite, bite, bite};
			try {
				array.setValues(3, 0, replacements, 0);
			}
			catch(IndexOutOfBoundsException ioobe) {
				//got the expected exception
				return;
			}
			fail("Should have gotten an IndexOutOfBoundsException trying to use an offset of 3");
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tries to set an array with a source index greater than the total length of the new values array
	 * @throws Exception
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=359450
	 */
	public void testSetBadLowerSrcIndexArray() throws Exception {
		String typeName = "ByteArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(35, typeName);

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaVariable v = findVariable(frame, "bytes");
			assertNotNull(v);
			IJavaArrayType type = (IJavaArrayType) v.getJavaType();
			IJavaValue value = (IJavaValue) v.getValue();
			assertNotNull(value);
			// assign a new array
			IJavaArray javaArray = type.newInstance(3);
			v.setValue(javaArray);
			IJavaArray  array = (IJavaArray) v.getValue();
			IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
			IJavaValue bite = target.newValue((byte)-1);
			IJavaValue[] replacements = {bite, bite, bite};
			try {
				array.setValues(0, 1, replacements, -2);
			}
			catch(IndexOutOfBoundsException ioobe) {
				//got the expected exception
				return;
			}
			fail("Should have gotten an IndexOutOfBoundsException trying to use a source offset of -2");
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tries to set an array with a source index greater than the total length of the new values array
	 * @throws Exception
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=359450
	 */
	public void testSetBadUpperSrcIndexArray() throws Exception {
		String typeName = "ByteArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(35, typeName);

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaVariable v = findVariable(frame, "bytes");
			assertNotNull(v);
			IJavaArrayType type = (IJavaArrayType) v.getJavaType();
			IJavaValue value = (IJavaValue) v.getValue();
			assertNotNull(value);
			// assign a new array
			IJavaArray javaArray = type.newInstance(3);
			v.setValue(javaArray);
			IJavaArray  array = (IJavaArray) v.getValue();
			IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
			IJavaValue bite = target.newValue((byte)-1);
			IJavaValue[] replacements = {bite, bite, bite};
			try {
				array.setValues(0, 1, replacements, 4);
			}
			catch(IndexOutOfBoundsException ioobe) {
				//got the expected exception
				return;
			}
			fail("Should have gotten an IndexOutOfBoundsException trying to use a source offset of 4");
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tries to set an array with a source index greater than the total length of the new values array
	 * @throws Exception
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=359450
	 */
	public void testSetExactSrcIndexArray() throws Exception {
		String typeName = "ByteArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(35, typeName);

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaVariable v = findVariable(frame, "bytes");
			assertNotNull(v);
			IJavaArrayType type = (IJavaArrayType) v.getJavaType();
			IJavaValue value = (IJavaValue) v.getValue();
			assertNotNull(value);
			// assign a new array
			IJavaArray javaArray = type.newInstance(3);
			v.setValue(javaArray);
			IJavaArray  array = (IJavaArray) v.getValue();
			IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
			IJavaValue bite = target.newValue((byte)-1);
			IJavaValue[] replacements = {bite, bite, bite};
			try {
				array.setValues(0, 1, replacements, 3);
			}
			catch(IndexOutOfBoundsException ioobe) {
				//got the expected exception
				return;
			}
			fail("Should have gotten an IndexOutOfBoundsException trying to use a source offset of 3");
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tries to set an array with a length less than -1
	 * @throws Exception
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=359450
	 */
	public void testSetBadLowerLengthArray() throws Exception {
		String typeName = "ByteArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(35, typeName);

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaVariable v = findVariable(frame, "bytes");
			assertNotNull(v);
			IJavaArrayType type = (IJavaArrayType) v.getJavaType();
			IJavaValue value = (IJavaValue) v.getValue();
			assertNotNull(value);
			// assign a new array
			IJavaArray javaArray = type.newInstance(3);
			v.setValue(javaArray);
			IJavaArray  array = (IJavaArray) v.getValue();
			IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
			IJavaValue bite = target.newValue((byte)-1);
			IJavaValue[] replacements = {bite, bite, bite};
			try {
				array.setValues(0, -2, replacements, 3);
			}
			catch(IndexOutOfBoundsException ioobe) {
				//got the expected exception
				return;
			}
			fail("Should have gotten an IndexOutOfBoundsException trying to use a length less than -1");
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tries to set an array with a length equal to -1
	 * @throws Exception
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=359450
	 */
	public void testSetMinus1LengthArray() throws Exception {
		String typeName = "ByteArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(35, typeName);

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaVariable v = findVariable(frame, "bytes");
			assertNotNull(v);
			IJavaArrayType type = (IJavaArrayType) v.getJavaType();
			IJavaValue value = (IJavaValue) v.getValue();
			assertNotNull(value);
			// assign a new array
			IJavaArray javaArray = type.newInstance(3);
			v.setValue(javaArray);
			IJavaArray  array = (IJavaArray) v.getValue();
			IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
			IJavaValue bite = target.newValue((byte)-1);
			IJavaValue[] replacements = {bite, bite, bite};
			try {
				array.setValues(0, -1, replacements, 0);
			}
			catch(IndexOutOfBoundsException ioobe) {
				fail("should be able to set all values passing -1 as length");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tries to set an array where the given length and index combined exceed the length of the array
	 * @throws Exception
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=359450
	 */
	public void testSetBadLengthPlusIndexArray() throws Exception {
		String typeName = "ByteArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(35, typeName);

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaVariable v = findVariable(frame, "bytes");
			assertNotNull(v);
			IJavaArrayType type = (IJavaArrayType) v.getJavaType();
			IJavaValue value = (IJavaValue) v.getValue();
			assertNotNull(value);
			// assign a new array
			IJavaArray javaArray = type.newInstance(3);
			v.setValue(javaArray);
			IJavaArray  array = (IJavaArray) v.getValue();
			IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
			IJavaValue bite = target.newValue((byte)-1);
			IJavaValue[] replacements = {bite, bite, bite};
			try {
				array.setValues(2, 2, replacements, 0);
			}
			catch(IndexOutOfBoundsException ioobe) {
				//got the expected exception
				return;
			}
			fail("Should have gotten an IndexOutOfBoundsException trying to set a combined index of 4");
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tries to set an array where the given length and source index combined exceed the length of the array
	 * @throws Exception
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=359450
	 */
	public void testSetBadLengthPlusSrcIndexArray() throws Exception {
		String typeName = "ByteArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(35, typeName);

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaVariable v = findVariable(frame, "bytes");
			assertNotNull(v);
			IJavaArrayType type = (IJavaArrayType) v.getJavaType();
			IJavaValue value = (IJavaValue) v.getValue();
			assertNotNull(value);
			IJavaArray javaArray = type.newInstance(3);
			v.setValue(javaArray);
			IJavaArray  array = (IJavaArray) v.getValue();
			IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
			IJavaValue bite = target.newValue((byte)-1);
			IJavaValue[] replacements = {bite, bite, bite};
			try {
				array.setValues(0, 2, replacements, 2);
			}
			catch(IndexOutOfBoundsException ioobe) {
				//got the expected exception
				return;
			}
			fail("Should have gotten an IndexOutOfBoundsException trying to set a combined source index of 4");
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tries to set an array where the source array is longer than the array to set the values into
	 * @throws Exception
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=359450
	 */
	public void testSetLongerSrcArray() throws Exception {
		String typeName = "ByteArrayTests";
		ILineBreakpoint bp = createLineBreakpoint(35, typeName);

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull(frame);
			IJavaVariable v = findVariable(frame, "bytes");
			assertNotNull(v);
			IJavaArrayType type = (IJavaArrayType) v.getJavaType();
			IJavaValue value = (IJavaValue) v.getValue();
			assertNotNull(value);
			IJavaArray javaArray = type.newInstance(3);
			v.setValue(javaArray);
			IJavaArray  array = (IJavaArray) v.getValue();
			IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
			IJavaValue bite = target.newValue((byte)-1);
			IJavaValue[] replacements = {bite, bite, bite, bite, bite, bite};
			try {
				array.setValues(0, -1, replacements, 0);
			}
			catch(IndexOutOfBoundsException ioobe) {
				fail("Should not have gotten an IndexOutOfBoundsException trying to set an oversized source array");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
