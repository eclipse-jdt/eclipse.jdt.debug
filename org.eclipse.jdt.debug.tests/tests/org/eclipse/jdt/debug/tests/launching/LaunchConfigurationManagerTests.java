/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.launching;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * This class tests all the public methods of the launch configuration manager
 * @since 3.4 
 */
public class LaunchConfigurationManagerTests extends AbstractDebugTest {

	private LaunchConfigurationManager fLCM = DebugUIPlugin.getDefault().getLaunchConfigurationManager();
	
	public LaunchConfigurationManagerTests(String name) {
		super(name);
	}

	/**
	 * tests that the singleton object returned is always the same
	 */
	public void testSingleton() {
		assertNotNull("The launch configuration manager cannot be null", fLCM);
		assertEquals("The retruned instance of the manager should always be the same", fLCM, DebugUIPlugin.getDefault().getLaunchConfigurationManager());
	}
	
	/**
	 * tests that shortcut extensions are loaded properly by the manager
	 */
	public void testGetLaunchShortcuts() {
		assertNotNull("The launch configuration manager cannot be null", fLCM);
		List list = fLCM.getLaunchShortcuts();
		assertTrue("The listing of shortcuts cannot be empty", list.size() > 2);
	}
	
	/**
	 * tests that the java shortcut is found for a resource with a main method
	 */
	public void testGetJavaLaunchShortcutsForSpecificResource() {
		assertNotNull("The launch configuration manager cannot be null", fLCM);
		IResource resource = getResource("ThrowsNPE.java");
		assertNotNull("The resource ThrowsNPE must exist", resource);
		List list = fLCM.getLaunchShortcuts(resource);
		assertTrue("Only the java shortcut should apply", list.size() == 1);
		LaunchShortcutExtension ext = (LaunchShortcutExtension) list.get(0);
		assertTrue("the returned shortcut should be the java one", ext.getId().equals("org.eclipse.jdt.debug.ui.localJavaShortcut"));
	}
	
	/**
	 * tests that the applet shortcut is found for a resource that extends applet
	 */
	public void testGetAppletLaunchShortcutsForSpecificResource() {
		assertNotNull("The launch configuration manager cannot be null", fLCM);
		IResource resource = getResource("AppletImpl.java");
		assertNotNull("The resource AppletImpl must exist", resource);
		List list = fLCM.getLaunchShortcuts(resource);
		assertTrue("Only the applet shortcut should apply", list.size() == 1);
		LaunchShortcutExtension ext = (LaunchShortcutExtension) list.get(0);
		assertTrue("the returned shortcut should be the applet one", ext.getId().equals("org.eclipse.jdt.debug.ui.javaAppletShortcut"));
	}
	
	/**
	 * test that the correct shortcuts are returned for a resource which is an applet and has a main method
	 */
	public void testGetLaunchShortcutsForSpecificResource() {
		assertNotNull("The launch configuration manager cannot be null", fLCM);
		IResource resource = getResource("RunnableAppletImpl.java");
		assertNotNull("The resource RunnableAppletImpl must exist", resource);
		List list = fLCM.getLaunchShortcuts(resource);
		assertTrue("The java and applet shortcuts should apply", list.size() == 2);
		LaunchShortcutExtension ext = (LaunchShortcutExtension) list.get(0);
		assertTrue("the shortcut should be either the applet or java one", ext.getId().equals("org.eclipse.jdt.debug.ui.localJavaShortcut") || ext.getId().equals("org.eclipse.jdt.debug.ui.javaAppletShortcut"));
		ext = (LaunchShortcutExtension) list.get(1);
		assertTrue("the shortcut should be either the applet or java one", ext.getId().equals("org.eclipse.jdt.debug.ui.localJavaShortcut") || ext.getId().equals("org.eclipse.jdt.debug.ui.javaAppletShortcut"));
	}
	
	/**
	 * tests that shortcuts for given categories are returned, in this case the 'testing' category
	 */
	public void testGetLaunchShortcutsForCategory() {
		assertNotNull("The launch configuration manager cannot be null", fLCM);
		List list = fLCM.getLaunchShortcuts("testing");
		assertTrue("there should be the testing shortcut", list.size() == 1);
	}
	
	/**
	 * tests that a specific launch group can be found given its id
	 */
	public void testGetLaunchGroupForId() {
		assertNotNull("The launch configuration manager cannot be null", fLCM);
		LaunchGroupExtension ext = fLCM.getLaunchGroup("org.eclipse.debug.ui.launchGroup.run");
		assertNotNull("the run launch group should exist", ext);
	}
	
