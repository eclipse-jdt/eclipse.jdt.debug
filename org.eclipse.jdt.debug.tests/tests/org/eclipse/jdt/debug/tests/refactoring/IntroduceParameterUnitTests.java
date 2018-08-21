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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.internal.corext.refactoring.code.IntroduceParameterRefactoring;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class IntroduceParameterUnitTests extends AbstractRefactoringDebugTest {

	public IntroduceParameterUnitTests(String name) {
		super(name);
	}

	public void testExtractionFromPublicType() throws Exception {
		try {
			int lineNumber = 21;
			//create breakpoint to test
			createMethodBreakpoint("a.b.c.Movee", "testMethod1", "()V", true, false);
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
		try {
			int lineNumber = 40;
			//create breakpoint to test
			createMethodBreakpoint("a.b.c","Movee.java","NonPublicType","nonPublicMethod","()V",true, false);
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
		try {
			int lineNumber = 29;
			//create breakpoint to test
			createMethodBreakpoint("a.b.c.Movee$InnerType", "innerTypeMethod", "()V", true, false);
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

		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit = getCompilationUnit(javaProject, root, targetPackageName, cuName);
		IDocument sourceCUnit = new Document(cunit.getSource());
		IRegion lineInfo = sourceCUnit.getLineInformation(lineNumber-1);//-1 Document considers lineNumber different than createBreakpoint
		int allowanceForBrackets = 4;
		int itemOffset = lineInfo.getOffset() + lineInfo.getLength() - allowanceForBrackets;

		IntroduceParameterRefactoring ref= new IntroduceParameterRefactoring(cunit,itemOffset, 0);//0, no length

		RefactoringStatus preconditionResult= ref.checkInitialConditions(new NullProgressMonitor());
		if(!preconditionResult.isOK())
		{
			System.out.println(preconditionResult.getMessageMatchingSeverity(preconditionResult.getSeverity()));
			return null;
		}
		return ref;
	}
}
