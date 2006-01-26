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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class PushDownMethodUnitTests extends AbstractRefactoringDebugTest {

	public PushDownMethodUnitTests(String name) {
		super(name);
	}

	public void testLineBreakPoint() throws Exception {
		cleanTestFiles();
				
		try {
			int lineNumber = 29;
			//create breakpoint to test
			IJavaLineBreakpoint breakpoint = createLineBreakpoint(lineNumber, "a.b.c.Movee");
			//refactor
			Refactoring ref = setupRefactor("Movee","testMethod1","src","a.b.c","Movee.java");
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaLineBreakpoint lineBreakpoint = (IJavaLineBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",lineBreakpoint.getMarker().exists());
			assertEquals("wrong type name", "a.b.c.MoveeChild", lineBreakpoint.getTypeName());
			assertEquals("wrong line number", lineNumber, lineBreakpoint.getLineNumber());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testLineBreakPoint
	
	
	public void testMethodBreakPoint() throws Exception {
		cleanTestFiles();
		
		try {
			//create Breakpoint to test
			IJavaMethodBreakpoint breakpoint = createMethodBreakpoint("a.b.c.Movee", "testMethod1", "()V", true, false);
			//refactor
			Refactoring ref = setupRefactor("Movee","testMethod1","src","a.b.c","Movee.java");
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertEquals("wrong type name", "a.b.c.MoveeChild", methodBreakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method","testMethod1",methodBreakpoint.getMethodName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testBreakPoint
		
	
/////////////////////////////////////////
	
	private Refactoring setupRefactor(String parentClassName, String className, String root, String targetPackageName, String cuName) throws Exception {
		
		IJavaProject javaProject = getJavaProject();
		IType parentClas= getCompilationUnit(javaProject, root, targetPackageName, cuName).getType(parentClassName);
		IMethod clas= parentClas.getMethod(className, Signature.getParameterTypes("()V"));;
		
        PushDownRefactoringProcessor processor = new PushDownRefactoringProcessor(new IMethod[] {clas});
		PushDownRefactoring ref= new PushDownRefactoring(processor);
		RefactoringStatus preconditionResult= ref.checkInitialConditions(new NullProgressMonitor());

		return ref;
	}

	protected final void performRefactor(final Refactoring refactoring) throws Exception {
		CreateChangeOperation create= new CreateChangeOperation(refactoring);
		refactoring.checkFinalConditions(new NullProgressMonitor());
		PerformChangeOperation perform= new PerformChangeOperation(create);
		ResourcesPlugin.getWorkspace().run(perform, new NullProgressMonitor());
		waitForBuild();
	}	

}
