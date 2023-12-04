/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.model.JDIPrimitiveValue;

/**
 * Tests that the new forms allowed in literals in Java 7 work as expected
 *
 * @since 3.1.200
 */
public class LiteralTests17 extends AbstractDebugTest {

	public static final String LITERAL_TYPE_NAME = "Literals17";

	/**
	 * Constructor
	 */
	public LiteralTests17() {
		super("Tests for Java 1.7 literal support in evaluations");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.tests.AbstractDebugTest#getProjectContext()
	 */
	@Override
	protected IJavaProject getProjectContext() {
		return get17Project();
	}

	/**
	 * Perform the evaluation on the given snippet and return the value
	 * @return returns the evaluation value or <code>null</code>
	 */
	IValue doEval(String snippet) throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(28, LITERAL_TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(LITERAL_TYPE_NAME, bp);
			IEvaluationResult result = evaluate(snippet, thread);
			assertNotNull("There must be an evaluation result", result);
			assertFalse("There must be no errors in the result", result.hasErrors());
			return result.getValue();
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Runs evaluations on the two given snippets returning the value from the second snippet. This method allows us to
	 * run two snippets on the same thread where the second snippet may or may not depend on the state change from the
	 * first snippet
	 *
	 * @return the {@link IEvaluationResult}
	 */
	IValue doEval(String snippet, String snippet2) throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(28, LITERAL_TYPE_NAME);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(LITERAL_TYPE_NAME, bp);
			IEvaluationResult result = evaluate(snippet, thread);
			assertNotNull("There must be an evaluation result", result);
			assertFalse("There must be no errors in the result", result.hasErrors());
			result = evaluate(snippet2, thread);
			assertNotNull("There must be an evaluation result", result);
			assertFalse("There must be no errors in the result", result.hasErrors());
			return result.getValue();
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that an addition evaluation with an int with underscores in it works
	 */
	public void testUnderscoreIntEval() throws Exception {
		IValue value = doEval("literals.x1 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new integer value should be 11", 11, val.getIntValue());
	}

	/**
	 * Tests that we can assign a variable value to an int with underscores
	 */
	public void testUnderscoreIntVarAssignment() throws Exception {
		IValue value = doEval("literals.x1 = 1_______1;", "literals.x1 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new integer value should be 12", 12, val.getIntValue());
	}

	/**
	 * Tests that an addition evaluation with a short with underscores in it works
	 */
	public void testUnderscoreShortEval() throws Exception {
		IValue value = doEval("literals.x9 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new short value should be 11", 11, val.getShortValue());
	}

	/**
	 * Tests that we can assign a variable value to a short with underscores
	 */
	public void testUnderscoreShortVarAssignment() throws Exception {
		IValue value = doEval("literals.x9 = 1_______1;", "literals.x9 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new short value should be 12", 12, val.getShortValue());
	}

	/**
	 * Tests that an addition evaluation with a byte with underscores in it works
	 */
	public void testUnderscoreByteEval() throws Exception {
		IValue value = doEval("literals.x10 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new byte value should be 9", 9, val.getByteValue());
	}

	/**
	 * Tests that we can assign a variable value to a short with underscores
	 */
	public void testUnderscoreByteVarAssignment() throws Exception {
		IValue value = doEval("literals.x10 = 1_______1;", "literals.x10 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new byte value should be 12", 12, val.getByteValue());
	}

	/**
	 * Tests that an addition evaluation with a long with underscores in it works
	 */
	public void testUnderscoreLongEval() throws Exception {
		IValue value = doEval("literals.x8 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new long value should be 11", 11, val.getLongValue());
	}

	/**
	 * Tests that we can assign a variable value to a long with underscores
	 */
	public void testUnderscoreLongVarAssignment() throws Exception {
		IValue value = doEval("literals.x8 = 1_______1L;", "literals.x8 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new long value should be 12", 12, val.getLongValue());
	}

	/**
	 * Tests that an addition evaluation with a float with underscores in it works
	 */
	public void testUnderscoreFloatEval() throws Exception {
		IValue value = doEval("literals.x6 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new float value should be 4.1415", 4.1415F, val.getFloatValue(), .0F);
	}

	/**
	 * Tests that we can assign a variable value to a float with underscores
	 */
	public void testUnderscoreFloatVarAssignment() throws Exception {
		IValue value = doEval("literals.x6 = 6.1_4_1_5F;", "literals.x6 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new float value should be 7.1415", 7.1415F, val.getFloatValue(), .0F);
	}

	/**
	 * Tests that an addition evaluation with a double with underscores in it works
	 */
	public void testUnderscoreDoubleEval() throws Exception {
		IValue value = doEval("literals.x5 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new double value should be 11.556", 11.556D, val.getDoubleValue(), .0);
	}

	/**
	 * Tests that we can assign a variable value to a double with underscores
	 */
	public void testUnderscoreDoubleVarAssignment() throws Exception {
		IValue value = doEval("literals.x5 = 1_5.5_5_6D;", "literals.x5 + 1.000D");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new double value should be 16.556", 16.555999999999997D, val.getDoubleValue(), .0);
	}

	/**
	 * Tests that an addition evaluation with a binary literal with underscores in it works
	 */
	public void testUnderscoreBinaryEval() throws Exception {
		IValue value = doEval("literals.x4 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new integer value should be 9", 9, val.getIntValue());
	}

	/**
	 * Tests that we can assign a variable value to a binary literal with underscores
	 */
	public void testUnderscoreBinaryVarAssignment() throws Exception {
		IValue value = doEval("literals.x4 = 0b1_0_0_0_0;", "literals.x4 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new integer value should be 17", 17, val.getIntValue());
	}

	/**
	 * Tests that an addition evaluation with a hex with underscores in it works
	 */
	public void testUnderscoreHexEval() throws Exception {
		IValue value = doEval("literals.x2 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new integer value should be 17", 17, val.getIntValue());
	}

	/**
	 * Tests that we can assign a variable value to a hex with underscores
	 */
	public void testUnderscoreHexVarAssignment() throws Exception {
		IValue value = doEval("literals.x2 = 0x1_0_0;", "literals.x2 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new integer value should be 257", 257, val.getIntValue());
	}

	/**
	 * Tests that an addition evaluation with an octal with underscores in it works
	 */
	public void testUnderscoreOctEval() throws Exception {
		IValue value = doEval("literals.x3 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new integer value should be 9", 9, val.getIntValue());
	}

	/**
	 * Tests that we can assign a variable value to an octal with underscores
	 */
	public void testUnderscoreOctVarAssignment() throws Exception {
		IValue value = doEval("literals.x3 = 0_100;", "literals.x3 + 1");
		assertNotNull("The value should not be null", value);
		assertTrue("The underlying value must be a primitive value", value instanceof JDIPrimitiveValue);
		JDIPrimitiveValue val = (JDIPrimitiveValue) value;
		assertEquals("The new integer value should be 65", 65, val.getIntValue());
	}
}
