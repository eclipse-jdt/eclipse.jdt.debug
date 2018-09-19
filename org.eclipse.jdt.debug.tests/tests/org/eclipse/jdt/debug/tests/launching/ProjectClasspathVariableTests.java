/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.launching;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;

/**
 * Tests for the ${project_classpath} variable
 */
public class ProjectClasspathVariableTests extends AbstractDebugTest {

	public ProjectClasspathVariableTests(String name) {
		super(name);
	}

	/**
	 * Sets the selected resource in the navigator view.
	 *
	 * @param resource resource to select or <code>null</code> if empty
	 */
	protected void setSelection(final IResource resource) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage page = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
				assertNotNull("the active workbench window page should not be null", page);
				IViewPart part;
				try {
					part = page.showView("org.eclipse.ui.views.ResourceNavigator");
					assertNotNull("the part org.eclipse.ui.views.ResourceNavigator should not be null", part);
					ISelection selection = null;
					if (resource == null) {
						selection = new StructuredSelection();
					} else {
						selection = new StructuredSelection(resource);
					}
					IWorkbenchPartSite site = part.getSite();
					assertNotNull("The part site for org.eclipse.ui.views.ResourceNavigator should not be null ", site);
					ISelectionProvider provider = site.getSelectionProvider();
					assertNotNull("the selection provider should not be null for org.eclipse.ui.views.ResourceNavigator", provider);
					provider.setSelection(selection);
				} catch (PartInitException e) {
					assertNotNull("Failed to open navigator view", null);
				}

			}
		};
		DebugUIPlugin.getStandardDisplay().syncExec(r);
	}

	/**
	 * Tests that a project name must be specified.
	 *
	 * @throws Exception
	 */
	public void testMissingProjectName() throws Exception {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		setSelection(null);
		try {
			manager.performStringSubstitution("${project_classpath}");
		} catch (CoreException e) {
			return; // expected
		}
		assertNotNull("Test should have thrown an exception due to missing project name", null);
	}

	/**
	 * Tests the selected project's classpath.
	 *
	 * @throws Exception
	 */
	public void testSelectedProject() throws Exception {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		IProject project = get14Project().getProject();
		setSelection(project);
		String cp = manager.performStringSubstitution("${project_classpath}");
		StringBuilder buffer = new StringBuilder();
		// expecting default output location and A.jar
		buffer.append(ResourcesPlugin.getWorkspace().getRoot().getFolder(get14Project().getOutputLocation()).getLocation().toOSString());
		buffer.append(File.pathSeparatorChar);
		buffer.append(get14Project().getProject().getFolder("src").getFile("A.jar").getLocation().toOSString());
		assertEquals("Wrong classpath", buffer.toString(), cp);
	}

	/**
	 * Tests that a Java project must exist
	 *
	 * @throws Exception
	 */
	public void testProjectDoesNotExist() throws Exception {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		try {
			manager.performStringSubstitution("${project_classpath:a_non_existant_project}");
		} catch (CoreException e) {
			return; // expected
		}
		assertNotNull("Test should have thrown an exception due to project does not exist", null);
	}

	public void testProjectClasspath() throws Exception {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		String projectName = get14Project().getElementName();
		String cp = manager.performStringSubstitution("${project_classpath:" + projectName + "}");
		StringBuilder buffer = new StringBuilder();
		// expecting default output location and A.jar
		buffer.append(ResourcesPlugin.getWorkspace().getRoot().getFolder(get14Project().getOutputLocation()).getLocation().toOSString());
		buffer.append(File.pathSeparatorChar);
		buffer.append(get14Project().getProject().getFolder("src").getFile("A.jar").getLocation().toOSString());
		assertEquals("Wrong classpath", buffer.toString(), cp);
	}

}
