/*******************************************************************************
 *  Copyright (c) 2005, 2013 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.debug.tests.refactoring;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IReorgPolicy.IMovePolicy;
import org.eclipse.jdt.internal.corext.refactoring.reorg.JavaMoveProcessor;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgDestinationFactory;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgPolicyFactory;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
//
//then check number of and location of created breakpoint
/**
 * A set of tests which moves a CompilationUnit and verifies if
 * various breakpoints associated with that C.U. were moved.
 */
public class MoveCompilationUnitTests extends AbstractRefactoringDebugTest {

	public MoveCompilationUnitTests(String name) {
		super(name);
	}

	/**
	 * Performs a move refactoring.
	 *
	 * @param element element to move
	 * @param destination destination of move
	 * @throws Exception
	 */
	protected void move(IJavaElement element, IJavaElement destination) throws Exception {
		IMovePolicy movePolicy= ReorgPolicyFactory.createMovePolicy(
				new IResource[0],
				new IJavaElement[] {element});
		JavaMoveProcessor processor= new JavaMoveProcessor(movePolicy);
		processor.setDestination(ReorgDestinationFactory.createDestination(destination));
		processor.setReorgQueries(new MockReorgQueries());
		if(processor.canUpdateJavaReferences()) {
			processor.setUpdateReferences(true);
		}
		performRefactor(new MoveRefactoring(processor));
	}

	/**
	 * Tests if a LineBreakPoint was moved appropriately.
	 * @throws Exception
	 */
	public void testLineBreakPoint() throws Exception {
		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		try {
			int lineNumber = 21;
			//create lineBreakpoint to test
			createLineBreakpoint(lineNumber, "a.b.c.Movee");
			IPackageFragment destination= getPackageFragmentRoot(javaProject, "src").createPackageFragment("a.b", false, null);
			move(cunit, destination);
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaLineBreakpoint lineBreakpoint = (IJavaLineBreakpoint) breakpoints[0];
			assertEquals("wrong type name", "a.b.Movee", lineBreakpoint.getTypeName());
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

		try {
			//create an EntryMethod Breakpoint to test
			createMethodBreakpoint("a.b.c.Movee", "testMethod1", "()V", true, false);
			IPackageFragment destination= getPackageFragmentRoot(javaProject, "src").createPackageFragment("a.b", false, null);
			move(cunit, destination);
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertEquals("wrong type name", "a.b.Movee", methodBreakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method","testMethod1",methodBreakpoint.getMethodName());
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

		try {
			//create a watchPoint to test
			createWatchpoint("a.b.c.Movee", "anInt", true, true);
			IPackageFragment destination= getPackageFragmentRoot(javaProject, "src").createPackageFragment("a.b", false, null);
			move(cunit, destination);
			IBreakpoint[] breakPoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of watchpoints", 1, breakPoints .length);
			IJavaWatchpoint watchPoint = (IJavaWatchpoint) breakPoints [0];
			assertEquals("wrong type name", "a.b.Movee", watchPoint.getTypeName());
			assertEquals("breakpoint attached to wrong field", "anInt", watchPoint.getFieldName());
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

		try {
			//create a classLoad breakpoint to test
			createClassPrepareBreakpoint("a.b.c.Movee");
			IPackageFragment destination= getPackageFragmentRoot(javaProject, "src").createPackageFragment("a.b", false, null);
			move(cunit, destination);
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaClassPrepareBreakpoint classPrepareBreakpoint = (IJavaClassPrepareBreakpoint) breakpoints[0];
			assertEquals("wrong type name", "a.b.Movee", classPrepareBreakpoint.getTypeName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}
}
