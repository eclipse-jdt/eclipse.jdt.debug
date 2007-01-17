/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * Delegate for migrating Java launch configurations.
 * The migration process involves a resource mapping being created such that launch configurations
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationMigrationDelegate#isCandidate()
	 */
	public boolean isCandidate(ILaunchConfiguration candidate) throws CoreException {
		if(candidate.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING).equals(EMPTY_STRING)) {
			return false;
		}
		if(!candidate.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CONTEXT_LAUNCHING_MIGRATED, false)) {
			return true;
		}
		IResource[] mappedResources = candidate.getMappedResources();
		if(mappedResources != null && mappedResources.length > 0) {
			return false;
		}
		return true;
	}

	/**
	 * Returns an array of the associated <code>IResource</code>s for the specified launch configuration candidate
	 * @param candidate the candidate to get the backing resource for
	 * @return an array of associated <code>IResource</code>s or an empty array, never <code>null</code>
	 * 
	 * @since 3.3
	 * EXPERIMENTAL
	 * CONTEXTLAUNCHING 
	 * 
	 * @throws CoreException
	 */
	protected IResource[] getResourcesForCandidate(ILaunchConfiguration candidate) throws CoreException {
		ArrayList res = new ArrayList();
		String pname = candidate.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		if(!EMPTY_STRING.equals(pname)) {
			String tname = candidate.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, EMPTY_STRING);
			if(!EMPTY_STRING.equals(tname)) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(pname);
				if(project != null && project.exists()) {
					IJavaProject jproject = JavaCore.create(project);
					if(jproject != null && jproject.exists()) {
						tname = tname.replace('$', '.');
						IType type = jproject.findType(tname);
						if(type != null) {
							Object o = type.getUnderlyingResource();
							if(o != null) {
								res.add(o);
							}
							else {
								o = type.getAdapter(IResource.class);
								if(o != null) {
									res.add(o);
								}
							}
						}
					}
				}
			}
		}
 		return (IResource[]) res.toArray(new IResource[res.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationMigrationDelegate#migrate(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void migrate(ILaunchConfiguration candidate) throws CoreException {
		IResource[] resources = getResourcesForCandidate(candidate);
		if(resources.length > 0) {
			ArrayList mappings = new ArrayList();
			for(int i = 0; i < resources.length; i++) {
				if(!mappings.contains(resources[i])) {
					mappings.add(resources[i]);
				}
			}
			ILaunchConfigurationWorkingCopy wc = candidate.getWorkingCopy();
			wc.setMappedResources((IResource[])mappings.toArray(new IResource[mappings.size()]));
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CONTEXT_LAUNCHING_MIGRATED, true);
			wc.doSave();
		}
	}

}
