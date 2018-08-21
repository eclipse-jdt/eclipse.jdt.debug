/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
import org.eclipse.jdt.debug.testplugin.AlternateDelegate;
import org.eclipse.jdt.debug.testplugin.launching.TestLaunchDelegate1;
import org.eclipse.jdt.debug.testplugin.launching.TestLaunchDelegate2;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * Tests for launch delegates
 */
public class LaunchDelegateTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public LaunchDelegateTests(String name) {
		super(name);
	}

	/**
	 * Ensures a launch delegate can provide a launch object for
	 * a launch.
	 * @throws CoreException
	 */
	public void testProvideLaunch() throws CoreException {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType configurationType = manager.getLaunchConfigurationType("org.eclipse.jdt.debug.tests.testConfigType"); //$NON-NLS-1$
		assertNotNull("Missing test launch config type", configurationType); //$NON-NLS-1$
		ILaunchConfigurationWorkingCopy workingCopy = configurationType.newInstance(null, "provide-launch-object"); //$NON-NLS-1$
		// delegate will throw exception if test fails
		ILaunch launch = workingCopy.launch(ILaunchManager.DEBUG_MODE, null);
		manager.removeLaunch(launch);
	}

	/**
	 * Tests that a delegate extension can provide the source path computer.
	 */
	public void testSourcePathComputerExtension() {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType configurationType = manager.getLaunchConfigurationType("org.eclipse.jdt.debug.tests.testConfigType"); //$NON-NLS-1$
		assertNotNull("Missing test launch config type", configurationType); //$NON-NLS-1$
		ISourcePathComputer sourcePathComputer = configurationType.getSourcePathComputer();
		assertEquals("Wrong source path computer", "org.eclipse.jdt.debug.tests.testSourcePathComputer", sourcePathComputer.getId()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Tests that a delegate extension can provide the source locator.
	 */
	public void testSourceLocatorExtension() {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType configurationType = manager.getLaunchConfigurationType("org.eclipse.jdt.debug.tests.testConfigType"); //$NON-NLS-1$
		assertNotNull("Missing test launch config type", configurationType); //$NON-NLS-1$
		String sourceLocatorId = configurationType.getSourceLocatorId();
		assertEquals("Wrong source locater id", "org.eclipse.jdt.debug.tests.testSourceLocator", sourceLocatorId); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Test launch delegate for mixed launch mode.
	 * @throws CoreException
	 */
	public void testMixedModeDelegate() throws CoreException {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		assertNotNull("Missing java application launch config type", type); //$NON-NLS-1$
		HashSet<String> modes = new HashSet<>();
		modes.add("alternate"); //$NON-NLS-1$
		modes.add(ILaunchManager.DEBUG_MODE);
		assertTrue("Should support mixed mode (alternate/debug)", type.supportsModeCombination(modes)); //$NON-NLS-1$
		ILaunchDelegate[] delegates = type.getDelegates(modes);
		assertTrue("missing delegate", delegates.length > 0); //$NON-NLS-1$
		assertEquals("Wrong number of delegates", 1, delegates.length); //$NON-NLS-1$
		assertTrue("Wrong delegate", delegates[0].getDelegate() instanceof AlternateDelegate); //$NON-NLS-1$
	}

	/**
	 * Tests if the java launch delegate was found as one of the delegates for debug mode.
	 * @throws CoreException
	 */
	public void testSingleDebugModeDelegate() throws CoreException {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		assertNotNull("Missing java application launch config type", type); //$NON-NLS-1$
		HashSet<String> modes = new HashSet<>();
		modes.add(ILaunchManager.DEBUG_MODE);
		assertTrue("Should support mode (debug)", type.supportsModeCombination(modes)); //$NON-NLS-1$
		ILaunchDelegate[] delegates = type.getDelegates(modes);
		assertTrue("missing delegate", delegates.length > 0); //$NON-NLS-1$
		boolean found = false;
		for(int i = 0; i < delegates.length; i++) {
			if(delegates[i].getDelegate().getClass().getName().endsWith("JavaLaunchDelegate")) { //$NON-NLS-1$
				found = true;
				break;
			}
		}
		assertTrue("The java launch delegate was not one of the returned delegates", found);		 //$NON-NLS-1$
	}

	/**
	 * Tests correct delegate is found for alternate mode.
	 * @throws CoreException
	 */
	public void testSingleAlternateModeDelegate() throws CoreException {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		assertNotNull("Missing java application launch config type", type); //$NON-NLS-1$
		HashSet<String> modes = new HashSet<>();
		modes.add("alternate"); //$NON-NLS-1$
		assertTrue("Should support mode (alternate)", type.supportsModeCombination(modes)); //$NON-NLS-1$
		ILaunchDelegate[] delegates = type.getDelegates(modes);
		assertTrue("missing delegate", delegates.length > 0); //$NON-NLS-1$
		assertEquals("Wrong number of delegates", 1, delegates.length); //$NON-NLS-1$
		assertTrue("Wrong delegate", delegates[0].getDelegate() instanceof AlternateDelegate);		 //$NON-NLS-1$
	}

	/**
	 * Checks that the delegate definition is collecting and parsing mode combination information properly from both the delegate
	 * contribution and from modeCombination child elements
	 * @throws CoreException
	 */
	public void testMultipleModeSingleDelegate() throws CoreException {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		assertNotNull("Missing java application launch config type", type); //$NON-NLS-1$
		String[][] modesavail = {{"alternate"}, {"debug", "alternate"}, {"debug", "alternate2"}}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		HashSet<String> modes = null;
		for(int i = 0; i < modesavail.length; i++) {
			modes = new HashSet<>(Arrays.asList(modesavail[i]));
			assertTrue("Should support modes "+modes.toString(), type.supportsModeCombination(modes)); //$NON-NLS-1$
			ILaunchDelegate[] delegates = type.getDelegates(modes);
			assertTrue("missing delegate", delegates.length > 0); //$NON-NLS-1$
			assertEquals("Wrong number of delegates", 1, delegates.length); //$NON-NLS-1$
			assertTrue("Wrong delegate", delegates[0].getDelegate() instanceof AlternateDelegate);	 //$NON-NLS-1$
		}
	}

	/**
	 * Checks that all applicable delegates are found for given types and mode combinations
	 * @throws CoreException
	 */
	public void testSingleModeMultipleDelegates() throws CoreException {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		assertNotNull("Missing java application launch config type", type); //$NON-NLS-1$
		HashSet<String> modes = new HashSet<>();
		modes.add("alternate2"); //$NON-NLS-1$
		assertTrue("Should support mode 'alternate2'", type.supportsModeCombination(modes)); //$NON-NLS-1$
		ILaunchDelegate[] delegates = type.getDelegates(modes);
		assertTrue("missing delegate", delegates.length > 0); //$NON-NLS-1$
		assertEquals("Wrong number of delegates, should be 2 only "+delegates.length+" found", 2, delegates.length); //$NON-NLS-1$ //$NON-NLS-2$
		HashSet<Class<? extends ILaunchConfigurationDelegate>> dels = new HashSet<>();
		for(int i = 0; i < delegates.length; i++) {
			dels.add(delegates[i].getDelegate().getClass());
		}
		HashSet<Object> ds = new HashSet<>(Arrays.asList(new Object[] {TestLaunchDelegate1.class, TestLaunchDelegate2.class}));
		assertTrue("There must be only TestLaunchDelegate1 and TestLaunchDelegate2 as registered delegates for the mode alternate2 and the local java type", ds.containsAll(dels)); //$NON-NLS-1$
	}

	/**
	 * Checks to see the even with a partial match of a mode combination it will indicate that it does not support the specified modes
	 */
	public void testPartialModeCombination() {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		assertNotNull("Missing java application launch config type", type); //$NON-NLS-1$
		HashSet<String> modes = new HashSet<>();
		modes.add("alternate2"); //$NON-NLS-1$
		modes.add("foo"); //$NON-NLS-1$
		assertTrue("Should not support modes: "+modes.toString(), !type.supportsModeCombination(modes)); //$NON-NLS-1$
	}
}
