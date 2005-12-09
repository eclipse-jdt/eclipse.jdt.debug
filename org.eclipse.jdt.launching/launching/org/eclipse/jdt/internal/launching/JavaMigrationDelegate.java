/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationMigrationDelegate;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * Delegate for migrating Java launch configurations.
 * The migration process involves a resource mapping being craeted such that launch configurations
 * can be filtered from the launch configuration dialog based on resource availability
 * 
 * @since 3.2
 */
public class JavaMigrationDelegate implements ILaunchConfigurationMigrationDelegate {

	/**
	 * represents the empty string
	 */
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/**
	 * Constructor needed for reflection
	 */
	public JavaMigrationDelegate() {}
	
	/**
	 * Method to get the projects for the specified launch configuration that should be mapped to the launch configuration  
	 * 
	 * @param candidate the launch configuration that the projects will be mapped to.
	 * @return a list of the projects to be mapped or an empty list, never null.
	 * @throws CoreException 
	 */
	protected IProject[] getProjectsForCandidate(ILaunchConfiguration candidate) throws CoreException {
		String pname = candidate.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		return new IProject[] {ResourcesPlugin.getWorkspace().getRoot().getProject(pname)};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationMigrationDelegate#isCandidate()
	 */
	public boolean isCandidate(ILaunchConfiguration candidate) throws CoreException {
		if(candidate.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING).equals(EMPTY_STRING)) {
			return false;
		}
		IResource[] mappedResources = candidate.getMappedResources();
		if(mappedResources != null && mappedResources.length > 0) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationMigrationDelegate#migrate(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void migrate(ILaunchConfiguration candidate) throws CoreException {
		IProject[] projects = getProjectsForCandidate(candidate);
		ArrayList mappings = new ArrayList();
		for(int i = 0; i < projects.length; i++) {
			if(projects[i].exists() & !mappings.contains(projects[i])) {
				mappings.add(projects[i]);
			}
		}
		ILaunchConfigurationWorkingCopy wc = candidate.getWorkingCopy();
		wc.setMappedResources((IResource[])mappings.toArray(new IResource[mappings.size()]));
		wc.doSave();
	}

}
