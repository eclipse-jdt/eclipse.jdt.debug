/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.variables.ExpandVariableContext;
import org.eclipse.debug.core.variables.ILaunchVariableManager;
import org.eclipse.debug.core.variables.ISimpleLaunchVariable;
import org.eclipse.debug.core.variables.LaunchVariableUtil;
import org.eclipse.debug.core.variables.SimpleLaunchVariable;

/**
 * Tests launch configuration variables
 */
public class LaunchVariableTests extends LaunchConfigurationTests {

	public LaunchVariableTests(String name) {
		super(name);
	}

	// ########################
	// # Loc variable tests
	// ########################
	/**
	 * Tests expansion of the resource_loc variable with an argument.
	 */
	public void testResourceLoc() {
		doTestContextExpandVariable("resource_loc", null, getSelectedResource().getLocation().toOSString());
	}
	/**
	 * Tests expansion of the resource_loc variable with an argument.
	 */
	public void testResourceLocWithArgument() {
		doTestContextExpandVariable("resource_loc", getArgumentString(), getArgument().getLocation().toOSString());
	}
	/**
	 * Tests expansion of the project_loc variable with an argument.
	 */
	public void testProjectLoc() {
		doTestContextExpandVariable("project_loc", null, getSelectedResource().getProject().getLocation().toOSString());
	}
	/**
	 * Tests expansion of the project_loc variable with an argument.
	 */
	public void testProjectLocWithArgument() {
		doTestContextExpandVariable("project_loc", getArgumentString(), getArgument().getProject().getLocation().toOSString());
	}
	/**
	 * Tests expansion of the container_loc variable with an argument.
	 */
	public void testContainerLoc() {
		doTestContextExpandVariable("container_loc", null, getSelectedResource().getParent().getLocation().toOSString());
	}
	/**
	 * Tests expansion of the container_loc variable with an argument.
	 */
	public void testContainerLocWithArgument() {
		doTestContextExpandVariable("container_loc", getArgumentString(), getArgument().getParent().getLocation().toOSString());
	}
	/**
	 * Tests expansion of the workspace_loc variable without an argument.
	 */
	public void testWorkspaceLoc() {
		doTestContextExpandVariable("workspace_loc", null, getWorkspaceRoot().getLocation().toOSString());
	}
	/**
	 * Tests expansion of the workspace_loc variable with an argument.
	 */
	public void testWorkspaceLocWithArgument() {
		doTestContextExpandVariable("workspace_loc", getArgumentString(), getArgument().getLocation().toOSString());
	}
	
	// ########################
	// # Path variable tests
	// ########################
	/**
	 * Tests expansion of the resource_path variable without an argument.
	 */
	public void testResourcePath() {
		doTestContextExpandVariable("resource_path", null, getSelectedResource().getFullPath().toOSString());
	}
	/**
	 * Tests expansion of the resource_path variable with an argument.
	 */
	public void testResourcePathWithArgument() {
		doTestContextExpandVariable("resource_path", getArgumentString(), getArgument().getFullPath().toOSString());
	}
	/**
	 * Tests expansion of the container_path variable without an argument.
	 */
	public void testProjectPath() {
		doTestContextExpandVariable("project_path", null, getSelectedResource().getProject().getFullPath().toOSString());
	}
	/**
	 * Tests expansion of the project_path variable with an argument.
	 */
	public void testProjectPathWithArgument() {
		doTestContextExpandVariable("project_path", getArgumentString(), getArgument().getProject().getFullPath().toOSString());
	}
	/**
	 * Tests expansion of the container_path variable without an argument.
	 */
	public void testContainerPath() {
		doTestContextExpandVariable("container_path", null, getSelectedResource().getParent().getFullPath().toOSString());
	}
	/**
	 * Tests expansion of the container_path variable with an argument.
	 */
	public void testContainerPathWithArgument() {
		doTestContextExpandVariable("container_path", getArgumentString(), getArgument().getParent().getFullPath().toOSString());
	}
	
