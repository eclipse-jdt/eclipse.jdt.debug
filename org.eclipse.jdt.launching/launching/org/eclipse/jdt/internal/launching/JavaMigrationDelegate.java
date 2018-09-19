/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.internal.launching;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationMigrationDelegate;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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
	@Override
	public boolean isCandidate(ILaunchConfiguration candidate) throws CoreException {
		String pName = candidate.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		if(pName.equals(EMPTY_STRING)) {
			return false;
		}
		if (!isAvailable(pName)) {
			return false;
		}
		IResource[] mapped = candidate.getMappedResources();
		IResource target = getResource(candidate);
		if (target == null) {
			return mapped != null;
		}
		if (mapped == null) {
			return true;
		}
		if (mapped.length != 1) {
			return true;
		}
		return !target.equals(mapped[0]);
	}

	/**
	 * Returns whether the given project is available.
	 *
	 * @param projectName project name
	 * @return whether the project exists and is open
	 */
	private boolean isAvailable(String projectName) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		return project.exists() && project.isOpen();
	}

	/**
	 * Returns the associated <code>IResource</code> for the specified launch configuration
	 * or <code>null</code> if none.
	 *
	 * @param candidate the candidate to get the backing resource for
	 * @return associated <code>IResource</code> or <code>null</code>
	 *
	 * @since 3.3
	 *
	 * @throws CoreException if there is an error
	 */
	static IResource getResource(ILaunchConfiguration candidate) throws CoreException {
		IResource resource = null;
		String pname = candidate.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		if(Path.ROOT.isValidSegment(pname)) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(pname);
			String tname = candidate.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, EMPTY_STRING);
			if(!EMPTY_STRING.equals(tname)) {
				if(project != null && project.isAccessible()) {
					IJavaProject jproject = JavaCore.create(project);
					if(jproject != null && jproject.exists()) {
						tname = tname.replace('$', '.');
						IType type = jproject.findType(tname);
						if(type != null) {
							try {
								resource = type.getUnderlyingResource();
								if(resource == null) {
									resource = type.getAdapter(IResource.class);
								}
							}
							catch(JavaModelException jme) {
								LaunchingPlugin.log(jme);
								return null;
							}
						}
					}
				}
			} else {
				return project;
			}
			if (resource == null) {
				resource = project;
			}
		}
		return resource;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationMigrationDelegate#migrate(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void migrate(ILaunchConfiguration candidate) throws CoreException {
		ILaunchConfigurationWorkingCopy wc = candidate.getWorkingCopy();
		updateResourceMapping(wc);
		wc.doSave();
	}

	/**
	 * Updates the resource mapping for the given launch configuration.
	 *
	 * @param wc working copy
	 * @throws CoreException if an exception occurs updating resource mapping.
	 */
	public static void updateResourceMapping(ILaunchConfigurationWorkingCopy wc) throws CoreException {
		IResource resource = getResource(wc);
		IResource[] resources = null;
		if (resource != null) {
			resources = new IResource[]{resource};
		}
		wc.setMappedResources(resources);
	}

}
