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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

public class PushDownMethodUnitTests extends AbstractRefactoringDebugTest {

	public PushDownMethodUnitTests(String name) {
		super(name);
	}

	public void testLineBreakPoint() throws Exception {
		try {
			int lineNumber = 29;
			//create breakpoint to test
			createLineBreakpoint(lineNumber, "a.b.c.Movee");
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
		try {
			//create Breakpoint to test
			createMethodBreakpoint("a.b.c.Movee", "testMethod1", "()V", true, false);
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


	private Refactoring setupRefactor(String parentClassName, String className, String root, String targetPackageName, String cuName) throws Exception {
		IJavaProject javaProject = get14Project();
		IType parentClas= getCompilationUnit(javaProject, root, targetPackageName, cuName).getType(parentClassName);
		IMethod clas= parentClas.getMethod(className, Signature.getParameterTypes("()V"));
        PushDownRefactoringProcessor processor = new PushDownRefactoringProcessor(new IMethod[] {clas});
        ProcessorBasedRefactoring ref= new ProcessorBasedRefactoring(processor);
		ref.checkInitialConditions(new NullProgressMonitor());

		return ref;
	}
}
