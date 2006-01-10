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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.internal.corext.refactoring.structure.PullUpRefactoring;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class PullUpMethodUnitTests extends AbstractRefactoringDebugTest {

	public PullUpMethodUnitTests(String name) {
		super(name);
	}

	public void testLineBreakPoint() throws Exception {
		cleanTestFiles();
				
		try {
			int lineNumber = 29;
			int newLineNumber = 25;
			//create breakpoint to test
			IJavaLineBreakpoint breakpoint = createLineBreakpoint(lineNumber, "a.b.c.Movee");
			//refactor
			Refactoring ref = setupRefactor("MoveeChild","childsMethod","src","a.b.c","MoveeChild.java");
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaLineBreakpoint lineBreakpoint = (IJavaLineBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",lineBreakpoint.getMarker().exists());
			assertEquals("wrong type name", "a.b.c.Movee", lineBreakpoint.getTypeName());
			assertEquals("wrong line number", newLineNumber, lineBreakpoint.getLineNumber());
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
			IJavaMethodBreakpoint breakpoint = createMethodBreakpoint("a.b.c.MoveeChild", "childMethod", "()V", true, false);
			//refactor
			Refactoring ref = setupRefactor("MoveeChild","childsMethod","src","a.b.c","MoveeChild.java");
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertEquals("wrong type name", "a.b.c.Movee", methodBreakpoint.getTypeName());
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
		ICompilationUnit cunit = getCompilationUnit(javaProject, root, targetPackageName, cuName);
		IType parentClas= cunit.getType(parentClassName);
		IMethod clas= parentClas.getMethod(className, Signature.getParameterTypes("()V"));;
		
		PullUpRefactoring ref= new PullUpRefactoring(new IMember[] {clas},JavaPreferencesSettings.getCodeGenerationSettings(javaProject));
		ref.setMethodsToDelete(new IMethod[] {clas});
		ITypeHierarchy hierarchy = parentClas.newSupertypeHierarchy(new NullProgressMonitor());
		IType inheritedType[] = hierarchy.getAllSupertypes(parentClas);
		ref.setTargetClass(inheritedType[0]);
		RefactoringStatus preconditionResult= ref.checkInitialConditions(new NullProgressMonitor());

		return ref;
	}

	protected final void performRefactor(final Refactoring refactoring) throws Exception {
		CreateChangeOperation create= new CreateChangeOperation(refactoring);
		refactoring.checkFinalConditions(new NullProgressMonitor());
		PerformChangeOperation perform= new PerformChangeOperation(create);
		ResourcesPlugin.getWorkspace().run(perform, new NullProgressMonitor());//maybe SubPM?
		waitForBuild();
	}	

}
