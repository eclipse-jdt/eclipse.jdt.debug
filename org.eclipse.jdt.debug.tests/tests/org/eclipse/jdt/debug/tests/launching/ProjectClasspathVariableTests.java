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
package org.eclipse.jdt.debug.tests.launching;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests for the ${project_classpath} variable 
 */
public class ProjectClasspathVariableTests extends AbstractDebugTest {

	public ProjectClasspathVariableTests(String name) {
		super(name);
	}
	
	/**
	 * Tests that a project name must be specified.
	 * 
	 * @throws Exception
	 */
	public void testMissingProjectName() throws Exception {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		try {
			manager.performStringSubstitution("${project_classpath}");
		} catch (CoreException e) {
			return; // expected
		}
		assertNotNull("Test should have thrown an exception due to missing project name", null);
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
		String projectName = getJavaProject().getElementName();
		String cp = manager.performStringSubstitution("${project_classpath:" + projectName + "}");
		StringBuffer buffer = new StringBuffer();
		// expecting default output location and A.jar
		buffer.append(ResourcesPlugin.getWorkspace().getRoot().getFolder(getJavaProject().getOutputLocation()).getLocation().toOSString());
		buffer.append(File.pathSeparatorChar);
		buffer.append(getJavaProject().getProject().getFolder("src").getFile("A.jar").getLocation().toOSString());
		assertEquals("Wrong classpath", buffer.toString(), cp);
	}

}
