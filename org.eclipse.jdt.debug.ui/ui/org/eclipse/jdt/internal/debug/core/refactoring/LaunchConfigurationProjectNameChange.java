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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.IJavaProject;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import org.eclipse.jdt.internal.corext.refactoring.CompositeChange;
import org.eclipse.jdt.internal.corext.refactoring.base.Change;
import org.eclipse.jdt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 */
public class LaunchConfigurationProjectNameChange extends Change {
	
	private ILaunchConfiguration fLaunchConfiguration;
	
	private String fNewProjectName;
	
	/**
	 * @param javaProject
	 * @param string
	 * @return
	 */
	public static Change createChangesFor(IJavaProject javaProject, String newProjectName) throws CoreException {
		List changes= new ArrayList();
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType configurationType= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfiguration configs[]= manager.getLaunchConfigurations(configurationType);
		String projectName= javaProject.getElementName();
		for (int i= 0; i < configs.length; i++) {
			ILaunchConfiguration launchConfiguration = configs[i];
			String launchConfigurationProjectName= launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
			if (projectName.equals(launchConfigurationProjectName)) {
				changes.add(new LaunchConfigurationProjectNameChange(launchConfiguration, newProjectName));
			}
		}
		int nbChanges= changes.size();
		if (nbChanges == 0) {
			return null;
		} else if (nbChanges == 1) {
			return (Change) changes.get(0);
		} else {
			return new CompositeChange(RefactoringMessages.getString("LaunchConfigurationProjectNameChange.0"), (Change[])changes.toArray(new Change[changes.size()])); //$NON-NLS-1$
		}
	}
	
	public LaunchConfigurationProjectNameChange(ILaunchConfiguration launchConfiguration, String newProjectName) {
		fLaunchConfiguration= launchConfiguration;
		fNewProjectName= newProjectName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IChange#perform(org.eclipse.jdt.internal.corext.refactoring.base.ChangeContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		String currentProjectName= fLaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
		ILaunchConfigurationWorkingCopy copy = fLaunchConfiguration.getWorkingCopy();
		copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fNewProjectName);
		copy.doSave();
		return new LaunchConfigurationProjectNameChange(fLaunchConfiguration, currentProjectName);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IChange#getModifiedLanguageElement()
	 */
	public Object getModifiedElement() {
		return fLaunchConfiguration;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IChange#getName()
	 */
	public String getName() {
		return MessageFormat.format(RefactoringMessages.getString("LaunchConfigurationProjectNameChange.1"), new String[] {fLaunchConfiguration.getName()}); //$NON-NLS-1$
	}

	public void initializeValidationData(IProgressMonitor pm) throws CoreException {
		// must be implemented to decide correct value of isValid
	}

	public RefactoringStatus isValid(IProgressMonitor pm) {
		// TODO
		// This method must ensure that the change object is still valid.
		// This is in particular interesting when performing an undo change
		// since the workspace could have changed since the undo change has
		// been created.
		return new RefactoringStatus();
	}
}
