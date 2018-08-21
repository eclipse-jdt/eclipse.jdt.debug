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
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
//
//then check number of and location of created breakpoint
/**
 * A set of tests which moves a CompilationUnit and verifies if
 * various breakpoints associated with that C.U. were moved.
 */
public class MoveFieldUnitTests extends MoveRefactoringTest {

	public MoveFieldUnitTests(String name) {
		super(name);
	}

	/**
	 * Tests if a WatchPointBreakPoint was moved appropriately.
	 * @throws Exception
	 */
	public void testPublicTypeFieldMove() throws Exception {
		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		IJavaElement type = cunit.getType("Movee").getField("anInt");

		try {
			//create a watchPoint to test
			createWatchpoint("a.b.c.Movee", "anInt", true, true);

			refactor(javaProject, type);

			IBreakpoint[] breakPoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of watchpoints", 1, breakPoints .length);
			IJavaWatchpoint watchPoint = (IJavaWatchpoint) breakPoints [0];
			assertEquals("wrong type name", "a.b.MoveeReciepient", watchPoint.getTypeName());
			assertEquals("breakpoint attached to wrong field", "anInt", watchPoint.getFieldName());
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
	public void testInnerTypeFieldMove() throws Exception {
		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		IJavaElement type = cunit.getType("Movee").getType("InnerType").getField("innerTypeInt");

		try {
			//create a watchPoint to test
			createWatchpoint("a.b.c.Movee.InnerType", "innerTypeInt", true, true);

			refactor(javaProject, type);

			IBreakpoint[] breakPoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of watchpoints", 1, breakPoints .length);
			IJavaWatchpoint watchPoint = (IJavaWatchpoint) breakPoints [0];
			assertEquals("wrong type name", "a.b.MoveeReciepient", watchPoint.getTypeName());
			assertEquals("breakpoint attached to wrong field", "innerTypeInt", watchPoint.getFieldName());
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
	public void testNonPublicTypeFieldMove() throws Exception {
		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		IJavaElement type = cunit.getType("NonPublicType").getField("differentInt");

		try {
			//create a watchPoint to test
			createWatchpoint("a.b.c","Movee.java", "NonPublicType", "differentInt", true, true);

			refactor(javaProject, type);

			IBreakpoint[] breakPoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of watchpoints", 1, breakPoints .length);
			IJavaWatchpoint watchPoint = (IJavaWatchpoint) breakPoints [0];
			assertEquals("wrong type name", "a.b.MoveeReciepient", watchPoint.getTypeName());
			assertEquals("breakpoint attached to wrong field", "differentInt", watchPoint.getFieldName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}
}
