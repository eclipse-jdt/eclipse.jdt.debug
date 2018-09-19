/*******************************************************************************
 *  Copyright (c) 2007, 2011 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests for the new capability of getting the file encoding for a launch configuration given a variety of
 * scenarios
 * @since 3.4
 */
@SuppressWarnings("deprecation")
public class ConfigurationEncodingTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public ConfigurationEncodingTests(String name) {
		super(name);
	}

	/**
	 * convenience method to the the preference store for the <code>ResourcesPlugin</code>
	 */
	protected Preferences getResourcesPreferences() {
		return ResourcesPlugin.getPlugin().getPluginPreferences();
	}

	/**
	 * Returns the default workbench encoding
	 * @return the default workbench encoding
	 */
	protected String getDefaultEncoding() {
		return getResourcesPreferences().getDefaultString(ResourcesPlugin.PREF_ENCODING);
	}

	/**
	 * Tests that if no encoding is set on the configuration and there is no encoding changes to the workspace pref
	 * than the encoding retrieved for the configuration matches the system encoding
	 */
	public void testGetSystemDefaultEncoding() throws CoreException {
		String oldencoding = ResourcesPlugin.getEncoding();
		String oldsystemencoding = System.getProperty("file.encoding");
		try {
			getResourcesPreferences().setValue(ResourcesPlugin.PREF_ENCODING, getDefaultEncoding());
			System.setProperty("file.encoding", "UTF-16BE");
			ILaunchConfiguration config = getLaunchConfiguration("LaunchHistoryTest");
			assertTrue("the configuration could not be found", config != null);
			assertTrue("there should be no encoding set on the configuration", config.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, (String)null) == null);
			String encoding = getLaunchManager().getEncoding(config);
			assertTrue("The configuration encoding should not be null", encoding != null);
			assertTrue("The configuration encoding should match the file system encoding", encoding.equals(System.getProperty("file.encoding")));
		}
		finally {
			//ensure old encoding is restored
			getResourcesPreferences().setValue(ResourcesPlugin.PREF_ENCODING, (oldencoding == null ? getDefaultEncoding() : oldencoding));
			System.setProperty("file.encoding", oldsystemencoding);
		}
	}

	/**
	 * Tests that if a specific encoding is set for the workspace and there is no encoding set on the configuration
	 * than the returned encoding matches the workspace pref
	 */
	public void testGetSpecificWorkbenchEncoding() throws CoreException {
		String oldencoding = ResourcesPlugin.getEncoding();
		try {
			getResourcesPreferences().setValue(ResourcesPlugin.PREF_ENCODING, "UTF-16");
			ILaunchConfiguration config = getLaunchConfiguration("LaunchHistoryTest");
			assertTrue("the configuration could not be found", config != null);
			assertTrue("there should be no encoding set on the configuration", config.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, (String)null) == null);
			String encoding = getLaunchManager().getEncoding(config);
			assertTrue("The configuration encoding should not be null", encoding != null);
			assertTrue("The configuration encoding should match the workbench preference system encoding", encoding.equals("UTF-16"));
		}
		finally {
			//ensure old encoding is restored
			getResourcesPreferences().setValue(ResourcesPlugin.PREF_ENCODING, (oldencoding == null ? getDefaultEncoding() : oldencoding));
		}
	}

	/**
	 * Tests that if a specific encoding is set on the configuration itself that that is the encoding returned
	 */
	public void testGetSpecificConfigurationEncoding() throws CoreException {
		String oldencoding = ResourcesPlugin.getEncoding();
		ILaunchConfiguration config = getLaunchConfiguration("LaunchHistoryTest");
		assertTrue("the configuration could not be found", config != null);
		IResource[] oldmapped = config.getMappedResources();
		ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
		try {
			getResourcesPreferences().setValue(ResourcesPlugin.PREF_ENCODING, getDefaultEncoding());
			copy.setAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, "UTF-16");
			copy.doSave();
			assertTrue("there should be an encoding set on the configuration", config.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, (String)null) != null);
			String encoding = getLaunchManager().getEncoding(config);
			assertTrue("The configuration encoding should not be null", encoding != null);
			assertTrue("The configuration encoding should match the file system encoding", encoding.equals("UTF-16"));
			copy.setAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, (String)null);
			copy.setMappedResources(null);
			copy.doSave();
		}
		finally {
			//ensure old encoding is restored
			getResourcesPreferences().setValue(ResourcesPlugin.PREF_ENCODING, (oldencoding == null ? getDefaultEncoding() : oldencoding));
			//ensure old resource mappings are restored
			copy.setMappedResources(oldmapped);
			copy.doSave();
		}
	}

	/**
	 * Tests that if there is no specified encoding for the config itself, and there is a mapped resource,
	 * the encoding will come from the mapped resource (if a specific one is set)
	 */
	public void testGetSpecificResourceEncoding() throws CoreException {
		String oldencoding = ResourcesPlugin.getEncoding();
		ILaunchConfiguration config = getLaunchConfiguration("LaunchHistoryTest");
		assertTrue("the configuration could not be found", config != null);
		assertTrue("there should be no encoding set on the configuration", config.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, (String)null) == null);
		IResource[] oldmapped = config.getMappedResources();
		ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
		try {
			getResourcesPreferences().setValue(ResourcesPlugin.PREF_ENCODING, "UTF-16LE");
			IFile res = (IFile) getResource("MigrationTests.java");
			assertTrue("the resource MigrationTests.java should not be null", res != null);
			copy.setMappedResources(new IResource[] {res});
			copy.doSave();
			res.setCharset("UTF-8", null);
			String encoding = getLaunchManager().getEncoding(config);
			assertTrue("The configuration encoding should not be null", encoding != null);
			assertTrue("The configuration encoding should match the file system encoding", encoding.equals(res.getCharset()));
		}
		finally {
			//ensure old encoding is restored
			getResourcesPreferences().setValue(ResourcesPlugin.PREF_ENCODING, (oldencoding == null ? getDefaultEncoding() : oldencoding));
			//ensure old mapped resource is restored
			copy.setMappedResources(oldmapped);
			copy.doSave();
		}
	}

	/**
	 * Tests that if there is no specific encoding set on the config and there is more than one mapped resource,
	 * the first mapped resource is used to derive the encoding
	 */
	public void testGetSpecificResourcesEncoding() throws CoreException {
		String oldencoding = ResourcesPlugin.getEncoding();
		IFile res = (IFile) getResource("MigrationTests.java"),
		res2 = (IFile) getResource("MigrationTests2.java");
		String resCharset = res.getCharset();
		String res2Charset = res2.getCharset();
		ILaunchConfiguration config = getLaunchConfiguration("LaunchHistoryTest");
		assertTrue("the configuration could not be found", config != null);
		assertTrue("there should be no encoding set on the configuration", config.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, (String)null) == null);
		IResource[] oldmapped = config.getMappedResources();
		ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
		try {
			getResourcesPreferences().setValue(ResourcesPlugin.PREF_ENCODING, "UTF-16LE");
			assertTrue("the resource MigrationTests.java should not be null", res != null);
			assertTrue("the resource MigrationTests2.java should not be null", res2 != null);
			copy.setMappedResources(new IResource[] {res, res2});
			copy.doSave();
			res.setCharset("UTF-16BE", null);
			res2.setCharset("UTF-8", null);
			String encoding = getLaunchManager().getEncoding(config);
			assertTrue("The configuration encoding should not be null", encoding != null);
			assertTrue("The configuration encoding should match the file system encoding", encoding.equals(res.getCharset()));
			copy.setMappedResources(null);
			copy.doSave();
		}
		finally {
			//ensure old encoding is restored
			getResourcesPreferences().setValue(ResourcesPlugin.PREF_ENCODING, (oldencoding == null ? getDefaultEncoding() : oldencoding));
			res.setCharset(resCharset, null);
			res2.setCharset(res2Charset, null);
			//ensure old mapping is restored
			copy.setMappedResources(oldmapped);
			copy.doSave();
		}
	}

}
