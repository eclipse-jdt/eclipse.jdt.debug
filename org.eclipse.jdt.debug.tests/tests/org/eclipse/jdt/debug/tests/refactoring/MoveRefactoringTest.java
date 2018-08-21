/*******************************************************************************
 *  Copyright (c) 2007, 2013 IBM Corporation and others.
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

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IReorgPolicy.IMovePolicy;
import org.eclipse.jdt.internal.corext.refactoring.reorg.JavaMoveProcessor;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgDestinationFactory;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgPolicyFactory;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;

/**
 * Common code for setting up and performing a move refactoring.
 *
 * @since 3.4
 */
public class MoveRefactoringTest extends AbstractRefactoringDebugTest {

	/**
	 * @param name
	 */
	public MoveRefactoringTest(String name) {
		super(name);
	}

	/** Configures a processor for refactoring
	 * @param javaProject
	 * @param type
	 * @return the configured processor that will be used in refactoring
	 * @throws JavaModelException
	 */
	protected JavaMoveProcessor setupRefactor(IJavaProject javaProject, IJavaElement type) throws JavaModelException {
		IMovePolicy movePolicy= ReorgPolicyFactory.createMovePolicy(
				new IResource[0],
				new IJavaElement[] {type});
		JavaMoveProcessor processor= new JavaMoveProcessor(movePolicy);
		IJavaElement destination= getPackageFragmentRoot(javaProject, "src").getPackageFragment("a.b").getCompilationUnit("MoveeRecipient.java");
		processor.setDestination(ReorgDestinationFactory.createDestination(destination));
		processor.setReorgQueries(new MockReorgQueries());
		if(processor.canUpdateJavaReferences())
			processor.setUpdateReferences(true);//assuming is properly set otherwise
		return processor;
	}

	/** Sets up a refactoring and executes it.
	 * @param javaProject
	 * @param cunit
	 * @throws JavaModelException
	 * @throws Exception
	 */
	protected void refactor(IJavaProject javaProject, IJavaElement type) throws JavaModelException, Exception {
		JavaMoveProcessor processor = setupRefactor(javaProject, type);
		performRefactor(new MoveRefactoring(processor));
	}
}
