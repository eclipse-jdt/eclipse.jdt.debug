/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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

import org.eclipse.core.resources.IContainer;
import org.eclipse.debug.internal.core.LaunchConfiguration;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Class for testing method from the class {@link LaunchConfiguration}
 * @since 3.7
 */
public class LaunchConfigurationTests extends AbstractDebugTest {

	/**
	 * A configuration for testing
	 */
	class TestConfiguration extends LaunchConfiguration {

		/**
		 * Constructor
		 * @param name
		 * @param container
		 */
		protected TestConfiguration(String name, IContainer container) {
			super(name, container);
		}

		public String getSimpleNameProxy(String fileName) {
			return getSimpleName(fileName);
		}

		public void setNameProxy(String name) {
			setName(name);
		}

		public String getFileNameProxy() {
			return getFileName();
		}
	}

	TestConfiguration config = new TestConfiguration("Test", null);

	/**
	 * Constructor
	 * @param name
	 */
	public LaunchConfigurationTests(String name) {
		super(name);
	}

	/**
	 * Tests the {@link LaunchConfiguration#getSimpleName} method
	 *
	 * @throws Exception
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=332410
	 */
	public void testGetSimpleName() throws Exception {
		//filenames with the ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION as an extension
		String name = config.getSimpleNameProxy("launch.launch");
		assertEquals("Did not get expected name: 'launch'", "launch", name);
		name = config.getSimpleNameProxy("launch.foo.launch");
		assertEquals("Did not get expected name: 'launch.foo'", "launch.foo", name);
		name = config.getSimpleNameProxy("launch.foo.bar.launch");
		assertEquals("Did not get expected name: 'launch.foo.bar'", "launch.foo.bar", name);
		//filenames without the ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION extension
		name = config.getSimpleNameProxy("launch");
		assertEquals("Did not get expected name: 'launch'", "launch", name);
		name = config.getSimpleNameProxy("launch.foo");
		assertEquals("Did not get expected name: 'launch.foo'", "launch.foo", name);
		name = config.getSimpleNameProxy("launch.foo.bar");
		assertEquals("Did not get expected name: 'launch.foo.bar'", "launch.foo.bar", name);
	}

	/**
	 * Tests the {@link LaunchConfiguration#setName} method
	 *
	 * @throws Exception
	 */
	public void testSetName() throws Exception {
		assertEquals("The default name should be: 'Test'", config.getName(), "Test");
		config.setNameProxy("newname");
		assertEquals("The new name should be: 'newname'", config.getName(), "newname");
		//reset the name
		config.setNameProxy("Test");
	}

	/**
	 * Tests the {@link LaunchConfiguration#getFileName} method
	 *
	 * @throws Exception
	 */
	public void testGetFileName() throws Exception {
		String filename = config.getFileNameProxy();
		assertEquals("The filename should be: 'Test.launch'", "Test.launch", filename);
		config.setNameProxy("launch");
		filename = config.getFileNameProxy();
		assertEquals("The filename should be: 'launch.launch'", "launch.launch", filename);
		config.setNameProxy("launch.foo");
		filename = config.getFileNameProxy();
		assertEquals("The filename should be: 'launch.foo.launch'", "launch.foo.launch", filename);
		//reset the name
		config.setNameProxy("Test");
	}
}
