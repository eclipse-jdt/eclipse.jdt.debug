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
package org.eclipse.debug.jdi.tests;

import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.VoidType;

/**
 * Tests for JDI com.sun.jdi.Method
 * and JDWP Method command set.
 */
public class MethodTest extends AbstractJDITest {

	private Method fMethod1;
	/**
	 * Creates a new test.
	 */
	public MethodTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get method useLocalVars(Thread, MainClass)
		fMethod1 =
			getMethod(
				"useLocalVars",
				"(Ljava/lang/Thread;Lorg/eclipse/debug/jdi/tests/program/MainClass;)V");
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new MethodTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.Method";
	}
	/**
	 * Test JDI arguments() and JDWP 'Method - Get variable table'.
	 */
	public void testJDIArguments() {
		List<?> arguments = null;
		try {
			arguments = fMethod1.arguments();
		} catch (AbsentInformationException e) {
			assertTrue("1", false);
		}
		assertEquals("2", 2, arguments.size());
		assertEquals("3", "t", ((LocalVariable) arguments.get(0)).name());
		assertEquals("4", "o", ((LocalVariable) arguments.get(1)).name());
	}
	/**
	 * Test JDI argumentTypeNames().
	 */
	public void testJDIArgumentTypeNames() {
		List<?> names = fMethod1.argumentTypeNames();
		assertEquals("1", 2, names.size());
		assertEquals("2", "java.lang.Thread", names.get(0));
		assertEquals(
			"3",
			"org.eclipse.debug.jdi.tests.program.MainClass",
			names.get(1));
	}
	/**
	 * Test JDI argumentTypes().
	 */
	public void testJDIArgumentTypes() {
		List<?> types = null;
		try {
			types = fMethod1.argumentTypes();
		} catch (ClassNotLoadedException e) {
			assertTrue("1", false);
		}
		assertEquals("2", 2, types.size());
		assertEquals("3", fVM.classesByName("java.lang.Thread").get(0), types.get(0));
		assertEquals(
			"4",
			fVM.classesByName("org.eclipse.debug.jdi.tests.program.MainClass").get(0),
			types.get(1));
	}
	/**
	 * Test JDI bytecodes().
	 */
	public void testJDIBytecodes() {
		if (!fVM.canGetBytecodes())
			return;

		byte[] bytecodes = fMethod1.bytecodes();
		assertEquals("1", 27, bytecodes.length);
	}
	/**
	 * Test JDI equals() and hashCode().
	 */
	public void testJDIEquality() {
		assertTrue("1", fMethod1.equals(fMethod1));
		Method other = getMethod("run", "()V");
		assertTrue("2", !fMethod1.equals(other));
		assertTrue("3", !fMethod1.equals(new Object()));
		assertTrue("4", !fMethod1.equals(null));
		assertTrue("5", fMethod1.hashCode() != other.hashCode());
	}
	/**
	 * Test JDI isAbstract().
	 */
	public void testJDIIsAbstract() {
		assertTrue("1", !fMethod1.isAbstract());
	}
	/**
	 * Test JDI isConstructor().
	 */
	public void testJDIIsConstructor() {
		assertTrue("1", !fMethod1.isConstructor());
	}
	/**
	 * Test JDI isNative().
	 */
	public void testJDIIsNative() {
		assertTrue("1", !fMethod1.isNative());
	}
	/**
	 * Test JDI isStaticInitializer().
	 */
	public void testJDIIsStaticInitializer() {
		assertTrue("1", !fMethod1.isStaticInitializer());
	}
	/**
	 * Test JDI isSynchronized().
	 */
	public void testJDIIsSynchronized() {
		assertTrue("1", !fMethod1.isSynchronized());
	}
	/**
	 * Test JDI locationOfCodeIndex(long).
	 */
	public void testJDILocationOfCodeIndex() {
		Location expected = fMethod1.location();
		Location result = fMethod1.locationOfCodeIndex(expected.codeIndex());
		assertEquals("1", expected, result);
	}
	/**
	 * Test JDI locationsOfLine(int) and JDWP 'Method - Get line table'.
	 */
	public void testJDILocationsOfLine() {
		int expected = fMethod1.location().lineNumber();
		List<?> locations = null;
		try {
			locations = fMethod1.locationsOfLine(expected);
		} catch (AbsentInformationException e) {
			assertTrue("1", false);
		}
		assertEquals("2", 1, locations.size());
		assertEquals("3", expected, ((Location) locations.get(0)).lineNumber());
	}
	/**
	 * Test JDI returnType().
	 */
	public void testJDIReturnType() {
		try {
			assertTrue("1", fMethod1.returnType() instanceof VoidType);
		} catch (ClassNotLoadedException e) {
			assertTrue("2", false);
		}
	}
	/**
	 * Test JDI returnTypeName().
	 */
	public void testJDIReturnTypeName() {
		assertEquals("1", "void", fMethod1.returnTypeName());
	}
	/**
	 * Test JDI variables() and JDWP 'Method - Get variable table'.
	 */
	public void testJDIVariables() {
		List<?> variables = null;
		try {
			variables = fMethod1.variables();
		} catch (AbsentInformationException e) {
			assertTrue("1", false);
		}
		assertEquals("2", 2, variables.size());
		assertEquals("3", "t", ((LocalVariable) variables.get(0)).name());
		assertEquals("4", "o", ((LocalVariable) variables.get(1)).name());
	}

	/**
	 * Test JDI variables() and JDWP 'Method - Get variable table'
	 * for native method.
	 */
	public void testJDINativeMethodVariables() {
		Method method = getMethod("java.lang.Thread", "currentThread", "()Ljava/lang/Thread;");

		try {
			method.variables();
		} catch (AbsentInformationException e) {
			// since 1.4, returns an AbsentInformationException for native methods
			assertFalse("1", "1.3".equals(fVM.version()));
			return;
		} catch (NativeMethodException nme) {
			assertTrue("1", "1.3".equals(fVM.version()));
			return;
		}
		assertTrue("Should have thrown native method exception", false);
	}

	/**
	 * Test JDI variables() and JDWP 'Method - Get variable table'
	 * for non-native method with a long argument.
	 */
	public void testJDIMethodVariablesWithLong() {
		List<?> variables = null;
		Method method = getMethod("variablesTest", "(J)V");

		try {
			variables = method.variables();
		} catch (AbsentInformationException e) {
			assertTrue("1", false);
		}
		assertEquals("1", 1, variables.size());
	}

	/**
	 * Test JDI variablesByName(String) and JDWP 'Method - Get variable table'.
	 */
	public void testJDIVariablesByName() {
		String varName = "t";
		List<?> variables = null;
		try {
			variables = fMethod1.variablesByName(varName);
		} catch (AbsentInformationException e) {
			assertTrue("1", false);
		}
		assertEquals("2", 1, variables.size());
		assertEquals("3", varName, ((LocalVariable) variables.get(0)).name());
	}
}
