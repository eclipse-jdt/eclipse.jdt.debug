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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.CompositeChange;
import org.eclipse.jdt.internal.corext.refactoring.base.Change;
import org.eclipse.jdt.internal.corext.refactoring.base.ChangeAbortException;
import org.eclipse.jdt.internal.corext.refactoring.base.ChangeContext;
import org.eclipse.jdt.internal.corext.refactoring.base.IChange;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 */
public class LaunchConfigurationProjectNameChange extends Change {
	
	private ILaunchConfiguration fLaunchConfiguration;
	
	private String fNewProjectName;
	
	private IChange fUndo;
	
	/**
	 * @param javaProject
	 * @param string
	 * @return
	 */
	public static IChange createChangesFor(IJavaProject javaProject, String newProjectName) throws CoreException {
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
			return (IChange) changes.get(0);
		} else {
			return new CompositeChange(RefractoringMessages.getString("LaunchConfigurationProjectNameChange.0"), (IChange[])changes.toArray(new IChange[changes.size()])); //$NON-NLS-1$
		}
	}
	
	public LaunchConfigurationProjectNameChange(ILaunchConfiguration launchConfiguration, String newProjectName) {
		fLaunchConfiguration= launchConfiguration;
		fNewProjectName= newProjectName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IChange#perform(org.eclipse.jdt.internal.corext.refactoring.base.ChangeContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void perform(ChangeContext context, IProgressMonitor pm) throws JavaModelException, ChangeAbortException {
		try {
			String currentProjectName= fLaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
			ILaunchConfigurationWorkingCopy copy = fLaunchConfiguration.getWorkingCopy();
			copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fNewProjectName);
			copy.doSave();
			fUndo= new LaunchConfigurationProjectNameChange(fLaunchConfiguration, currentProjectName);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IChange#getUndoChange()
	 */
	public IChange getUndoChange() {
		return fUndo;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IChange#getModifiedLanguageElement()
	 */
	public Object getModifiedLanguageElement() {
		return fLaunchConfiguration;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IChange#getName()
	 */
	public String getName() {
		return MessageFormat.format(RefractoringMessages.getString("LaunchConfigurationProjectNameChange.1"), new String[] {fLaunchConfiguration.getName()}); //$NON-NLS-1$
	}
}
