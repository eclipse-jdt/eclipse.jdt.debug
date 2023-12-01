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

package org.eclipse.debug.jdi.tests;

import static org.junit.Assert.assertNotEquals;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.LocalVariable;

/**
 * Tests for JDI com.sun.jdi.LocalVariable.
 */
public class LocalVariableTest extends AbstractJDITest {

	private LocalVariable fVar;
	/**
	 * Creates a new test.
	 */
	public LocalVariableTest() {
		super();
	}

	public LocalVariableTest(String name) {
		super(name);
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Wait for the program to be ready
		waitUntilReady();

		// Get local variable "t" in the frame running MainClass.run()
		fVar = getLocalVariable();
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new LocalVariableTest().runSuite(args);
	}
	/**
	 * Test JDI equals() and hashCode().
	 */
	public void testJDIEquality() {
		assertTrue("1", fVar.equals(fVar));
		LocalVariable other = null;
		try {
			other = getFrame(RUN_FRAME_OFFSET).visibleVariableByName("o");
		} catch (AbsentInformationException e) {
			fail("2");
		}
		assertFalse("3", fVar.equals(other));
		assertFalse("4", fVar.equals(new Object()));
		assertFalse("5", fVar.equals(null));
		assertNotEquals("6", fVar.hashCode(), other.hashCode());
	}
	/**
	 * Test JDI isArgument().
	 */
	public void testJDIIsArgument() {
		assertFalse("1", fVar.isArgument());
	}
	/**
	 * Test JDI isVisible(StackFrame).
	 */
	public void testJDIIsVisible() {
		assertTrue("1", fVar.isVisible(getFrame(RUN_FRAME_OFFSET)));

		boolean gotException = false;
		try {
			fVar.isVisible(getFrame(0));
		} catch (IllegalArgumentException e) {
			gotException = true;
		}
		assertTrue("2", gotException);
	}
	/**
	 * Test JDI name().
	 */
	public void testJDIName() {
		assertEquals("1", "t", fVar.name());
	}
	/**
	 * Test JDI signature().
	 */
	public void testJDISignature() {
		assertEquals("1", "Ljava/lang/Thread;", fVar.signature());
	}
	/**
	 * Test JDI type().
	 */
	public void testJDIType() {
		try {
			assertEquals(
				"1",
				fVM.classesByName("java.lang.Thread").get(0),
				fVar.type());
		} catch (ClassNotLoadedException e) {
			fail("2");
		}
	}
	/**
	 * Test JDI typeName().
	 */
	public void testJDITypeName() {
		assertEquals("1", "java.lang.Thread", fVar.typeName());
	}
}
