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

import org.eclipse.jdt.core.IField;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

/**
 */
public class JavaWatchpointIFieldRenameParticipant extends RenameParticipant {
	
	private IField fField;

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.IRefactoringParticipant#initialize(org.eclipse.jdt.internal.corext.refactoring.participants.IRefactoringProcessor, java.lang.Object)
	 */
	public void initialize(RefactoringProcessor processor, Object element) throws CoreException {
		setProcessor(processor);
		fField= (IField) element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.IRefactoringParticipant#isAvailable()
	 */
	public boolean isApplicable() throws CoreException {
		return !fField.getDeclaringType().isLocal();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.IRefactoringParticipant#checkActivation()
	 */
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException {
		return new RefactoringStatus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.IRefactoringParticipant#checkInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException {
		return new RefactoringStatus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.IRefactoringParticipant#createChange(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change createChange(IProgressMonitor pm) throws CoreException {
		return JavaWatchpointFieldNameChange.createChange(fField, getArguments().getNewName());
	}

}