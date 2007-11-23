/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.refactoring;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

public class PushDownFieldUnitTests extends AbstractRefactoringDebugTest {

	public PushDownFieldUnitTests(String name) {
		super(name);
	}

	public void testWatchPoint() throws Exception {
		cleanTestFiles();
		
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
	}//end testBreakPoint
		
	
/////////////////////////////////////////
	
	private Refactoring setupRefactor(String parentClassName, String className, String root, String targetPackageName, String cuName) throws Exception {
		
		IJavaProject javaProject = getJavaProject();
		IType parentClas= getCompilationUnit(javaProject, root, targetPackageName, cuName).getType(parentClassName);
		IField clas= parentClas.getField("anInt");
		
        PushDownRefactoringProcessor processor = new PushDownRefactoringProcessor(new IField[] {clas});
		ProcessorBasedRefactoring ref= new ProcessorBasedRefactoring(processor);
		ref.checkInitialConditions(new NullProgressMonitor());

		return ref;
	}

	protected final void performRefactor(final Refactoring refactoring) throws Exception {
		CreateChangeOperation create= new CreateChangeOperation(refactoring);
		refactoring.checkFinalConditions(new NullProgressMonitor());
		PerformChangeOperation perform= new PerformChangeOperation(create);
		ResourcesPlugin.getWorkspace().run(perform, new NullProgressMonitor());
		waitForBuild();
	}	

}
