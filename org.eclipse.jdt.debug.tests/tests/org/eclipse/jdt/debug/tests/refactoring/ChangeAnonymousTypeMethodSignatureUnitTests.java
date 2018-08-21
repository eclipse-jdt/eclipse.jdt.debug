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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.internal.corext.refactoring.structure.ChangeSignatureProcessor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

public class ChangeAnonymousTypeMethodSignatureUnitTests extends AbstractRefactoringDebugTest {

	public ChangeAnonymousTypeMethodSignatureUnitTests(String name) {
		super(name);
	}


	public void testAnonymousTypeMethodChange() throws Exception {
		try {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$childsMethod()V$1$anonTypeMethod()QString",
					newAnonMethodName = "changedMethod",
					newAnonMethodSig = "()QObject";
			//create breakpoint to test
			createMethodBreakpoint(src, pack, cunit,fullTargetName, true, false);
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

	private Refactoring setupRefactor(String root, String packageName, String cuName, String fullTargetName) throws Exception {
		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit = getCompilationUnit(javaProject, root, packageName, cuName);
		IMethod method = (IMethod)(getMember(cunit,fullTargetName));

        ChangeSignatureProcessor processor = new ChangeSignatureProcessor(method);
        ProcessorBasedRefactoring ref= new ProcessorBasedRefactoring(processor);

        //configure the processor a little more here!
        processor.setNewMethodName("changedMethod");
        processor.setNewReturnTypeName("Object");
        processor.setVisibility(Modifier.PUBLIC);

		RefactoringStatus preconditionResult= ref.checkInitialConditions(new NullProgressMonitor());
		if(!preconditionResult.isOK())
		{
			System.out.println(preconditionResult.getMessageMatchingSeverity(preconditionResult.getSeverity()));
			return null;
		}
		return ref;
	}
}