	// ########################
	// # Name variable tests
	// ########################
	/**
	 * Tests expansion of the resource_name variable without an argument.
	 */
	public void testResourceName() {
		doTestContextExpandVariable("resource_name", null, getSelectedResource().getName());
	}
	/**
	 * Tests expansion of the resource_name variable with an argument.
	 */
	public void testResourceNameWithArgument() {
		doTestContextExpandVariable("resource_name", getArgumentString(), getArgument().getName());
	}
	/**
	 * Tests expansion of the project_name variable without an argument.
	 */
	public void testProjectName() {
		doTestContextExpandVariable("project_name", null, getSelectedResource().getProject().getName());
	}
	/**
	 * Tests expansion of the project_name variable with an argument.
	 */
	public void testProjectNameWithArgument() {
		doTestContextExpandVariable("project_name", getArgumentString(), getArgument().getProject().getName());
	}
	/**
	 * Tests expansion of the container_name variable without an argument.
	 */
	public void testContainerName() {
		doTestContextExpandVariable("container_name", null, getSelectedResource().getParent().getName());
	}
	/**
	 * Tests expansion of the container_name variable with an argument.
	 */
	public void testContainerNameWithArgument() {
		doTestContextExpandVariable("container_name", getArgumentString(), getArgument().getParent().getName());
	}
	
	// ########################
	// # Name variable tests
	// ########################
	/**
	 * Create a simple variable, add and retrieve it from the registry, and
	 * check its value.
	 */
	public void testSimpleVariable() {
		String variableName= "SimpleVariable";
		ISimpleLaunchVariable variable= new SimpleLaunchVariable(variableName);
		String variableValue= "VariableValue";
		variable.setText(variableValue);
		ILaunchVariableManager manager= DebugPlugin.getDefault().getLaunchVariableManager(); 
		manager.addSimpleVariables(new ISimpleLaunchVariable[] { variable });
		assertNotNull("Added variable not retrieved from simple variable registry.", manager.getSimpleVariable(variableName));
		String expandedValue= LaunchVariableUtil.expandVariables("${" + variableName + "}", new MultiStatus(DebugPlugin.getUniqueIdentifier(), IStatus.ERROR, "An exception occurred while retrieving simple variable value.", null), null);
		assertEquals("Simple variable value not equal to set value", variableValue, expandedValue);
	}
	
	/**
	 * Tests whether the given variable exists and whether expanding it with
	 * no arguments yields the expected value.
	 * 
	 * @param variableName the name of the variable to test
	 * @param expectedValue the expected value from expanding this value in the
	 * 		context of the resurce returned from <code>getSelectedResource()</code>.
	 */
	protected void doTestContextExpandVariable(String variableName, String argument, String expectedValue) {
		assertNotNull(MessageFormat.format("{0} variable should not be null", new String[] { variableName }), DebugPlugin.getDefault().getLaunchVariableManager().getContextVariable(variableName));
		StringBuffer variableString= new StringBuffer("${");
		variableString.append(variableName);
		if (argument != null) {
			variableString.append(':').append(argument);
		}
		variableString.append('}');
		
		MultiStatus status= new MultiStatus(DebugPlugin.getUniqueIdentifier(), IStatus.ERROR, MessageFormat.format("An exception occurred while expanding variable {0}", new String[] { variableName }), null);
		IResource selectedResource= getJavaProject().getResource();
		String expandedString= LaunchVariableUtil.expandVariables(variableString.toString(), status, new ExpandVariableContext(selectedResource));
		assertEquals(MessageFormat.format("{0} should match selected resource", new String[] { variableName }), expectedValue, expandedString);
	}
	
	/**
	 * Returns a resource (the test project) to be used as the selected resource context
	 * for variable expansion
	 * 
	 * @return a resource to be used as a selection context
	 */
	private IProject getSelectedResource() {
		return (IProject) getJavaProject().getResource();
	}
	
	private IResource getArgument() {
		return getSelectedResource().getFolder("bin");
	}
	
	private String getArgumentString() {
		return getArgument().getFullPath().toString();
	}
	
	/**
	 * Returns the workspace root.
	 * @return the workspace root
	 */
	private IResource getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
}
