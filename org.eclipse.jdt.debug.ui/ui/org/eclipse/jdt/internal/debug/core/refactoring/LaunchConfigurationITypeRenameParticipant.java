/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
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

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;


/**
 */
public class LaunchConfigurationITypeRenameParticipant extends RenameParticipant {

	private IType fType;

	protected boolean initialize(Object element) {
		fType= (IType) element;
		
		return true;
	}
	
	public String getName() {
		return RefactoringMessages.getString("LaunchConfigurationITypeRenameParticipant.0"); //$NON-NLS-1$
	}
	
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) {
		return new RefactoringStatus();
	}

	public Change createChange(IProgressMonitor pm) throws CoreException {
		String packageName= Signature.getQualifier(fType.getFullyQualifiedName());
		String newFullyQualifiedName;
		if (packageName.length() == 0) {
			newFullyQualifiedName= getArguments().getNewName();
		} else {
			newFullyQualifiedName= packageName + '.' + getArguments().getNewName();
		}
		return LaunchConfigurationMainTypeNameChange.createChangesFor(fType, newFullyQualifiedName);
	}
}
