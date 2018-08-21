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
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class ExtractMethodUnitTests extends AbstractRefactoringDebugTest {

	public ExtractMethodUnitTests(String name) {
		super(name);
	}

	public void testExtractionFromPublicType() throws Exception {
		try {
			int lineNumber = 21;
			int newLineNumber = 25;
			//create breakpoint to test
			createLineBreakpoint(lineNumber, "a.b.c.Movee");
			//refactor

			Refactoring ref = setupRefactor(lineNumber,"src","a.b.c","Movee.java");
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

	public void testExtractionFromNonPublicType() throws Exception {
		try {
			int lineNumber = 40;
			int newLineNumber = 44;
			//create breakpoint to test
			createLineBreakpoint(lineNumber,"a.b.c","Movee.java","NonPublicType");
			//refactor

			Refactoring ref = setupRefactor(lineNumber,"src","a.b.c","Movee.java");
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaLineBreakpoint lineBreakpoint = (IJavaLineBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",lineBreakpoint.getMarker().exists());
			assertEquals("wrong type name", "a.b.c.NonPublicType", lineBreakpoint.getTypeName());
			assertEquals("wrong line number", newLineNumber, lineBreakpoint.getLineNumber());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testLineBreakPoint

	public void testExtractionFromInternalType() throws Exception {
		try {
			int lineNumber = 29;
			int newLineNumber = 32;
			//create breakpoint to test
			createLineBreakpoint(lineNumber, "a.b.c.Movee.InnerType");
			//refactor

			Refactoring ref = setupRefactor(lineNumber,"src","a.b.c","Movee.java");
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaLineBreakpoint lineBreakpoint = (IJavaLineBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",lineBreakpoint.getMarker().exists());
			assertEquals("wrong type name", "a.b.c.Movee$InnerType", lineBreakpoint.getTypeName());
			assertEquals("wrong line number", newLineNumber, lineBreakpoint.getLineNumber());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testLineBreakPoint


/////////////////////////////////////////

	private Refactoring setupRefactor(int lineNumber, String root, String targetPackageName, String cuName) throws Exception {

		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit = getCompilationUnit(javaProject, root, targetPackageName, cuName);

		IDocument sourceCUnit = new Document(cunit.getSource());
		IRegion lineInfo = sourceCUnit.getLineInformation(lineNumber-1);//-1 Document considers lineNumber different than createBreakpoint

		ExtractMethodRefactoring ref= new ExtractMethodRefactoring(cunit,lineInfo.getOffset(), lineInfo.getLength());

		RefactoringStatus preconditionResult= ref.checkInitialConditions(new NullProgressMonitor());
		if(!preconditionResult.isOK())
		{
			System.out.println(preconditionResult.getMessageMatchingSeverity(preconditionResult.getSeverity()));
			return null;
		}
		return ref;
	}
}
