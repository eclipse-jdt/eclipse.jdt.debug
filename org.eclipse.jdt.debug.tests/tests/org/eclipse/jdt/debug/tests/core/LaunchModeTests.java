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

import junit.framework.AssertionFailedError;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.debug.testplugin.TestModeLaunchDelegate;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * LaunchModeTests
 */
public class LaunchModeTests extends AbstractDebugTest {
	
	private ILaunchConfiguration fConfiguration;
	private String fMode;

	/**
	 * @param name
	 */
	public LaunchModeTests(String name) {
		super(name);
	}

	/**
	 * Called by launch "TestModeLaunchDelegate" delegate when launch method invoked.
	 * 
	 * @param configuration
	 * @param mode
	 * @param launch
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch) {
		fConfiguration = configuration;
		fMode = mode;
	}

	/**
	 * Tests that launch delegate for "TEST_MODE" and java apps is invoked when
	 * "TEST_MODE" is used.
	 * 
	 * @throws CoreException
	 * @see TestModeLaunchDelegate
	 */
	public void testContributedLaunchMode() throws CoreException {
		ILaunch launch = null;
		try {
			fConfiguration = null;
			fMode = null;
			TestModeLaunchDelegate.setTestCase(this);
			ILaunchConfiguration configuration = getLaunchConfiguration("Breakpoints");
			assertNotNull(configuration);
			launch = configuration.launch("TEST_MODE", null);
			assertEquals("Launch delegate not invoked", configuration, fConfiguration);
			assertEquals("Launch delegate not invoked in correct mode", "TEST_MODE", fMode);
		} finally {
			TestModeLaunchDelegate.setTestCase(null);
			fConfiguration = null;
			fMode = null;
			if (launch != null) {
				getLaunchManager().removeLaunch(launch);
			}
		}
	}
	
	/**
	 * Ensure our contributed launch mode exists.
	 */
	public void testLaunchModes() {
		String[] modes = getLaunchManager().getLaunchModes();
		assertContains("Missing TEST_MODE", modes, "TEST_MODE");
		assertContains("Missing debug mode", modes, ILaunchManager.DEBUG_MODE);
		assertContains("Missing run mode", modes, ILaunchManager.RUN_MODE);
	}
	
	/**
	 * Asserts that the array contains the given object
	 */
	static public void assertContains(String message, Object[] array, Object object) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(object)) {
				return;
			}			
		}
		throw new AssertionFailedError(message);
	}	
	
	/**
	 * Ensure our contributed mode is supported.
	 *
	 */
	public void testSupportsMode() throws CoreException {
		ILaunchConfiguration configuration = getLaunchConfiguration("Breakpoints");
		assertNotNull(configuration);
		assertTrue("Java application configuration should support TEST_MODE", configuration.supportsMode("TEST_MODE"));
		assertTrue("Java application type should support TEST_MODE", configuration.getType().supportsMode("TEST_MODE"));
		
		assertTrue("Java application configuration should support debug mode", configuration.supportsMode(ILaunchManager.DEBUG_MODE));
		assertTrue("Java application type should support debug mode", configuration.getType().supportsMode(ILaunchManager.DEBUG_MODE));
		
		assertTrue("Java application configuration should support run mode", configuration.supportsMode(ILaunchManager.RUN_MODE));
		assertTrue("Java application type should support run mode", configuration.getType().supportsMode(ILaunchManager.RUN_MODE));		
	}
}
