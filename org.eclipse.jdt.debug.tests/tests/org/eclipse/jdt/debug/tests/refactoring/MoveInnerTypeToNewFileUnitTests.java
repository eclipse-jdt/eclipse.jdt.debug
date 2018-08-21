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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInnerToTopRefactoring;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.Refactoring;

public class MoveInnerTypeToNewFileUnitTests extends AbstractRefactoringDebugTest {

	public MoveInnerTypeToNewFileUnitTests(String name) {
		super(name);
	}

	public void testLineBreakPoint() throws Exception {
		try {
			int lineNumber = 29;
			//create breakpoint to test
			createLineBreakpoint(lineNumber, "a.b.c.Movee$InnerType");
			//refactor
			Refactoring ref = setupRefactor("Movee","InnerType","src","a.b.c","Movee.java");
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaLineBreakpoint lineBreakpoint = (IJavaLineBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",lineBreakpoint.getMarker().exists());
			assertEquals("wrong type name", "a.b.c.InnerType", lineBreakpoint.getTypeName());
			assertEquals("wrong line number", lineNumber, lineBreakpoint.getLineNumber());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testBreakPoint


	public void testMethodBreakPoint() throws Exception {
		try {
			//create Breakpoint to test
			createMethodBreakpoint("a.b.c.Movee$InnerType", "innerTypeMethod", "()V", true, false);
			//refactor
			Refactoring ref = setupRefactor("Movee","InnerType","src","a.b.c","Movee.java");
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertEquals("wrong type name", "a.b.MoveeRecipient$InnerType", methodBreakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method","innerTypeMethod",methodBreakpoint.getMethodName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testBreakPoint

	public void testWatchPoint() throws Exception {
		try {
			//create Breakpoint to test
			createWatchpoint("a.b.c.Movee$InnerType", "innerTypeInt", true, true);
			//refactor
			Refactoring ref = setupRefactor("Movee","InnerType","src","a.b.c","Movee.java");
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakPoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of watchpoints", 1, breakPoints .length);
			IJavaWatchpoint watchPoint = (IJavaWatchpoint) breakPoints [0];
			assertEquals("wrong type name", "a.b.MoveeRecipient$InnerType", watchPoint.getTypeName());
			assertEquals("breakpoint attached to wrong field", "differentInt", watchPoint.getFieldName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testBreakPoint

	public void testClassLoadBreakPoint() throws Exception {
		try {
			//create Breakpoint to test
			createClassPrepareBreakpoint("a.b.c.Movee$InnerType");
			//refactor
			Refactoring ref = setupRefactor("Movee","InnerType","src","a.b.c","Movee.java");
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaClassPrepareBreakpoint classPrepareBreakpoint = (IJavaClassPrepareBreakpoint) breakpoints[0];
			assertEquals("wrong type name", "a.b.MoveeRecipient$InnerType", classPrepareBreakpoint.getTypeName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testBreakPoint

	private Refactoring setupRefactor(String parentClassName, String className, String root, String targetPackageName, String cuName) throws Exception {
		IJavaProject javaProject = get14Project();
		IType parentClas= getCompilationUnit(javaProject, root, targetPackageName, cuName).getType(parentClassName);
		IType clas= parentClas.getType(className);

		MoveInnerToTopRefactoring ref= new MoveInnerToTopRefactoring(clas, JavaPreferencesSettings.getCodeGenerationSettings(clas.getJavaProject()));
		ref.checkInitialConditions(new NullProgressMonitor());

		return ref;
	}
}
