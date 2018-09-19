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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

public class RenameFieldUnitTests extends AbstractRefactoringDebugTest {

	public RenameFieldUnitTests(String name) {
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
	protected void runWatchPointTest(String src, String pack, String cunit, String fullTargetName, String targetLineage) throws Exception {
		String newFieldName = "renamedField";
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
			assertEquals("breakpoint attached to wrong field", newFieldName, breakpoint.getFieldName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}

	protected Refactoring setupRefactor(String root, String packageName, String cuName, String fullTargetName) throws Exception {

		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit = getCompilationUnit(javaProject, root, packageName, cuName);
		IField field = (IField)getMember(cunit, fullTargetName);
		JavaRenameProcessor proc = new RenameFieldProcessor(field);
		proc.setNewElementName("renamedField");

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

	public void testInnerAnonymousTypeWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType$innerChildsMethod()V$1$anAnonInt",
					targetLineage = pack+"."+"MoveeChild$InnerChildType$1";

			runWatchPointTest(src, pack, cunit, fullTargetName, targetLineage);
	}//end testBreakPoint

	public void testInnerWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$InnerChildType$innerChildInt",
					targetLineage = pack+"."+"MoveeChild$InnerChildType";

			runWatchPointTest(src, pack, cunit, fullTargetName, targetLineage);
	}//end testBreakPoint

	public void testNonPublicAnonymousTypeWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType$nonPublicChildsMethod()V$1$anAnonInt",
					targetLineage = pack+"."+"NonPublicChildType$1";

			runWatchPointTest(src, pack, cunit, fullTargetName, targetLineage);
	}//end testBreakPoint

	public void testNonPublicWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "NonPublicChildType$nonPublicChildInt",
					targetLineage = pack+"."+"NonPublicChildType";

			runWatchPointTest(src, pack, cunit, fullTargetName, targetLineage);
	}//end testBreakPoint

	public void testPublicAnonymousTypeWatchpoint() throws Exception {
		String 	src = "src",
		pack = "a.b.c",
		cunit = "MoveeChild.java",
		fullTargetName = "MoveeChild$childsMethod()V$1$anAnonInt",
		targetLineage = pack+"."+"MoveeChild$1";

		runWatchPointTest(src, pack, cunit, fullTargetName, targetLineage);
	}//end testBreakPoint

	public void testPublicWatchpoint() throws Exception {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "MoveeChild.java",
					fullTargetName = "MoveeChild$aChildInt",
					targetLineage = pack+"."+"MoveeChild";
			runWatchPointTest(src, pack, cunit, fullTargetName, targetLineage);
	}//end testBreakPoint

}
