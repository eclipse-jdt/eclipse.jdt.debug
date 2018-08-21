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
public class MoveInnerTypeUnitTests extends MoveRefactoringTest {

	public MoveInnerTypeUnitTests(String name) {
		super(name);
	}
	/**
	 * Tests if a LineBreakPoint was moved appropriately.
	 * @throws Exception
	 */
	public void testLineBreakPoint() throws Exception {
		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		IType type = cunit.getType("Movee").getType("InnerType");
		try {
			int lineNumber = 38;
			//create lineBreakpoint to test
			createLineBreakpoint(lineNumber, "a.b.c.Movee$InnerType");
			refactor(javaProject, type);
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaLineBreakpoint lineBreakpoint = (IJavaLineBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",lineBreakpoint.getMarker().exists());
			assertEquals("wrong type name", "a.b.MoveeRecipient$InnerType", lineBreakpoint.getTypeName());
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
		IType type = cunit.getType("Movee").getType("InnerType");

		try {
			//create an EntryMethod Breakpoint to test & do so
			createMethodBreakpoint("a.b.c.Movee$InnerType", "innerTypeMethod", "()V", true, false);
			refactor(javaProject, type);
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertEquals("wrong type name", "a.b.MoveeRecipient$InnerType", methodBreakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method","innerTypeMethod",methodBreakpoint.getMethodName());
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
		IType type = cunit.getType("Movee").getType("InnerType");
		try {
			//create a watchPoint to test
			createWatchpoint("a.b.c.Movee$InnerType", "innerTypeInt", true, true);
			refactor(javaProject, type);
			IBreakpoint[] breakPoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of watchpoints", 1, breakPoints .length);
			IJavaWatchpoint watchPoint = (IJavaWatchpoint) breakPoints [0];
			assertEquals("wrong type name", "a.b.MoveeRecipient$InnerType", watchPoint.getTypeName());
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
		IType type = cunit.getType("Movee").getType("InnerType");

		try {
			//create a classLoad breakpoint to test
			createClassPrepareBreakpoint("a.b.c.Movee$InnerType");

			refactor(javaProject, type);

			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaClassPrepareBreakpoint classPrepareBreakpoint = (IJavaClassPrepareBreakpoint) breakpoints[0];
			assertEquals("wrong type name", "a.b.MoveeRecipient$InnerType", classPrepareBreakpoint.getTypeName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}
}
