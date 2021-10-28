/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
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

package org.eclipse.jdt.internal.debug.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

/**
 * Provides a rename participant for module descriptions with respect to launch configurations
 */
public class LaunchConfigurationIModuleDescriptionRenameParticipant extends RenameParticipant {

	/**
	 * the module to rename
	 */
	private IModuleDescription fModuleDescription;

	@Override
	protected boolean initialize(Object element) {
		fModuleDescription = (IModuleDescription) element;
		return true;
	}

	@Override
	public String getName() {
		return RefactoringMessages.LaunchConfigurationParticipant_0;
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException {
		return JDTDebugRefactoringUtil.createChangesForModuleRename(fModuleDescription, getArguments().getNewName());
	}
}
