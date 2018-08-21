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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenamePackageProcessor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

public class RenamePackageUnitTests extends AbstractRefactoringDebugTest {

	public RenamePackageUnitTests(String name) {
		super(name);
	}

	/**
	 * @param src
	 * @param pack
	 * @param cunit
	 * @param fullTargetName
	 * @param targetLineage
	 * @throws Exception
	 */
	protected void runClassLoadBreakpointTest(String src, String pack, String cunit, String fullTargetName, String targetLineage) throws Exception {
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
			assertEquals("breakpoint attached to wrong type", targetLineage, breakpoint.getTypeName());
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
	 * @param targetsLineage
	 * @param lineNumber
	 * @throws Exception
	 */
	protected void runLineBreakpointTest(String src, String pack, String cunit, String fullTargetName, String targetsLineage, int lineNumber) throws Exception {
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
			assertEquals("breakpoint attached to wrong type", targetsLineage, breakpoint.getTypeName());
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
	 * @param targetLineage
	 * @param methodName
	 * @throws Exception
	 */
	protected void runMethodBreakpointTest(String src, String pack, String cunit, String fullTargetName, String targetLineage, String methodName) throws Exception {
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
			assertEquals("wrong type name", targetLineage, breakpoint.getTypeName());
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
	 * @param targetLineage
	 * @param fieldName
	 * @throws Exception
	 */
	protected void runWatchPointTest(String src, String pack, String cunit, String fullTargetName, String targetLineage, String fieldName) throws Exception {
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
			assertEquals("breakpoint attached to wrong type", targetLineage, breakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong field", fieldName, breakpoint.getFieldName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

	protected Refactoring setupRefactor(String root, String packageName, String cuName) throws Exception {
		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit = getCompilationUnit(javaProject, root, packageName, cuName);
		IPackageFragment packageFragment = (IPackageFragment )cunit.getParent();
		JavaRenameProcessor proc = new RenamePackageProcessor(packageFragment);
		proc.setNewElementName("renamedPackage");
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

	//////////////////////////////////////////////////////////////////////////////////////

	public void testInnerAnonmyousTypeClassLoadpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType$innerChildsMethod()V$1",
					targetLineage = "renamedPackage"+"."+"MoveeChild$InnerChildType$1";
			runClassLoadBreakpointTest(src, pack, cunit, fullTargetName, targetLineage);
	}

	public void testInnerAnonymousTypeLineBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType$innerChildsMethod()V$1",
					targetLineage = "renamedPackage"+"."+"MoveeChild$InnerChildType$1";
			int lineNumber = 40;

			runLineBreakpointTest(src, pack, cunit, fullTargetName, targetLineage, lineNumber);
	}

	public void testInnerAnonymousTypeMethodBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType$innerChildsMethod()V$1$anonTypeMethod()V",
					targetLineage = "renamedPackage"+"."+"MoveeChild$InnerChildType$1",
					methodName = "anonTypeMethod";
			runMethodBreakpointTest(src, pack, cunit, fullTargetName, targetLineage, methodName);
	}

	public void testInnerAnonymousTypeWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType$innerChildsMethod()V$1$anAnonInt",
					targetLineage = "renamedPackage"+"."+"MoveeChild$InnerChildType$1",
					fieldName = "anAnonInt";

			runWatchPointTest(src, pack, cunit, fullTargetName, targetLineage, fieldName);
	}

	public void testInnerClassLoadpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType",
					targetLineage = "renamedPackage"+"."+"MoveeChild$InnerChildType";

			runClassLoadBreakpointTest(src, pack, cunit, fullTargetName, targetLineage);
	}

	public void testInnerLineBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType",
					targetLineage = "renamedPackage"+"."+"MoveeChild$InnerChildType";
			int lineNumber = 35;

			runLineBreakpointTest(src, pack, cunit, fullTargetName, targetLineage, lineNumber);
	}

