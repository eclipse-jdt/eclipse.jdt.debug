/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.launching;

import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests the capabilities of launch shortcuts from the <code>LaunchShortcuts</code> extension point
 * 
 * @since 3.3
 */
public class LaunchShortcutTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public LaunchShortcutTests(String name) {
		super(name);
	}
	
	/**
	 * Tests to see that the local java launch shortcut supports the java
	 * application launch configuration type
	 */
	public void testAssociatedConfigurationTypeSupported() {
		LaunchShortcutExtension ext = getJavaApplicationLaunchShortcut();
		assertNotNull("java app shortcut not found", ext);
		String typeid = "org.eclipse.jdt.launching.localJavaApplication";
		assertTrue("local java app shortcut should support java app types", ext.getAssociatedConfigurationTypes().contains(typeid));
	}

	/**
	 * Tests that the local java app shortcut does not support some fake type id 'foo'
	 */
	public void testAssociatedConfigurationTypeNotSupported() {
		LaunchShortcutExtension ext = getJavaApplicationLaunchShortcut();
		assertNotNull("java app shortcut not found", ext);
		String typeid = "org.eclipse.jdt.launching.foo";
		assertTrue("local java app shortcut should not support foo", !ext.getAssociatedConfigurationTypes().contains(typeid));
	}
	
	/**
	 * Tests that the java app shortcut supports debug and java perspectives
	 */
	public void testAssociatedPespectiveSupported() {
		LaunchShortcutExtension ext = getJavaApplicationLaunchShortcut();
		assertNotNull("java app shortcut not found", ext);
		assertTrue("java app shortcut should support debug perspective", ext.getPerspectives().contains("org.eclipse.debug.ui.DebugPerspective"));
		assertTrue("java app shortcut should support java perspective", ext.getPerspectives().contains("org.eclipse.jdt.ui.JavaPerspective"));
	}
	
	/**
	 * Tests that the local java app shortcut does not support some fake perspective foo
	 */
	public void testAssociatedPerspectiveNotSupported() {
		LaunchShortcutExtension ext = getJavaApplicationLaunchShortcut();
		assertNotNull("java app shortcut not found", ext);
		assertTrue("java app shortcut should not support foo perspective", !ext.getPerspectives().contains("org.eclipse.debug.ui.FooPerspective"));
	}
	
	/**
	 * Tests that the testing launch shortcut can be found based on specified perspective and category
	 */
	public void testGetLaunchShortcutPerspectiveCategory() {
		LaunchConfigurationManager lcm = getLaunchConfigurationManager();
		assertNotNull("launch configuration manager cannot be null", lcm);
		assertTrue("there should be one shortcut for the debug perspective and testing category", lcm.getLaunchShortcuts("org.eclipse.debug.ui.DebugPerspective", "testing").size() == 1);
	}
	
	/**
	 * Tests that the testing launch shortcut can be found based on the specified category
	 */
	public void testGetLaunchShortcutCategory() {
		LaunchConfigurationManager lcm = getLaunchConfigurationManager();
		assertNotNull("launch configuration manager cannot be null", lcm);
		assertTrue("there should be one shortcut for the testing category", lcm.getLaunchShortcuts("testing").size() == 1);
	}
	
	/**
	 * Tests that shortcuts can be found based on the specified launch configuration type id.
	 * For this test there should be a minimum of two shortcuts found.
	 */
	public void testGetApplicableLaunchShortcuts() {
		LaunchConfigurationManager lcm = getLaunchConfigurationManager();
		assertNotNull("launch configuration manager cannot be null", lcm);
		assertTrue("there should be 2 or more shortcuts", lcm.getApplicableLaunchShortcuts("org.eclipse.jdt.launching.localJavaApplication").size() >= 2);
	}
}
