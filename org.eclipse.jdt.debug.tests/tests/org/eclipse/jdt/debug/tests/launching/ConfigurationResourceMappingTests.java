/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests that resource mappings can be correctly set and retrieved on launch configurations
 * @since 3.4
 */
public class ConfigurationResourceMappingTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public ConfigurationResourceMappingTests(String name) {
		super(name);
	}

	/**
	 * Tests that setting the mapped resources to <code>null</code> removes
	 * the resource mapping
	 */
	public void testRemovingMappedResources1() throws CoreException {
		ILaunchConfiguration config = getLaunchConfiguration("MigrationTests");
		assertTrue("the configuration cannot be null", config != null);
		ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
		IResource[] mapped = copy.getMappedResources();
		try {
			if(mapped == null) {
				//map some
				IResource res = getResource("MigrationTests.java");
				assertTrue("the resource MigrationTests.java should not be null", res != null);
				copy.setMappedResources(new IResource[] {res});
				copy.doSave();
				assertTrue("a resource mapping should have been added", copy.getMappedResources() != null);
			}
			copy.setMappedResources(null);
			copy.doSave();
			assertTrue("the mapped resources should have been removed", copy.getMappedResources() == null);
		}
		finally {
			//put back any mappings that might have been there
			copy.setMappedResources(mapped);
			copy.doSave();
		}
	}

	/**
	 * Tests that setting the resource mapping to an empty array of <code>IResource</code> removes
	 * the resource mapping
	 */
	public void testRemovingMappedResources2() throws CoreException {
		ILaunchConfiguration config = getLaunchConfiguration("MigrationTests");
		assertTrue("the configuration cannot be null", config != null);
		ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
		IResource[] mapped = copy.getMappedResources();
		try {
			if(mapped == null) {
				//map some
				IResource res = getResource("MigrationTests.java");
				assertTrue("the resource MigrationTests.java should not be null", res != null);
				copy.setMappedResources(new IResource[] {res});
				copy.doSave();
				assertTrue("a resource mapping should have been added", copy.getMappedResources() != null);
			}
			copy.setMappedResources(new IResource[0]);
			copy.doSave();
			assertTrue("the mapped resources should have been removed", copy.getMappedResources() == null);
		}
		finally {
			//put back any mappings that might have been there
			copy.setMappedResources(mapped);
			copy.doSave();
		}
	}

	/**
	 * Tests that a single element array can be set as a mapped resource
	 */
	public void testSetMappedResource() throws CoreException {
		ILaunchConfiguration config = getLaunchConfiguration("MigrationTests");
		assertTrue("the configuration cannot be null", config != null);
		ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
		IResource res = getResource("MigrationTests.java");
		assertTrue("the resource MigrationTests.java should not be null", res != null);
		IResource[] oldmapped = copy.getMappedResources();
		try {
			copy.setMappedResources(new IResource[] {res});
			copy.doSave();
			IResource[] mapped = copy.getMappedResources();
			assertTrue("there should only be one resource mapped", mapped.length == 1);
			assertTrue("the one resource should be MigrationTests.java", mapped[0].equals(res));
		}
		finally {
			//put back any mappings that might have been there
			copy.setMappedResources(oldmapped);
			copy.doSave();
		}
	}

	/**
	 * Tests that > 1 resource can be mapped correctly
	 * @throws CoreException
	 */
	public void testSetMappedResources() throws CoreException {
		ILaunchConfiguration config = getLaunchConfiguration("MigrationTests");
		assertTrue("the configuration cannot be null", config != null);
		ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
		IResource res = getResource("MigrationTests.java"),
				res2 = getResource("MigrationTests2.java");
		assertTrue("the resource MigrationTests.java should not be null", res != null);
		assertTrue("the resource MigrationTests2.java should not be null", res2 != null);
		IResource[] oldmapped = copy.getMappedResources();
		try {
			copy.setMappedResources(new IResource[] {res, res2});
			copy.doSave();
			IResource[] mapped = copy.getMappedResources();
			assertTrue("there should be two resources mapped", mapped.length == 2);
			assertTrue("the first resource should be MigrationTests.java", mapped[0].equals(res));
			assertTrue("the second resource should be MigrationTests2.java", mapped[1].equals(res2));
		}
		finally {
			//put back any mappings that might have been there
			copy.setMappedResources(oldmapped);
			copy.doSave();
		}
	}

}