	public void testInnerMethodBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType$innerChildsMethod()V",
					targetLineage = "renamedPackage"+"."+"MoveeChild$InnerChildType",
					methodName = "innerChildsMethod";
			runMethodBreakpointTest(src, pack, cunit, fullTargetName, targetLineage, methodName);
	}

	public void testInnerWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType$innerChildInt",
					targetLineage = "renamedPackage"+"."+"MoveeChild$InnerChildType",
					fieldName = "innerChildInt";

			runWatchPointTest(src, pack, cunit, fullTargetName, targetLineage, fieldName);
	}

	public void testNonPublicAnonymousTypeClassLoadpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType$nonPublicChildsMethod()V$1",
					targetLineage = "renamedPackage"+"."+"NonPublicChildType$1";

				runClassLoadBreakpointTest(src, pack, cunit, fullTargetName, targetLineage);
	}

	public void testNonPublicAnonymousTypeLineBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType$nonPublicChildsMethod()V$1",
					targetLineage = "renamedPackage"+"."+"NonPublicChildType$1";
			int lineNumber = 56;

			runLineBreakpointTest(src, pack, cunit, fullTargetName, targetLineage, lineNumber);
	}

	public void testNonPublicAnonymousTypeMethodBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType$nonPublicChildsMethod()V$1$anonTypeMethod()V",
					targetLineage = "renamedPackage"+"."+"NonPublicChildType$1",
					methodName = "anonTypeMethod";
			runMethodBreakpointTest(src, pack, cunit, fullTargetName, targetLineage, methodName);
	}

	public void testNonPublicAnonymousTypeWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType$nonPublicChildsMethod()V$1$anAnonInt",
					targetLineage = "renamedPackage"+"."+"NonPublicChildType$1",
					fieldName = "anAnonInt";

			runWatchPointTest(src, pack, cunit, fullTargetName, targetLineage, fieldName);
	}

	public void testNonPublicClassLoadpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType",
					targetLineage = "renamedPackage"+"."+"NonPublicChildType";


			runClassLoadBreakpointTest(src, pack, cunit, fullTargetName, targetLineage);
	}

	public void testNonPublicLineBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType",
					targetLineage = "renamedPackage"+"."+"NonPublicChildType";
			int lineNumber = 51;

			runLineBreakpointTest(src, pack, cunit, fullTargetName, targetLineage, lineNumber);
	}

	public void testNonPublicMethodBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType$nonPublicChildsMethod()V$",
					targetLineage = "renamedPackage"+"."+"NonPublicChildType",
					methodName = "nonPublicChildsMethod";
			runMethodBreakpointTest(src, pack, cunit, fullTargetName, targetLineage, methodName);
	}

	public void testNonPublicWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType$nonPublicChildInt",
					targetLineage = "renamedPackage"+"."+"NonPublicChildType",
					fieldName = "nonPublicChildInt";

			runWatchPointTest(src, pack, cunit, fullTargetName, targetLineage, fieldName);
	}

	public void testPublicAnonymousTypeClassLoadpoint() throws Exception {

		String 	src = "src",
		pack = "a.b.c",
		cunit = "MoveeChild.java",
		fullTargetName = "MoveeChild$childsMethod()V$1",
		targetLineage = "renamedPackage"+"."+"MoveeChild$1";

		runClassLoadBreakpointTest(src, pack, cunit, fullTargetName, targetLineage);
	}

	public void testPublicAnonymousTypeLineBreakpoint() throws Exception {

		String 	src = "src",
		pack = "a.b.c",
		cunit = "MoveeChild.java",
		fullTargetName = "MoveeChild$childsMethod()V$1",
		targetLineage = "renamedPackage"+"."+"MoveeChild$1";
		int lineNumber = 26;

		runLineBreakpointTest(src, pack, cunit, fullTargetName, targetLineage, lineNumber);
	}

	public void testPublicAnonymousTypeMethodBreakpoint() throws Exception {
		String 	src = "src",
		pack = "a.b.c",
		cunit = "MoveeChild.java",
		fullTargetName = "MoveeChild$childsMethod()V$1$anonTypeMethod()V",
		targetLineage = "renamedPackage"+"."+"MoveeChild$1",
		methodName = "anonTypeMethod";

		runMethodBreakpointTest(src, pack, cunit, fullTargetName, targetLineage, methodName);
	}

	public void testPublicAnonymousTypeWatchpoint() throws Exception {
		String 	src = "src",
		pack = "a.b.c",
		cunit = "MoveeChild.java",
		fullTargetName = "MoveeChild$childsMethod()V$1$anAnonInt",
		targetLineage = "renamedPackage"+"."+"MoveeChild$1",
		fieldName = "anAnonInt";

		runWatchPointTest(src, pack, cunit, fullTargetName, targetLineage, fieldName);
	}

	public void testPublicClassLoadpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild",
					targetLineage = "renamedPackage"+"."+"MoveeChild";
			runClassLoadBreakpointTest(src, pack, cunit, fullTargetName, targetLineage);
	}

	public void testPublicLineBreakpoint() throws Exception {

			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild",
					targetLineage = "renamedPackage"+"."+"MoveeChild";
			int lineNumber = 21;

			runLineBreakpointTest(src, pack, cunit, fullTargetName, targetLineage, lineNumber);
	}

	public void testPublicMethodBreakpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$childsMethod()V",
					targetLineage = "renamedPackage"+"."+"MoveeChild",
					methodName = "childsMethod";
			runMethodBreakpointTest(src, pack, cunit, fullTargetName, targetLineage, methodName);
	}

	public void testPublicWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$aChildInt",
					targetLineage = "renamedPackage"+"."+"MoveeChild",
					fieldName = "aChildInt";
			runWatchPointTest(src, pack, cunit, fullTargetName, targetLineage, fieldName);
	}

}
