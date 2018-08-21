/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
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

package org.eclipse.jdt.debug.tests.refactoring;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
//
//then check number of and location of created breakpoint
/**
 * A set of tests which moves a CompilationUnit and verifies if
 * various breakpoints associated with that C.U. were moved.
 */
public class MovePublicTypeMethodUnitTests extends MoveRefactoringTest {

	public MovePublicTypeMethodUnitTests(String name) {
		super(name);
	}
	/**
	 * Tests if a LineBreakPoint was moved appropriately.
	 * @throws Exception
	 */
	public void testLineBreakPoint() throws Exception {
		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		IJavaElement type = cunit.getType("Movee").getMethod("testMethod1", Signature.getParameterTypes("()V"));
		try {
			int lineNumber = 21;
			int newLineNumber = 19;
			//create lineBreakpoint to test
			createLineBreakpoint(lineNumber, "a.b.c.Movee");
			refactor(javaProject, type);
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaLineBreakpoint lineBreakpoint = (IJavaLineBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",lineBreakpoint.getMarker().exists());
			assertEquals("wrong type name", "a.b.MoveeRecipient", lineBreakpoint.getTypeName());
			assertEquals("wrong line number", newLineNumber, lineBreakpoint.getLineNumber());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testLineBreakPoint

	/**
	 * Tests if a MethodBreakPoint was moved appropriately.
	 * @throws Exception
	 */
	public void testMethodBreakPoint() throws Exception {
		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		IJavaElement type = cunit.getType("Movee").getMethods()[0];

		try {
			//create an EntryMethod Breakpoint to test & do so
			createMethodBreakpoint("a.b.c.Movee", "testMethod1", "()V", true, false);
			refactor(javaProject, type);
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertEquals("wrong type name", "a.b.MoveeRecipient", methodBreakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method","innerTypeMethod",methodBreakpoint.getMethodName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}
}
