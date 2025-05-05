/*******************************************************************************
 * Copyright (c) 2010, 2018 IBM Corporation and others.
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.osgi.util.NLS;

/**
 * Resolver for {@code ${project_classpath:<project_name>}}. Returns a string corresponding to the class path of the corresponding Java project.
 */
public class ProjectClasspathVariableResolver implements IDynamicVariableResolver {

	@Override
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		IProject proj = null;
		if (argument == null) {
			IResource resource = getSelectedResource();
			if (resource != null && resource.exists()) {
				proj = resource.getProject();
			}
			if (proj == null) {
				throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, LaunchingMessages.ProjectClasspathVariableResolver_2));
			}
		} else {
			proj = ResourcesPlugin.getWorkspace().getRoot().getProject(argument);
		}
		IJavaProject javaProject = JavaCore.create(proj);
		if (javaProject.exists()) {
			IRuntimeClasspathEntry2 defClassPath = (IRuntimeClasspathEntry2) JavaRuntime.newDefaultProjectClasspathEntry(javaProject);
			IRuntimeClasspathEntry[] entries = defClassPath.getRuntimeClasspathEntries(false);
			List<IRuntimeClasspathEntry> collect = new ArrayList<>();
			for (int i = 0; i < entries.length; i++) {
				IRuntimeClasspathEntry[] children = JavaRuntime.resolveRuntimeClasspathEntry(entries[i], javaProject, false);
				for (int j = 0; j < children.length; j++) {
					collect.add(children[j]);
				}
			}
			entries = collect.toArray(new IRuntimeClasspathEntry[collect.size()]);
			StringBuilder buffer = new StringBuilder();
			for (int i = 0; i < entries.length; i++) {
				if (i > 0) {
					buffer.append(File.pathSeparatorChar);
				}
				buffer.append(entries[i].getLocation());
			}
			return buffer.toString();
		}
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, NLS.bind(LaunchingMessages.ProjectClasspathVariableResolver_1, argument)));
	}

	/**
	 * Returns the selected resource. Uses the ${selected_resource_path} variable
	 * to determine the selected resource. This variable is provided by the debug.ui
	 * plug-in. Selected resource resolution is only available when the debug.ui
	 * plug-in is present.
	 *
	 * @return selected resource
	 * @throws CoreException if there is no selection
	 */
	protected IResource getSelectedResource() throws CoreException {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		try {
			String pathString = manager.performStringSubstitution("${selected_resource_path}"); //$NON-NLS-1$
			return ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(pathString));
		} catch (CoreException e) {
			// unable to resolve a selection
		}
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, LaunchingMessages.ProjectClasspathVariableResolver_3));
	}

}
