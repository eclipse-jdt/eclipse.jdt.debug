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
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPresentationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jdt.debug.testplugin.TestModeLaunchDelegate;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaSourceLookupTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.JavaUI;

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
	public void launch(ILaunchConfiguration configuration, String mode) {
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
	
	/**
	 * Tests that mode specific tab group contributions work.
	 */
	public void testModeSpecificTabGroups() throws CoreException {
		ILaunchConfigurationType javaType = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION); 
		ILaunchConfigurationTabGroup standardGroup = LaunchConfigurationPresentationManager.getDefault().getTabGroup(javaType, ILaunchManager.DEBUG_MODE);
		ILaunchConfigurationTabGroup testGroup = LaunchConfigurationPresentationManager.getDefault().getTabGroup(javaType, "TEST_MODE");
		ILaunchConfigurationDialog dialog = new LaunchConfigurationsDialog(DebugUIPlugin.getShell(), DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP));
		standardGroup.createTabs(dialog, ILaunchManager.DEBUG_MODE);
		testGroup.createTabs(dialog, "TEST_MODE");
		
		ILaunchConfigurationTab[] tabs = standardGroup.getTabs();
		assertEquals("Wrong number of tabs in the standard group", 6, tabs.length);
		assertTrue("Tab 0 should be 'Main'", tabs[0] instanceof JavaMainTab);
		assertTrue("Tab 1 should be 'Arguments'", tabs[1] instanceof JavaArgumentsTab);
		assertTrue("Tab 2 should be 'JRE'", tabs[2] instanceof JavaJRETab);
		assertTrue("Tab 3 should be 'Classpath'", tabs[3] instanceof JavaClasspathTab);
		assertTrue("Tab 4 should be 'Sourcepath'", tabs[4] instanceof JavaSourceLookupTab);
		assertTrue("Tab 5 should be 'Common'", tabs[5] instanceof CommonTab);
		
		tabs = testGroup.getTabs();
		assertEquals("Wrong number of tabs in the test group", 4, tabs.length);
		assertTrue("Tab 0 should be 'Main'", tabs[0] instanceof JavaMainTab);
		assertTrue("Tab 1 should be 'Arguments'", tabs[1] instanceof JavaArgumentsTab);
		assertTrue("Tab 2 should be 'JRE'", tabs[2] instanceof JavaJRETab);
		assertTrue("Tab 3 should be 'Classpath'", tabs[3] instanceof JavaClasspathTab);
		
		standardGroup.dispose();
		testGroup.dispose();
	}
	
	/**
	 * Tests that the default debug persepctive for java apps is debug.
	 */
	public void testDefaultDebugLaunchPerspective() {
		ILaunchConfigurationType javaType = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		assertEquals("Java debug persepctive should be debug", IDebugUIConstants.ID_DEBUG_PERSPECTIVE,
			DebugUITools.getLaunchPerspective(javaType, ILaunchManager.DEBUG_MODE));
	}
	
	/**
	 * Tests that the default run persepctive for java apps is none (<code>null</code>).
	 */
	public void testDefaultRunLaunchPerspective() {
		ILaunchConfigurationType javaType = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		assertNull("Java run persepctive should be null", 
			DebugUITools.getLaunchPerspective(javaType, ILaunchManager.RUN_MODE));
	}	
	
	/**
	 * Tests that the default debug perspective can be overriden and reset
	 */
	public void testResetDebugLaunchPerspective() {
		ILaunchConfigurationType javaType = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		assertEquals("Java debug persepctive should be debug", IDebugUIConstants.ID_DEBUG_PERSPECTIVE,
			DebugUITools.getLaunchPerspective(javaType, ILaunchManager.DEBUG_MODE));
		// set to NONE
		DebugUITools.setLaunchPerspective(javaType, ILaunchManager.DEBUG_MODE, IDebugUIConstants.PERSPECTIVE_NONE);
		assertNull("Java debug persepctive should now be null", 
			DebugUITools.getLaunchPerspective(javaType, ILaunchManager.DEBUG_MODE));
		// re-set to default
		DebugUITools.setLaunchPerspective(javaType, ILaunchManager.DEBUG_MODE, IDebugUIConstants.PERSPECTIVE_DEFAULT);
		assertEquals("Java debug persepctive should now be debug", IDebugUIConstants.ID_DEBUG_PERSPECTIVE,
			DebugUITools.getLaunchPerspective(javaType, ILaunchManager.DEBUG_MODE));
				
	}	
	
	/**
	 * Tests that the default run perspective can be overriden and reset
	 */
	public void testResetRunLaunchPerspective() {
		ILaunchConfigurationType javaType = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		assertNull("Java run persepctive should be null", 
			DebugUITools.getLaunchPerspective(javaType, ILaunchManager.RUN_MODE));
		// set to Java perspective
		DebugUITools.setLaunchPerspective(javaType, ILaunchManager.RUN_MODE, JavaUI.ID_PERSPECTIVE);
		assertEquals("Java run persepctive should now be java", JavaUI.ID_PERSPECTIVE,
					DebugUITools.getLaunchPerspective(javaType, ILaunchManager.RUN_MODE));
		// re-set to default
		DebugUITools.setLaunchPerspective(javaType, ILaunchManager.RUN_MODE, IDebugUIConstants.PERSPECTIVE_DEFAULT);
		assertNull("Java run persepctive should now be null", 
			DebugUITools.getLaunchPerspective(javaType, ILaunchManager.RUN_MODE));		
	}	
	
	/**
	 * Tests a perspective contributed with a launch tab group.
	 */
	public void testContributedLaunchPerspective() {
		ILaunchConfigurationType javaType = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		assertEquals("persepctive for TEST_MODE should be 'java'", JavaUI.ID_PERSPECTIVE,
			DebugUITools.getLaunchPerspective(javaType, "TEST_MODE"));
	}
}