	/**
	 * tests that a launch group exists for a given configuration and mode,
	 * in this case we are testing that a group exists for a java application config
	 * in debug mode
	 */
	public void testGetLaunchGroupForConfigurationAndMode() {
		assertNotNull("The launch configuration manager cannot be null", fLCM);
		ILaunchConfiguration configuration = getLaunchConfiguration("ThrowsNPE");
		assertNotNull("the ThrowsNPE configuration should exist", configuration);
		ILaunchGroup group = DebugUITools.getLaunchGroup(configuration, "debug");
		assertNotNull("the launch group for a java app config in debug mode should exist", group);
	}
	
	/**
	 * tests that all of the launch groups can be acquired, there should be at least 3 of
	 * them (run, debug, profile)
	 */
	public void testGetAllLaunchGroups() {
		assertNotNull("The launch configuration manager cannot be null", fLCM);
		ILaunchGroup[] groups = fLCM.getLaunchGroups();
		assertNotNull("the listing of launch groups cannot be null", groups);
		assertTrue("there shoulod be at least 3 launch groups", groups.length > 2);
	}
	
	/**
	 * tests that the default launch group for run mode is the launch group
	 * contributed by debug
	 */
	public void testGetDefaultLaunchGroupForRunMode() {
		assertNotNull("The launch configuration manager cannot be null", fLCM);
		ILaunchGroup group = fLCM.getDefaultLaunchGroup("run");
		assertNotNull("the default launch group cannot be null", group);
		assertTrue("the default launch group for run mode should be the debug contribution", group.getIdentifier().equals("org.eclipse.debug.ui.launchGroup.run"));
	}
	
	/**
	 * tests that the default launch group for debug mode is the launch group
	 * contributed by debug
	 */
	public void testGetDefaultLaunchGroupForDebugMode() {
		assertNotNull("The launch configuration manager cannot be null", fLCM);
		ILaunchGroup group = fLCM.getDefaultLaunchGroup("debug");
		assertNotNull("the default launch group cannot be null", group);
		assertTrue("the default launch group for debug mode should be the debug contribution", group.getIdentifier().equals("org.eclipse.debug.ui.launchGroup.debug"));
	}
	
	/**
	 * tests that the correct corresponding launch config types are found for a given resource,
	 * in this test the applet and local java types should be found
	 */
	public void testGetApplicableLaunchConfigurationTypes() {
		assertNotNull("The launch configuration manager cannot be null", fLCM);
		IResource resource = getResource("RunnableAppletImpl.java");
		assertNotNull("The resource RunnableAppletImpl must exist", resource);
		String[] types = fLCM.getApplicableConfigurationTypes(resource);
		assertNotNull("this listing should never be null", types);
		assertTrue("there should be 2 types: java and applet", types.length == 2);
		assertTrue("The type should be either applet or local java application", types[0].equals("org.eclipse.jdt.launching.localJavaApplication") || types[0].equals("org.eclipse.jdt.launching.javaApplet"));
		assertTrue("The type should be either applet or local java application", types[1].equals("org.eclipse.jdt.launching.localJavaApplication") || types[1].equals("org.eclipse.jdt.launching.javaApplet"));
	}
	
	/**
	 * tests that configuration applicable to specific resources can be found
	 */
	public void testGetApplicableLaunchConfigurationsForResource() {
		assertNotNull("The launch configuration manager cannot be null", fLCM);
		IResource resource = getResource("RunnableAppletImpl.java");
		assertNotNull("The resource RunnableAppletImpl must exist", resource);
		String[] list = fLCM.getApplicableConfigurationTypes(resource);
		assertNotNull("the listing cannot be null", list);
		assertTrue("there should be at least one configuration for this resource", list.length > 0);
	}
	
	/**
	 * Tests that a launch group does not exist for a given configuration and mode that it does not support.
	 * In this case we are testing that a group does not exist for a java application config
	 * in profile mode
	 */
	public void testGetLaunchGroupForConfigurationAndUnsupportedMode() {
		assertNotNull("The launch configuration manager cannot be null", fLCM);
		ILaunchConfiguration configuration = getLaunchConfiguration("ThrowsNPE");
		assertNotNull("the ThrowsNPE configuration should exist", configuration);
		ILaunchGroup group = DebugUITools.getLaunchGroup(configuration, ILaunchManager.PROFILE_MODE);
		assertNull("the launch group for a java app config in profile mode should *not* exist", group);
	}	
}