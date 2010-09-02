/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry2;
import org.eclipse.jdt.launching.JavaRuntime;

import com.ibm.icu.text.MessageFormat;

/**
 * Resolver for ${project_classpath:<project_name>}. Returns a string corresponding to the
 * class path of the corresponding Java project.
 */
public class ProjectClasspathVariableResolver implements IDynamicVariableResolver {

	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		if (argument == null) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, LaunchingMessages.ProjectClasspathVariableResolver_0));
		}
		IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(argument);
		IJavaProject javaProject = JavaCore.create(proj);
		if (javaProject.exists()) {
			IRuntimeClasspathEntry2 defClassPath = (IRuntimeClasspathEntry2) JavaRuntime.newDefaultProjectClasspathEntry(javaProject);
			IRuntimeClasspathEntry[] entries = defClassPath.getRuntimeClasspathEntries(null);
			List collect = new ArrayList();
			for (int i = 0; i < entries.length; i++) {
				IRuntimeClasspathEntry[] children = JavaRuntime.resolveRuntimeClasspathEntry(entries[i], javaProject);
				for (int j = 0; j < children.length; j++) {
					collect.add(children[j]);
				}
			}
			entries = (IRuntimeClasspathEntry[]) collect.toArray(new IRuntimeClasspathEntry[collect.size()]);
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < entries.length; i++) {
				if (i > 0) {
					buffer.append(File.pathSeparatorChar);
				}
				buffer.append(entries[i].getLocation());
			}
			return buffer.toString();
		} else {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, MessageFormat.format(LaunchingMessages.ProjectClasspathVariableResolver_1, new String[]{argument})));
		}
	}

}
