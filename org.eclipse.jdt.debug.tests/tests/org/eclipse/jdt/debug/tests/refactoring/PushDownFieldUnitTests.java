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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

public class PushDownFieldUnitTests extends AbstractRefactoringDebugTest {

	public PushDownFieldUnitTests(String name) {
		super(name);
	}

	public void testWatchPoint() throws Exception {
		try {
			//create Breakpoint to test
			createWatchpoint("a.b.c.Movee", "anInt", true, true);
			//refactor
			Refactoring ref = setupRefactor("Movee","anInt","src","a.b.c","Movee.java");
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakPoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of watchpoints", 1, breakPoints .length);
			IJavaWatchpoint watchPoint = (IJavaWatchpoint) breakPoints [0];
			assertEquals("wrong type name", "a.b.c.MoveeChild", watchPoint.getTypeName());
			assertEquals("breakpoint attached to wrong field", "anInt", watchPoint.getFieldName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}


/////////////////////////////////////////

	/**
	 * @param parentClassName
	 * @param className
	 * @param root
	 * @param targetPackageName
	 * @param cuName
	 * @return
	 * @throws Exception
	 */
	private Refactoring setupRefactor(String parentClassName, String className, String root, String targetPackageName, String cuName) throws Exception {

		IJavaProject javaProject = get14Project();
		IType parentClas= getCompilationUnit(javaProject, root, targetPackageName, cuName).getType(parentClassName);
		IField clas= parentClas.getField("anInt");

        PushDownRefactoringProcessor processor = new PushDownRefactoringProcessor(new IField[] {clas});
		ProcessorBasedRefactoring ref= new ProcessorBasedRefactoring(processor);
		ref.checkInitialConditions(new NullProgressMonitor());

		return ref;
	}
}
