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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.corext.refactoring.structure.ChangeSignatureRefactoring;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class ChangeAnonymousTypeMethodSignatureUnitTests extends AbstractDebugTest{

	public ChangeAnonymousTypeMethodSignatureUnitTests(String name) {
		super(name);
	}

	
	public void testAnonymousTypeMethodChange() throws Exception {
		cleanTestFiles();
				
		try {
			String 	src = "src", 
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$childsMethod()V$1$anonTypeMethod()QString",
					type = "MoveeChild",
					methodName = "childsMethod",
					methodSig = "()V",
					newAnonMethodName = "changedMethod",
					newAnonMethodSig = "()QObject";
			//create breakpoint to test
			IJavaMethodBreakpoint breakpoint = createMethodBreakpoint(src, pack, cunit,fullTargetName, true, false);
			//refactor
			Refactoring ref = setupRefactor(src, pack, cunit,fullTargetName);
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",methodBreakpoint.getMarker().exists());
			assertEquals("wrong method Signature", newAnonMethodSig, methodBreakpoint.getMethodSignature());
			assertEquals("wrong type name", "a.b.c.MoveeChild$1", methodBreakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method",newAnonMethodName,methodBreakpoint.getMethodName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testBreakPoint	
		
//////////////////////////////////////////////////////////////////////////////////////	
	private Refactoring setupRefactor(String root, String packageName, String cuName, String fullTargetName) throws Exception {
		
		IJavaProject javaProject = getJavaProject();
		ICompilationUnit cunit = getCompilationUnit(javaProject, root, packageName, cuName);
		IMethod method = (IMethod)(getMember(cunit,fullTargetName));
		
		ChangeSignatureRefactoring ref= ChangeSignatureRefactoring.create(method);
		//configure the ref a little more here!
		ref.setNewMethodName("changedMethod");
		ref.setNewReturnTypeName("Object");
		ref.setVisibility(Modifier.PUBLIC);

		RefactoringStatus preconditionResult= ref.checkInitialConditions(new NullProgressMonitor());
		if(!preconditionResult.isOK())
		{
			System.out.println(preconditionResult.getMessageMatchingSeverity(preconditionResult.getSeverity()));
			return null;
		}
		return ref;
	}
	
	protected final void performRefactor(final Refactoring refactoring) throws Exception {
		if(refactoring==null)
			return;
		CreateChangeOperation create= new CreateChangeOperation(refactoring);
		refactoring.checkFinalConditions(new NullProgressMonitor());
		PerformChangeOperation perform= new PerformChangeOperation(create);
		ResourcesPlugin.getWorkspace().run(perform, new NullProgressMonitor());//maybe SubPM?
		waitForBuild();
	}	
	
	protected void cleanTestFiles() throws Exception
	{
		new FileCleaner(null).cleanTestFiles();//ensure proper packages
	}
	
}
