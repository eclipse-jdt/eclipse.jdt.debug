/*******************************************************************************
 * Copyright (c) 2004 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.debug.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.corext.refactoring.participants.IRefactoringProcessor;
import org.eclipse.jdt.internal.corext.refactoring.participants.RenameParticipant;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class LaunchConfigurationIPackageFragmentRenameParticipant extends RenameParticipant {
	
	private IPackageFragment fPackageFragment;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.IRefactoringParticipant#initialize(org.eclipse.jdt.internal.corext.refactoring.participants.IRefactoringProcessor, java.lang.Object)
	 */
	public void initialize(IRefactoringProcessor processor, Object element) throws CoreException {
		super.initialize(processor);
		fPackageFragment= (IPackageFragment)element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.IRefactoringParticipant#isAvailable()
	 */
	public boolean isAvailable() throws CoreException {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.IRefactoringParticipant#operatesOn(java.lang.Object)
	 */
	public boolean operatesOn(Object element) {
		return fPackageFragment.equals(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.IRefactoringParticipant#checkActivation()
	 */
	public RefactoringStatus checkActivation() throws CoreException {
		return new RefactoringStatus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.IRefactoringParticipant#checkInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RefactoringStatus checkInput(IProgressMonitor pm) throws CoreException {
		return new RefactoringStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.IRefactoringParticipant#createChange(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change createChange(IProgressMonitor pm) throws CoreException {
		return LaunchConfigurationMainTypeNameChange.createChangesFor(fPackageFragment, getNewName());
	}
}