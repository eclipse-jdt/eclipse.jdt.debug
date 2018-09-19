/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.debug.jdi.tests;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

/**
 * Tests for JDI com.sun.jdi.StackFrame.
 */
public class StackFrameTest extends AbstractJDITest {

	private StackFrame fFrame;
	/**
	 * Creates a new test.
	 */
	public StackFrameTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get the frame running MainClass.run()
		fFrame = getFrame(RUN_FRAME_OFFSET);

	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new StackFrameTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.StackFrame";
	}
	/**
	 * Test JDI equals() and hashCode().
	 */
	public void testJDIEquality() {
		StackFrame sameFrame = getFrame(RUN_FRAME_OFFSET);
		StackFrame otherFrame = getFrame(0);

		// Not identical
		assertTrue("1", fFrame != sameFrame);
		// But equal
		assertTrue("2", fFrame.equals(sameFrame));
		assertTrue("3", fFrame.hashCode() == sameFrame.hashCode());

		assertTrue("4", fFrame.equals(fFrame));
		assertTrue("5", fFrame.hashCode() == fFrame.hashCode());

		assertTrue("6", !fFrame.equals(otherFrame));

		assertTrue("7", !fFrame.equals(new Object()));
		assertTrue("8", !fFrame.equals(null));
	}
	/**
	 * Test JDI location().
	 */
	public void testJDILocation() {
		assertNotNull("1", fFrame.location());
	}
	/**
	 * Test JDI setValue(LocalVariable, Value), getValue(LocalVariable) and
	 * getValues(List).
	 */
	public void testJDISetGetValue() {
		// setValue
		ThreadReference thread = fVM.allThreads().get(0);
		LocalVariable var = getLocalVariable();
		try {
			fFrame.setValue(var, thread);
		} catch (ClassNotLoadedException e) {
			assertTrue("1.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("1.2", false);
		}

		// getValue(LocalVariable)
		ThreadReference value =
			(ThreadReference) fFrame.getValue(getLocalVariable());
		assertEquals("2", thread, value);

		// getValues(List)
		List<LocalVariable> vars = new LinkedList<>();
		vars.add(var);
		Map<?, ?> values = fFrame.getValues(vars);
		value = (ThreadReference) values.get(var);
		assertEquals("3", thread, value);

		// test null value
		var = getLocalVariable();
		try {
			fFrame.setValue(var, null);
		} catch (ClassNotLoadedException e) {
			assertTrue("4.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("4.2", false);
		}

		value = (ThreadReference) fFrame.getValue(getLocalVariable());
		assertEquals("5", null, value);

	}
	/**
	 * Test JDI thisObject().
	 */
	public void testJDIThisObject() {
		ObjectReference object = fFrame.thisObject();
		ReferenceType expected = getMainClass();
		ReferenceType referenceType = object.referenceType();
		assertEquals("1", expected, referenceType);
	}
	/**
	 * Test JDI thread().
	 */
	public void testJDIThread() {
		assertEquals("1", getThread(), fFrame.thread());
	}
	/**
	 * Test JDI visibleVariableByName(String).
	 */
	public void testJDIVisibleVariableByName() {
		LocalVariable var = null;
		try {
			var = fFrame.visibleVariableByName("t");
		} catch (AbsentInformationException e) {
			assertTrue("1", false);
		}
		assertEquals("2", getLocalVariable(), var);
		try {
			var = fFrame.visibleVariableByName("bogus");
		} catch (AbsentInformationException e) {
			assertTrue("3", false);
		}
		assertTrue("4", null == var);
	}
	/**
	 * Test JDI visibleVariables().
	 */
	public void testJDIVisibleVariables() {
		List<?> vars = null;
		try {
			vars = fFrame.visibleVariables();
		} catch (AbsentInformationException e) {
			assertTrue("1", false);
		}
		assertEquals("2", 2, vars.size());

		LocalVariable var;
		int i = 0;
		do {
			var = (LocalVariable) vars.get(i++);
		} while (!var.name().equals("t"));
		assertEquals("3", getLocalVariable(), var);
	}
}
