/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * Tests the capabilities of launch shortcuts from the <code>LaunchShortcuts</code> extension point
 *
 * @since 3.3
 */
@SuppressWarnings("deprecation")
public class LaunchShortcutTests extends AbstractDebugTest {

	private static String TESTING = "testing"; //$NON-NLS-1$

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
		LaunchShortcutExtension ext = getLaunchShortcutExtension(JAVA_LAUNCH_SHORTCUT_ID);
		assertNotNull("java app shortcut not found", ext); //$NON-NLS-1$
		String typeid = "org.eclipse.jdt.launching.localJavaApplication"; //$NON-NLS-1$
		assertTrue("local java app shortcut should support java app types", ext.getAssociatedConfigurationTypes().contains(typeid)); //$NON-NLS-1$
	}

	/**
	 * Tests that the local java app shortcut does not support some fake type id 'foo'
	 */
	public void testAssociatedConfigurationTypeNotSupported() {
		LaunchShortcutExtension ext = getLaunchShortcutExtension(JAVA_LAUNCH_SHORTCUT_ID);
		assertNotNull("java app shortcut not found", ext); //$NON-NLS-1$
		String typeid = "org.eclipse.jdt.launching.foo"; //$NON-NLS-1$
		assertTrue("local java app shortcut should not support foo", !ext.getAssociatedConfigurationTypes().contains(typeid)); //$NON-NLS-1$
	}

	/**
	 * Tests that the java app shortcut supports debug and java perspectives
	 */
	public void testAssociatedPespectiveSupported() {
		LaunchShortcutExtension ext = getLaunchShortcutExtension(TEST_LAUNCH_SHORTCUT);
		assertNotNull("java app shortcut not found", ext); //$NON-NLS-1$
		assertTrue("java app shortcut should support debug perspective", ext.getPerspectives().contains("org.eclipse.debug.ui.DebugPerspective")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Tests that the local java app shortcut does not support some fake perspective foo
	 */
	public void testAssociatedPerspectiveNotSupported() {
		LaunchShortcutExtension ext = getLaunchShortcutExtension(TEST_LAUNCH_SHORTCUT);
		assertNotNull("test shortcut not found", ext); //$NON-NLS-1$
		assertTrue("java app shortcut should not support foo perspective", !ext.getPerspectives().contains("org.eclipse.debug.ui.FooPerspective")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Tests that the testing launch shortcut can be found based on specified perspective and category
	 */
	public void testGetLaunchShortcutPerspectiveCategory() {
		LaunchConfigurationManager lcm = getLaunchConfigurationManager();
		assertNotNull("launch configuration manager cannot be null", lcm); //$NON-NLS-1$
		assertTrue("there should be one shortcut for the debug perspective and testing category", lcm.getLaunchShortcuts("org.eclipse.debug.ui.DebugPerspective", TESTING).size() == 1); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Tests that the testing launch shortcut can be found based on the specified category
	 */
	public void testGetLaunchShortcutCategory() {
		LaunchConfigurationManager lcm = getLaunchConfigurationManager();
		assertNotNull("launch configuration manager cannot be null", lcm); //$NON-NLS-1$
		assertTrue("there should be one shortcut for the testing category", lcm.getLaunchShortcuts(TESTING).size() == 1); //$NON-NLS-1$
	}

	/**
	 * Tests that shortcuts can be found based on the specified launch configuration type id.
	 * For this test there should be a minimum of two shortcuts found.
	 */
	public void testGetApplicableLaunchShortcuts() {
		assertTrue("there should be 2 or more shortcuts", getApplicableLaunchShortcuts("org.eclipse.jdt.launching.localJavaApplication").size() >= 2); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Tests that a description can be retrieved for a specified mode when it is the general description,
	 * i.e. that the description has been provided in the shortcut definition and is NOT a description child element
	 * @since 3.3
	 */
	public void testGetGeneralShortcutDescription() {
		LaunchConfigurationManager lcm = getLaunchConfigurationManager();
		assertNotNull("launch configuration manager cannot be null", lcm); //$NON-NLS-1$
		List<LaunchShortcutExtension> list = lcm.getLaunchShortcuts(TESTING);
		assertTrue("There must be at least one testing shortcut", list.size() > 0); //$NON-NLS-1$
		LaunchShortcutExtension ext = list.get(0);
		String descr = ext.getShortcutDescription("debug"); //$NON-NLS-1$
		assertNotNull("The description should not be null for debug mode", descr); //$NON-NLS-1$
		assertTrue("The description should match the general one: General Description", descr.equals("General Description")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Test that the general shortcut description provided is over-loaded with the one
	 * specifically provided for the run mode. i.e. the run mode description is provided as a
	 * child element for the launch shortcut
	 * @since 3.3
	 */
	public void testGetRunShortcutDescription() {
		LaunchConfigurationManager lcm = getLaunchConfigurationManager();
		assertNotNull("launch configuration manager cannot be null", lcm); //$NON-NLS-1$
		List<LaunchShortcutExtension> list = lcm.getLaunchShortcuts(TESTING);
		assertTrue("There must be at least one testing shortcut", list.size() > 0); //$NON-NLS-1$
		LaunchShortcutExtension ext = list.get(0);
		String descr = ext.getShortcutDescription("run"); //$NON-NLS-1$
		assertNotNull("The description should not be null for run mode", descr); //$NON-NLS-1$
		assertTrue("The description should match the specific run one: Run Description", descr.equals("Run Description")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns a listing of all applicable <code>LaunchShortcutExtension</code>s for the given
	 * launch configuration type id.
	 * @param typeid the id of the launch configuration
	 * @return a listing of <code>LaunchShortcutExtension</code>s that are associated with the specified launch configuration
	 * type id or an empty list, never <code>null</code>
	 *
	 * @since 3.3
	 */
	public List<LaunchShortcutExtension> getApplicableLaunchShortcuts(String typeid) {
		List<LaunchShortcutExtension> list = new ArrayList<>();
		LaunchShortcutExtension ext = null;
		List<LaunchShortcutExtension> shortcuts = getLaunchConfigurationManager().getLaunchShortcuts();
		for(int i = 0; i < shortcuts.size(); i++) {
			ext = shortcuts.get(i);
			if(ext.getAssociatedConfigurationTypes().contains(typeid) && !WorkbenchActivityHelper.filterItem(ext)) {
				list.add(ext);
			}
		}
		return list;
	}
}
