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
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameCompilationUnitProcessor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

public class RenameCompilationUnitUnitTests extends AbstractRefactoringDebugTest {

	public RenameCompilationUnitUnitTests(String name) {
		super(name);
	}

	/**
	 * @param src
	 * @param pack
	 * @param cunit
	 * @param fullTargetName
	 * @param newTargetLineage
	 * @throws Exception
	 */
	private void runClassLoadBreakpointTest(String src, String pack, String cunit, String fullTargetName, String newTargetLineage) throws Exception {
		try {
			//create breakpoint to test
			IJavaClassPrepareBreakpoint breakpoint = createClassPrepareBreakpoint(src, pack, cunit, fullTargetName);
			//refactor
			Refactoring ref = setupRefactor(src, pack, cunit);
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			breakpoint = (IJavaClassPrepareBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",breakpoint.getMarker().exists());
			assertEquals("breakpoint attached to wrong type", newTargetLineage, breakpoint.getTypeName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * @param src
	 * @param pack
	 * @param cunit
	 * @param fullTargetName
	 * @param targetsParentName
	 * @param lineNumber
	 * @throws Exception
	 */
	private void runLineBreakpointTest(String src, String pack, String cunit, String fullTargetName, String targetsParentName, int lineNumber) throws Exception {
		try {
			//create breakpoint to test
			IJavaLineBreakpoint breakpoint = createLineBreakpoint(lineNumber, src, pack, cunit, fullTargetName);
			//refactor
			Refactoring ref = setupRefactor(src, pack, cunit);
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			breakpoint = (IJavaLineBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",breakpoint.getMarker().exists());
			assertEquals("breakpoint attached to wrong type", targetsParentName, breakpoint.getTypeName());
			assertEquals("breakpoint on wrong line", lineNumber, breakpoint.getLineNumber());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * @param src
	 * @param pack
	 * @param cunit
	 * @param fullTargetName
	 * @param newTargetLineage
	 * @param methodName
	 * @throws Exception
	 */
	private void runMethodBreakpointTest(String src, String pack, String cunit, String fullTargetName, String newTargetLineage, String methodName) throws Exception {
		try {
			//create breakpoint to test
			IJavaMethodBreakpoint breakpoint = createMethodBreakpoint(src, pack, cunit,fullTargetName, true, false);
			//refactor
			Refactoring ref = setupRefactor(src, pack, cunit);
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			breakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",breakpoint.getMarker().exists());
			assertEquals("wrong type name", newTargetLineage, breakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method",methodName,breakpoint.getMethodName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * @param src
	 * @param pack
	 * @param cunit
	 * @param fullTargetName
	 * @param newTargetLineage
	 * @param fieldName
	 * @throws Exception
	 */
	private void runWatchPointTest(String src, String pack, String cunit, String fullTargetName, String newTargetLineage, String fieldName) throws Exception {
		try {
			//create breakpoint to test
			IJavaWatchpoint breakpoint = createNestedTypeWatchPoint(src, pack, cunit, fullTargetName, true, true);
			//refactor
			Refactoring ref = setupRefactor(src, pack, cunit);
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			breakpoint = (IJavaWatchpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",breakpoint.getMarker().exists());
			assertEquals("breakpoint attached to wrong type", newTargetLineage, breakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong field", fieldName, breakpoint.getFieldName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

	private Refactoring setupRefactor(String root, String packageName, String cuName) throws Exception {
		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit = getCompilationUnit(javaProject, root, packageName, cuName);
		JavaRenameProcessor proc = new RenameCompilationUnitProcessor(cunit);
		proc.setNewElementName("RenamedCompilationUnit.java");
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

	public void testInnerAnonmyousTypeClassLoadpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType$innerChildsMethod()V$1",
					newTargetLineage = pack+"."+"RenamedCompilationUnit$InnerChildType$1";
			runClassLoadBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage);
	}//end testBreakPoint

	public void testInnerAnonymousTypeLineBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType$innerChildsMethod()V$1",
					newTargetLineage = pack+"."+"RenamedCompilationUnit$InnerChildType$1";
			int lineNumber = 40;

			runLineBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage, lineNumber);
	}//end testBreakPoint

	public void testInnerAnonymousTypeMethodBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType$innerChildsMethod()V$1$anonTypeMethod()V",
					newTargetLineage = pack+"."+"RenamedCompilationUnit$InnerChildType$1",
					methodName = "anonTypeMethod";
			runMethodBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage, methodName);
	}//end testBreakPoint

	public void testInnerAnonymousTypeWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType$innerChildsMethod()V$1$anAnonInt",
					newTargetLineage = pack+"."+"RenamedCompilationUnit$InnerChildType$1",
					fieldName = "anAnonInt";

			runWatchPointTest(src, pack, cunit, fullTargetName, newTargetLineage, fieldName);
	}//end testBreakPoint

	public void testInnerClassLoadpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType",
					newTargetLineage = pack+"."+"RenamedCompilationUnit$InnerChildType";

			runClassLoadBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage);
	}//end testBreakPoint

	public void testInnerLineBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType",
					newTargetLineage = pack+"."+"RenamedCompilationUnit$InnerChildType";
			int lineNumber = 35;

			runLineBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage, lineNumber);
	}//end testBreakPoint

	public void testInnerMethodBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType$innerChildsMethod()V",
					newTargetLineage = pack+"."+"RenamedCompilationUnit$InnerChildType",
					methodName = "innerChildsMethod";
			runMethodBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage, methodName);
	}//end testBreakPoint

	public void testInnerWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType$innerChildInt",
					newTargetLineage = pack+"."+"RenamedCompilationUnit$InnerChildType",
					fieldName = "innerChildInt";

			runWatchPointTest(src, pack, cunit, fullTargetName, newTargetLineage, fieldName);
	}//end testBreakPoint

	public void testNonPublicAnonymousTypeClassLoadpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType$nonPublicChildsMethod()V$1",
					newTargetLineage = pack+"."+"NonPublicChildType$1";

			runClassLoadBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage);
	}//end testBreakPoint

	public void testNonPublicAnonymousTypeLineBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType$nonPublicChildsMethod()V$1",
					newTargetLineage = pack+"."+"NonPublicChildType$1";
			int lineNumber = 56;

			runLineBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage, lineNumber);
	}//end testBreakPoint

	public void testNonPublicAnonymousTypeMethodBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType$nonPublicChildsMethod()V$1$anonTypeMethod()V",
					newTargetLineage = pack+"."+"NonPublicChildType$1",
					methodName = "anonTypeMethod";
			runMethodBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage, methodName);
	}//end testBreakPoint

	public void testNonPublicAnonymousTypeWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType$nonPublicChildsMethod()V$1$anAnonInt",
					newTargetLineage = pack+"."+"NonPublicChildType$1",
					fieldName = "anAnonInt";

			runWatchPointTest(src, pack, cunit, fullTargetName, newTargetLineage, fieldName);
	}//end testBreakPoint

	public void testNonPublicClassLoadpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType",
					newTargetLineage = pack+"."+"NonPublicChildType";


			runClassLoadBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage);
	}//end testBreakPoint

	public void testNonPublicLineBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType",
					newTargetLineage = pack+"."+"NonPublicChildType";
			int lineNumber = 51;

			runLineBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage, lineNumber);
	}//end testBreakPoint

	public void testNonPublicMethodBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType$nonPublicChildsMethod()V$",
					newTargetLineage = pack+"."+"NonPublicChildType",
					methodName = "nonPublicChildsMethod";
			runMethodBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage, methodName);
	}//end testBreakPoint

	public void testNonPublicWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType$nonPublicChildInt",
					newTargetLineage = pack+"."+"NonPublicChildType",
					fieldName = "nonPublicChildInt";

			runWatchPointTest(src, pack, cunit, fullTargetName, newTargetLineage, fieldName);
	}//end testBreakPoint

	public void testPublicAnonymousTypeClassLoadpoint() throws Exception {

		String 	src = "src",
		pack = "a.b.c",
		cunit = "MoveeChild.java",
		fullTargetName = "MoveeChild$childsMethod()V$1",
		newTargetLineage = pack+"."+"RenamedCompilationUnit$1";

		runClassLoadBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage);
	}//end testBreakPoint

	public void testPublicAnonymousTypeLineBreakpoint() throws Exception {

		String 	src = "src",
		pack = "a.b.c",
		cunit = "MoveeChild.java",
		fullTargetName = "MoveeChild$childsMethod()V$1",
		newTargetLineage = pack+"."+"RenamedCompilationUnit$1";
		int lineNumber = 26;

		runLineBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage, lineNumber);
	}//end testBreakPoint

	public void testPublicAnonymousTypeMethodBreakpoint() throws Exception {
		String 	src = "src",
		pack = "a.b.c",
		cunit = "MoveeChild.java",
		fullTargetName = "MoveeChild$childsMethod()V$1$anonTypeMethod()V",
		newTargetLineage = pack+"."+"RenamedCompilationUnit$1",
		methodName = "anonTypeMethod";

		runMethodBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage, methodName);
	}//end testBreakPoint

	public void testPublicAnonymousTypeWatchpoint() throws Exception {
		String 	src = "src",
		pack = "a.b.c",
		cunit = "MoveeChild.java",
		fullTargetName = "MoveeChild$childsMethod()V$1$anAnonInt",
		newTargetLineage = pack+"."+"RenamedCompilationUnit$1",
		fieldName = "anAnonInt";

		runWatchPointTest(src, pack, cunit, fullTargetName, newTargetLineage, fieldName);
	}//end testBreakPoint

	public void testPublicClassLoadpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild",
					newTargetLineage = pack+"."+"RenamedCompilationUnit";
			runClassLoadBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage);
	}//end testBreakPoint

public void testPublicLineBreakpoint() throws Exception {

			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild",
					newTargetLineage = pack+"."+"RenamedCompilationUnit";
			int lineNumber = 21;

			runLineBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage, lineNumber);
	}//end testBreakPoint

	public void testPublicMethodBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$childsMethod()V",
					newTargetLineage = pack+"."+"RenamedCompilationUnit",
					methodName = "childsMethod";
			runMethodBreakpointTest(src, pack, cunit, fullTargetName, newTargetLineage, methodName);
	}//end testBreakPoint

	public void testPublicWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$aChildInt",
					newTargetLineage = pack+"."+"RenamedCompilationUnit",
					fieldName = "aChildInt";
			runWatchPointTest(src, pack, cunit, fullTargetName, newTargetLineage, fieldName);
	}//end testBreakPoint

}
