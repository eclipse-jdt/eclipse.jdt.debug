/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
import org.eclipse.jdt.debug.testplugin.AlternateDelegate;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * Tests for launch delegates
 */
public class LaunchDelegateTests extends AbstractDebugTest {
	
	public LaunchDelegateTests(String name) {
		super(name);
	}
	
	/**
	 * Ensures a launch delegate can provide a launch object for
	 * a launch.
	 */
	public void testProvideLaunch() throws CoreException {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType configurationType = manager.getLaunchConfigurationType("org.eclipse.jdt.debug.tests.testConfigType");
		assertNotNull("Missing test launch config type", configurationType);
		ILaunchConfigurationWorkingCopy workingCopy = configurationType.newInstance(null, "provide-launch-object");
		// delegate will throw exception if test fails
		ILaunch launch = workingCopy.launch(ILaunchManager.DEBUG_MODE, null);
		manager.removeLaunch(launch);
	}
	
	/**
	 * Tests that a delegate extension can provide the source path computer.
	 */
	public void testSourcePathComputerExtension() {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType configurationType = manager.getLaunchConfigurationType("org.eclipse.jdt.debug.tests.testConfigType");
		assertNotNull("Missing test launch config type", configurationType);
		ISourcePathComputer sourcePathComputer = configurationType.getSourcePathComputer();
		assertEquals("Wrond source path computer", "org.eclipse.jdt.debug.tests.testSourcePathComputer", sourcePathComputer.getId());
	}
	
	/**
	 * Tests that a delegate extension can provide the source locator.
	 */
	public void testSourceLocatorExtension() {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType configurationType = manager.getLaunchConfigurationType("org.eclipse.jdt.debug.tests.testConfigType");
		assertNotNull("Missing test launch config type", configurationType);
		String sourceLocatorId = configurationType.getSourceLocatorId();
		assertEquals("Wrong source locater id", "org.eclipse.jdt.debug.tests.testSourceLocator", sourceLocatorId);
	}
	
	/**
	 * Test launch delegate for mixed launch mode.
	 * @throws CoreException 
	 */
	public void testMixedModeDelegate() throws CoreException {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		assertNotNull("Missing java application launch config type", type);
		HashSet modes = new HashSet();
		modes.add("alternate");
		modes.add(ILaunchManager.DEBUG_MODE);
		assertTrue("Should support mixed mode (alternate/debug)", type.supportsModeCombination(modes));
		ILaunchConfigurationDelegate[] delegates = type.getDelegates(modes);
		assertTrue("missing delegate", delegates.length > 0);
		assertEquals("Wrong number of delegates", 1, delegates.length);
		assertTrue("Wrong delegate", delegates[0] instanceof AlternateDelegate);
	}
	
	/**
	 * Tests correct delegate is found for debug mode.
	 * @throws CoreException
	 */
	public void testSingleDebugModeDelegate() throws CoreException {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		assertNotNull("Missing java application launch config type", type);
		HashSet modes = new HashSet();
		modes.add(ILaunchManager.DEBUG_MODE);
		assertTrue("Should support mode (debug)", type.supportsModeCombination(modes));
		ILaunchConfigurationDelegate[] delegates = type.getDelegates(modes);
		assertTrue("missing delegate", delegates.length > 0);
		assertEquals("Wrong number of delegates", 1, delegates.length);
		assertTrue("Wrong delegate", delegates[0].getClass().getName().endsWith("JavaLaunchDelegate"));		
	}
	
	/**
	 * Tests correct delegate is found for alternate mode.
	 * @throws CoreException
	 */
	public void testSingleAlternateModeDelegate() throws CoreException {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		assertNotNull("Missing java application launch config type", type);
		HashSet modes = new HashSet();
		modes.add("alternate");
		assertTrue("Should support mode (alternate)", type.supportsModeCombination(modes));
		ILaunchConfigurationDelegate[] delegates = type.getDelegates(modes);
		assertTrue("missing delegate", delegates.length > 0);
		assertEquals("Wrong number of delegates", 1, delegates.length);
		assertTrue("Wrong delegate", delegates[0] instanceof AlternateDelegate);		
	}	
}
