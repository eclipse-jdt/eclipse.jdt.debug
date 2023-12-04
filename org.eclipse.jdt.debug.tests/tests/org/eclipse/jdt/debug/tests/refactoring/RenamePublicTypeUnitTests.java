/*******************************************************************************
 *  Copyright (c) 2005, 2013 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.refactoring;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeProcessor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

public class RenamePublicTypeUnitTests extends AbstractRefactoringDebugTest {

	public RenamePublicTypeUnitTests(String name) {
		super(name);
	}

	protected void runClassLoadBreakpointTest(String src, String pack, String cunit, String fullTargetName, String targetLineage) throws Exception {
		try {
			//create breakpoint to test
			IJavaClassPrepareBreakpoint breakpoint = createClassPrepareBreakpoint(src, pack, cunit, fullTargetName);
			//refactor
			Refactoring ref = setupRefactor(src, pack, cunit, fullTargetName);
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			breakpoint = (IJavaClassPrepareBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",breakpoint.getMarker().exists());
			assertEquals("breakpoint attached to wrong type", targetLineage, breakpoint.getTypeName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Creates an exception breakpoint and adds a filter. Refactors & checks
	 * if the filter changed appropriately w/ the refactor.
	 * @param src name of src file
	 * @param exceptionName TODO
	 */
	protected void runExceptionBreakpointTest(String src, String pack, String cunit, String targetName, String exceptionName) throws Exception {
		try {
			//create breakpoint to test
			IJavaExceptionBreakpoint breakpoint = createExceptionBreakpoint(exceptionName, true, true);

			//refactor
			Refactoring ref = setupRefactor(src, pack, cunit, targetName);
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			breakpoint = (IJavaExceptionBreakpoint) breakpoints[0];
			assertEquals("breakpoint attached to wrong type", exceptionName, breakpoint.getTypeName());
			assertTrue("Breakpoint Marker has ceased existing",breakpoint.getMarker().exists());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

	protected void runLineBreakpointTest(String src, String pack, String cunit, String fullTargetName, String targetLineage, int lineNumber) throws Exception {
		try {
			//create breakpoint to test
			IJavaLineBreakpoint breakpoint = createLineBreakpoint(lineNumber, src, pack, cunit, fullTargetName);
			//refactor
			Refactoring ref = setupRefactor(src, pack, cunit, fullTargetName);
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			breakpoint = (IJavaLineBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",breakpoint.getMarker().exists());
			assertEquals("breakpoint attached to wrong type", pack+"."+targetLineage, breakpoint.getTypeName());
			assertEquals("breakpoint on wrong line", lineNumber, breakpoint.getLineNumber());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

	protected void runMethodBreakpointTest(String src, String pack, String cunit, String fullTargetName, String targetLineage, String methodName) throws Exception {
		try {
			//create breakpoint to test
			IJavaMethodBreakpoint breakpoint = createMethodBreakpoint(src, pack, cunit,fullTargetName, true, false);
			//refactor
			Refactoring ref = setupRefactor(src, pack, cunit, fullTargetName);
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			breakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",breakpoint.getMarker().exists());
			assertEquals("wrong type name", targetLineage, breakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method",methodName,breakpoint.getMethodName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

	protected void runWatchPointTest(String src, String pack, String cunit, String fullTargetName, String targetLineage, String fieldName) throws Exception {
		try {
			//create breakpoint to test
			IJavaWatchpoint breakpoint = createNestedTypeWatchPoint(src, pack, cunit, fullTargetName, true, true);
			//refactor
			Refactoring ref = setupRefactor(src, pack, cunit, fullTargetName);
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			breakpoint = (IJavaWatchpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",breakpoint.getMarker().exists());
			assertEquals("breakpoint attached to wrong type", targetLineage, breakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong field", fieldName, breakpoint.getFieldName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

	/**
	 *
	 * @param type TODO
	 */
	protected Refactoring setupRefactor(String root, String packageName, String cuName, String type) throws Exception {
		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit = getCompilationUnit(javaProject, root, packageName, cuName);
		IMember target = getMember(cunit, type);
		//if this was a non-typed test, get's it's parent type
		if(!(target instanceof IType))
			target = (IMember)target.getParent();

		IType targetType = (IType)target;

		JavaRenameProcessor proc = new RenameTypeProcessor(targetType);
		proc.setNewElementName("RenamedType");

		RenameRefactoring ref= new RenameRefactoring(proc);
		//setup final refactoring conditions
		RefactoringStatus refactoringStatus= ref.checkAllConditions(new NullProgressMonitor());
		if(!refactoringStatus.isOK())
		{
			System.out.println(refactoringStatus.getMessageMatchingSeverity(refactoringStatus.getSeverity()));
			return null;
		}

		return ref;
	}

	public void testPublicClassLoadpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild",
					targetLineage = pack+"."+"RenamedType";
			runClassLoadBreakpointTest(src, pack, cunit, fullTargetName, targetLineage);
	}//end testBreakPoint

	public void testPublicLineBreakpoint() throws Exception {

			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild",
					targetLineage = "RenamedType";
			int lineNumber = 21;

			runLineBreakpointTest(src, pack, cunit, fullTargetName, targetLineage, lineNumber);
	}//end testBreakPoint

	public void testPublicMethodBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$childsMethod()V",
					targetLineage = pack+"."+"RenamedType",
					methodName = "childsMethod";
			runMethodBreakpointTest(src, pack, cunit, fullTargetName, targetLineage, methodName);
	}//end testBreakPoint

	public void testPublicExceptionBreakpoint() throws Exception {
		String 	src = "src",
				pack = "a.b.c",
				cunit = "MoveeChild.java",
				typeName = "MoveeChild",
				exceptionName = "java.lang.NullPointerException";
		runExceptionBreakpointTest(src, pack, cunit, typeName, exceptionName);
}//end testBreakPoint



	public void testPublicWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$aChildInt",
					targetLineage = pack+"."+"RenamedType",
					fieldName = "aChildInt";
			runWatchPointTest(src, pack, cunit, fullTargetName, targetLineage, fieldName);
	}//end testBreakPoint
}
