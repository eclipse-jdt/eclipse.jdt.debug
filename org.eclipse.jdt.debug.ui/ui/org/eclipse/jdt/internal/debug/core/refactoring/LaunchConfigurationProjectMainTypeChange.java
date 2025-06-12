/*******************************************************************************
 * Copyright (c) 2003, 2012 IBM Corporation and others.
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
 *     Feng(Marvin) Wang <feng.wang@sybase.com> - bug 244395
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.refactoring;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.launching.JavaMigrationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;

/**
 * The change for the main type project change of a launch configuration
 */
public class LaunchConfigurationProjectMainTypeChange extends Change {

	private ILaunchConfiguration fLaunchConfiguration;
	private final String fNewMainTypeName;
	private final String fNewProjectName;
	private String fNewLaunchConfigurationName;
	private final String fOldMainTypeName;
	private final String fOldProjectName;
    private String fNewConfigContainerName;

    /**
	 * LaunchConfigurationProjectMainTypeChange constructor.
	 * @param launchConfiguration the launch configuration to modify
	 * @param newMainTypeName the name of the new main type, or <code>null</code> if not modified.
	 * @param newProjectName the name of the project, or <code>null</code> if not modified.
	 */
	public LaunchConfigurationProjectMainTypeChange(ILaunchConfiguration launchConfiguration, String newMainTypeName, String newProjectName) throws CoreException {
		fLaunchConfiguration = launchConfiguration;
		fNewMainTypeName = newMainTypeName;
        fNewProjectName = newProjectName;
		fOldMainTypeName = fLaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String) null);
		fOldProjectName = fLaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
		if (fNewMainTypeName != null) {
			// generate the new configuration name
			String oldName = Signature.getSimpleName(fOldMainTypeName);
			String newName = Signature.getSimpleName(fNewMainTypeName);
			String lcname = fLaunchConfiguration.getName();
			fNewLaunchConfigurationName = lcname.replaceAll(oldName, newName);
			if (lcname.equals(fNewLaunchConfigurationName) || DebugPlugin.getDefault().getLaunchManager().isExistingLaunchConfigurationName(fNewLaunchConfigurationName)) {
				fNewLaunchConfigurationName = null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getModifiedElement()
	 */
	@Override
	public Object getModifiedElement() {
		return fLaunchConfiguration;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	@Override
	public String getName() {
		if (fNewLaunchConfigurationName != null) {
			return NLS.bind(RefactoringMessages.LaunchConfigurationProjectMainTypeChange_0, fLaunchConfiguration.getName(), fNewLaunchConfigurationName);
		}
		if (fNewProjectName == null) {
			return NLS.bind(RefactoringMessages.LaunchConfigurationProjectMainTypeChange_1, fLaunchConfiguration.getName());
		}
		if (fNewMainTypeName == null) {
			return NLS.bind(RefactoringMessages.LaunchConfigurationProjectMainTypeChange_2, fLaunchConfiguration.getName());
		}
		return NLS.bind(RefactoringMessages.LaunchConfigurationProjectMainTypeChange_3, fLaunchConfiguration.getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#initializeValidationData(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void initializeValidationData(IProgressMonitor pm) {}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#isValid(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (fLaunchConfiguration.exists()) {
			String typeName = fLaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
			String projectName = fLaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
			if(fOldMainTypeName != null) {
				if (fOldMainTypeName.equals(typeName)) {
					if (fOldProjectName.equals(projectName)) {
						return new RefactoringStatus();
					}
					return RefactoringStatus.createWarningStatus(NLS.bind(RefactoringMessages.LaunchConfigurationProjectMainTypeChange_4, fLaunchConfiguration.getName(), fOldProjectName));
				}
				return RefactoringStatus.createWarningStatus(NLS.bind(RefactoringMessages.LaunchConfigurationProjectMainTypeChange_5, fLaunchConfiguration.getName(), fOldMainTypeName));
			}
			//need to catch the case for remote java LC's, they have no maintype
			if (fOldProjectName.equals(projectName)) {
				return new RefactoringStatus();
			}
			return RefactoringStatus.createWarningStatus(NLS.bind(RefactoringMessages.LaunchConfigurationProjectMainTypeChange_4, fLaunchConfiguration.getName(), fOldProjectName));
		}
		return RefactoringStatus.createFatalErrorStatus(NLS.bind(RefactoringMessages.LaunchConfigurationProjectMainTypeChange_6, fLaunchConfiguration.getName()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		final ILaunchConfigurationWorkingCopy wc = fLaunchConfiguration.getWorkingCopy();
        if (fNewConfigContainerName != null) {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IWorkspaceRoot root = workspace.getRoot();
            IProject project = root.getProject(fNewProjectName);
            IContainer container = (IContainer) project.findMember(fNewConfigContainerName);
            wc.setContainer(container);
        }
		String oldMainTypeName;
		String oldProjectName;
		if (fNewMainTypeName != null) {
			oldMainTypeName = fOldMainTypeName;
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, fNewMainTypeName);
		}
		else {
			oldMainTypeName = null;
		}
		if (fNewProjectName != null) {
			oldProjectName = fOldProjectName;
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fNewProjectName);
		}
		else {
			oldProjectName = null;
		}
		if (fNewLaunchConfigurationName != null) {
			wc.rename(fNewLaunchConfigurationName);
		}
		// update resource mapping
		JavaMigrationDelegate.updateResourceMapping(wc);
		if (wc.isDirty()) {
			fLaunchConfiguration = wc.doSave();
		}
		// create the undo change
		return new LaunchConfigurationProjectMainTypeChange(fLaunchConfiguration, oldMainTypeName, oldProjectName);
	}

	/**
     * Sets the new container name
     * @param newContainerName the new name for the container
     */
    public void setNewContainerName(String newContainerName) {
        fNewConfigContainerName = newContainerName;
    }
}
