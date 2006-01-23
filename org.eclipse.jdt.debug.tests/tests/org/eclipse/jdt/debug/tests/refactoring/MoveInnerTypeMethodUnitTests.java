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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.internal.corext.refactoring.reorg.JavaMoveProcessor;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgPolicyFactory;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IReorgPolicy.IMovePolicy;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
// 
//then check number of and location of created breakpoint
/**
 * A set of tests which moves a CompilationUnit and verifies if 
 * various breakpoints associated with that C.U. were moved. 
 */
public class MoveInnerTypeMethodUnitTests extends AbstractRefactoringDebugTest {

	public MoveInnerTypeMethodUnitTests(String name) {
		super(name);
	}
	/**
	 * Tests if a LineBreakPoint was moved appropriately.
	 * @throws Exception
	 */
	public void testLineBreakPoint() throws Exception {
		cleanTestFiles();
		IJavaProject javaProject = getJavaProject();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		IJavaElement type = cunit.getType("Movee").getType("InnerType").getMethod("innerTypeMethod", Signature.getParameterTypes("()V"));
		try {
			int lineNumber = 29;
			int newLineNumber = 20;
			//create lineBreakpoint to test
			IJavaLineBreakpoint breakpoint = createLineBreakpoint(lineNumber, "a.b.c.Movee$InnerType");
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
		cleanTestFiles();
		IJavaProject javaProject = getJavaProject();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		IJavaElement type = cunit.getType("Movee").getType("InnerType").getMethods()[0];
			
		try {
			//create an EntryMethod Breakpoint to test & do so
			IJavaMethodBreakpoint breakpoint = createMethodBreakpoint("a.b.c.Movee$InnerType", "innerTypeMethod", "()V", true, false);
			refactor(javaProject, type);	
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertEquals("wrong type name", "a.b.MoveeRecipient$InnerType", methodBreakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method","testMethod2",methodBreakpoint.getMethodName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}		
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	/** Sets up a refactoring and executes it.
	 * @param javaProject
	 * @param cunit
	 * @throws JavaModelException
	 * @throws Exception
	 */
	protected void refactor(IJavaProject javaProject, IJavaElement type) throws JavaModelException, Exception {
		JavaMoveProcessor processor = setupRefactor(javaProject, type);
		executeRefactoring(new MoveRefactoring(processor), RefactoringStatus.WARNING);
	}
	/** Configures a processor for refactoring
	 * @param javaProject
	 * @param type
	 * @return the configured processor that will be used in refactoring
	 * @throws JavaModelException
	 */
	protected JavaMoveProcessor setupRefactor(IJavaProject javaProject, IJavaElement type) throws JavaModelException {
		IMovePolicy movePolicy= ReorgPolicyFactory.createMovePolicy(
			new IResource[0], 
			new IJavaElement[] {type});
		JavaMoveProcessor processor= new JavaMoveProcessor(movePolicy);
		IJavaElement destination= getPackageFragmentRoot(javaProject, "src").getPackageFragment("a.b").getCompilationUnit("MoveeRecipient.java"); 
		processor.setDestination(destination);
		processor.setReorgQueries(new MockReorgQueries());
		if(processor.canUpdateReferences())
			processor.setUpdateReferences(true);//assuming is properly set otherwise
		return processor;
	}
	
	protected void executeRefactoring(Refactoring refactoring, int maxSeverity) throws Exception {
		PerformRefactoringOperation operation= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
		waitForBuild();
		// Flush the undo manager to not count any already existing undo objects
		// into the heap consumption
		//RefactoringCore.getUndoManager().flush();

		ResourcesPlugin.getWorkspace().run(operation, null);

		assertEquals(true, operation.getConditionStatus().getSeverity() <= maxSeverity);
		assertEquals(true, operation.getValidationStatus().isOK());

		//RefactoringCore.getUndoManager().flush();
	}

}
