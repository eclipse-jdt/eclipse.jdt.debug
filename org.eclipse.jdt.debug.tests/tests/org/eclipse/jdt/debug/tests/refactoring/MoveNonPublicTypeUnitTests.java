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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
//
//then check number of and location of created breakpoint
/**
 * A set of tests which moves a CompilationUnit and verifies if
 * various breakpoints associated with that C.U. were moved.
 */
public class MoveNonPublicTypeUnitTests extends MoveRefactoringTest {

	public MoveNonPublicTypeUnitTests(String name) {
		super(name);
	}
	/**
	 * Tests if a LineBreakPoint was moved appropriately.
	 * @throws Exception
	 */
	public void testLineBreakPoint() throws Exception {
		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		IType type = cunit.getType("NonPublicType");
		try {
			int lineNumber = 31;
			//create lineBreakpoint to test
			createLineBreakpoint(lineNumber,"a.b.c","Movee.java","NonPublicType");
			refactor(javaProject, type);
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaLineBreakpoint lineBreakpoint = (IJavaLineBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",lineBreakpoint.getMarker().exists());
			assertEquals("wrong type name", "a.b.MoveeRecipient", lineBreakpoint.getTypeName());
			assertEquals("wrong line number", lineNumber, lineBreakpoint.getLineNumber());
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
		IType type = cunit.getType("NonPublicType");

		try {
			//create an EntryMethod Breakpoint to test & do so
			createMethodBreakpoint("a.b.c","Movee.java","NonPublicType","nonPublicMethod","()V",true, false);
			refactor(javaProject, type);
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertEquals("wrong type name", "a.b.MoveeRecipient", methodBreakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method","nonPublicMethod",methodBreakpoint.getMethodName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests if a WatchPointBreakPoint was moved appropriately.
	 * @throws Exception
	 */
	public void testWatchPointBreakPoint() throws Exception {
		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		IType type = cunit.getType("NonPublicType");
		try {
			//create a watchPoint to test
			createNestedTypeWatchPoint("src", "a.b.c", "Movee.java", "NonPublicType$differentInt", true, true);

			refactor(javaProject, type);

			IBreakpoint[] breakPoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of watchpoints", 1, breakPoints .length);
			IJavaWatchpoint watchPoint = (IJavaWatchpoint) breakPoints [0];
			assertEquals("wrong type name", "a.b.MoveeRecipient", watchPoint.getTypeName());
			assertEquals("breakpoint attached to wrong field", "differentInt", watchPoint.getFieldName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests if a ClassLoadBreakPoint was moved appropriately.
	 * @throws Exception
	 */
	public void testClassLoadBreakPoint() throws Exception {
		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		IType type = cunit.getType("NonPublicType");

		try {
			//create a classLoad breakpoint to test
			createClassPrepareBreakpoint("a.b.c","Movee.java","NonPublicType");

			refactor(javaProject, type);

			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaClassPrepareBreakpoint classPrepareBreakpoint = (IJavaClassPrepareBreakpoint) breakpoints[0];
			assertEquals("wrong type name", "a.b.MoveeRecipient", classPrepareBreakpoint.getTypeName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}
}
