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
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.corext.refactoring.code.IntroduceParameterRefactoring;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class IntroduceParameterUnitTests extends AbstractDebugTest{

	public IntroduceParameterUnitTests(String name) {
		super(name);
	}

	public void testExtractionFromPublicType() throws Exception {
		cleanTestFiles();
				
		try {
			int lineNumber = 21;
			//create breakpoint to test
			IJavaMethodBreakpoint breakpoint = createMethodBreakpoint("a.b.c.Movee", "testMethod1", "()V", true, false);
			//refactor
			Refactoring ref = setupRefactor(lineNumber,"src","a.b.c","Movee.java");
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",methodBreakpoint.getMarker().exists());
			assertEquals("wrong method Signature", "(QString;)V", methodBreakpoint.getMethodSignature());
			assertEquals("wrong type name", "a.b.c.Movee", methodBreakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method","testMethod1",methodBreakpoint.getMethodName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testLineBreakPoint

	public void testExtractionFromNonPublicType() throws Exception {
		cleanTestFiles();
				
		try {
			int lineNumber = 40;
			//create breakpoint to test
			IJavaMethodBreakpoint breakpoint = createMethodBreakpoint("a.b.c","Movee.java","NonPublicType","nonPublicMethod","()V",true, false);
			//refactor
			Refactoring ref = setupRefactor(lineNumber,"src","a.b.c","Movee.java");
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",methodBreakpoint.getMarker().exists());
			assertEquals("wrong method Signature", "(QString;)V", methodBreakpoint.getMethodSignature());
			assertEquals("wrong type name", "a.b.c.NonPublicType", methodBreakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method","nonPublicMethod",methodBreakpoint.getMethodName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testLineBreakPoint
	
	public void testExtractionFromInternalType() throws Exception {
		cleanTestFiles();
				
		try {
			int lineNumber = 29;
			//create breakpoint to test
			IJavaMethodBreakpoint breakpoint = createMethodBreakpoint("a.b.c.Movee$InnerType", "innerTypeMethod", "()V", true, false);
			//refactor

			Refactoring ref = setupRefactor(lineNumber,"src","a.b.c","Movee.java");
			performRefactor(ref);
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",methodBreakpoint.getMarker().exists());
			assertEquals("wrong method Signature", "(QString;)V", methodBreakpoint.getMethodSignature());
			assertEquals("wrong type name", "a.b.c.Movee$InnerType", methodBreakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method","innerTypeMethod",methodBreakpoint.getMethodName());		
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testLineBreakPoint	
	
//////////////////////////////////////////////////////////////////////////////////////	
	private Refactoring setupRefactor(int lineNumber, String root, String targetPackageName, String cuName) throws Exception {
		
		IJavaProject javaProject = getJavaProject();
		ICompilationUnit cunit = getCompilationUnit(javaProject, root, targetPackageName, cuName);
		IDocument sourceCUnit = new Document(cunit.getSource());
		IRegion lineInfo = sourceCUnit.getLineInformation(lineNumber-1);//-1 Document considers lineNumber different than createBreakpoint
		int allowanceForBrackets = 4;
		int itemOffset = lineInfo.getOffset() + lineInfo.getLength() - allowanceForBrackets; 
						
		IntroduceParameterRefactoring ref= IntroduceParameterRefactoring.create(cunit,itemOffset, 0);//0, no length
		
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
