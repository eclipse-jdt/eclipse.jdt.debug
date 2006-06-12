/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
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
public class MoveFieldUnitTests extends AbstractRefactoringDebugTest {

	public MoveFieldUnitTests(String name) {
		super(name);
	}
		
	/**
	 * Tests if a WatchPointBreakPoint was moved appropriately.
	 * @throws Exception
	 */		
	public void testPublicTypeFieldMove() throws Exception {
		cleanTestFiles();
		IJavaProject javaProject = getJavaProject();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		IJavaElement type = cunit.getType("Movee").getField("anInt");
			
		try {
			//create a watchPoint to test
			IJavaWatchpoint wp = createWatchpoint("a.b.c.Movee", "anInt", true, true);
			
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
		cleanTestFiles();
		IJavaProject javaProject = getJavaProject();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		IJavaElement type = cunit.getType("Movee").getType("InnerType").getField("innerTypeInt");
			
		try {
			//create a watchPoint to test
			IJavaWatchpoint wp = createWatchpoint("a.b.c.Movee.InnerType", "innerTypeInt", true, true);
			
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
		cleanTestFiles();
		IJavaProject javaProject = getJavaProject();
		ICompilationUnit cunit= getCompilationUnit(javaProject, "src", "a.b.c", "Movee.java");
		IJavaElement type = cunit.getType("NonPublicType").getField("differentInt");
			
		try {
			//create a watchPoint to test
			IJavaWatchpoint wp = createWatchpoint("a.b.c","Movee.java", "NonPublicType", "differentInt", true, true);
			
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
