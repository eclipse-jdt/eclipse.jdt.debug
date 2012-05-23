/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.debug.internal.ui.stringsubstitution.IArgumentSelector;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Allows a Java project to be selected for the ${project_classpath} variable.
 */
public class ProjectClasspathArgumentSelector implements IArgumentSelector {

	public ProjectClasspathArgumentSelector() {
	}

	public String selectArgument(IStringVariable variable, Shell shell) {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new WorkbenchLabelProvider());
		dialog.setTitle(LauncherMessages.ProjectClasspathArugumentSelector_0);
		dialog.setMultipleSelection(false);
		dialog.setMessage(LauncherMessages.ProjectClasspathArugumentSelector_1);
		List<IJavaProject> javaProjects = new ArrayList<IJavaProject>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IJavaProject jp = JavaCore.create(projects[i]);
			if (jp.exists()) {
				javaProjects.add(jp);
			}
		}
		dialog.setElements(javaProjects.toArray()); 
		if (dialog.open() == Window.OK) {
			return (((IJavaProject)dialog.getResult()[0]).getElementName());
		}
		return null;
	}

}
