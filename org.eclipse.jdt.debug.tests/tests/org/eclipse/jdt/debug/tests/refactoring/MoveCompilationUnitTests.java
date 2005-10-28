/******************************************************************************* 
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.debug.tests.refactoring;

import java.util.HashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.corext.refactoring.reorg.JavaMoveProcessor;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
// 
//then check number of and location of created breakpoint
/**
 * A set of tests which moves a CompilationUnit and verifies if 
 * various breakpoints associated with that C.U. were moved. 
 */
public class MoveCompilationUnitTests extends AbstractDebugTest {

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
		JavaMoveProcessor processor= JavaMoveProcessor.create(
				new IResource[0], 
				new IJavaElement[] {element}); 
		processor.setDestination(destination);
		processor.setReorgQueries(new MockReorgQueries());
		if(processor.canUpdateReferences()) {
			processor.setUpdateReferences(true);
		}
		executeRefactoring(new MoveRefactoring(processor), RefactoringStatus.WARNING);			
	}
	
	/**
	 * Tests if a LineBreakPoint was moved appropriately.
	 * @throws Exception
	 */
	public void testLineBreakPoint() throws Exception {
		cleanTestFiles();
		IJavaProject javaProject = getJavaProject();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		try {
			int lineNumber = 21;
			//create lineBreakpoint to test
			IJavaLineBreakpoint breakpoint = createLineBreakpoint(lineNumber, "a.b.c.Movee");
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
		cleanTestFiles();
		IJavaProject javaProject = getJavaProject();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
				
		try {
			//create an EntryMethod Breakpoint to test
			IJavaMethodBreakpoint breakpoint = createMethodBreakpoint("a.b.c.Movee", "testMethod1", "()V", true, false);
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
		cleanTestFiles();
		IJavaProject javaProject = getJavaProject();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
				
		try {
			//create a watchPoint to test
			IJavaWatchpoint wp = createWatchpoint("a.b.c.Movee", "anInt", true, true);
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
		cleanTestFiles();
		IJavaProject javaProject = getJavaProject();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
				
		try {
			//create a classLoad breakpoint to test
			java.util.Map map = new HashMap();
			IResource projResource = javaProject.getResource();
			IJavaClassPrepareBreakpoint breakpoint = createClassPrepareBreakpoint("a.b.c.Movee");
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
	
	protected void executeRefactoring(Refactoring refactoring, int maxSeverity) throws Exception {
		PerformRefactoringOperation operation= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
		waitForBuild();
		// Flush the undo manager to not count any already existing undo objects
		// into the heap consumption
		RefactoringCore.getUndoManager().flush();

		ResourcesPlugin.getWorkspace().run(operation, null);

		assertEquals(true, operation.getConditionStatus().getSeverity() <= maxSeverity);
		assertEquals(true, operation.getValidationStatus().isOK());

		RefactoringCore.getUndoManager().flush();
	}
		
	/**
	 * Replaces the Movee.java file with a clean copy with which to continue tests 
	 * from a src file.
	 */
	protected void cleanTestFiles() throws Exception
	{
		new FileCleaner(null).cleanTestFiles();//ensure proper packages
	}
}
