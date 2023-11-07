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
		assertNotNull("the configuration cannot be null", config);
		ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
		IResource[] mapped = copy.getMappedResources();
		try {
			if(mapped == null) {
				//map some
				IResource res = getResource("MigrationTests.java");
				assertNotNull("the resource MigrationTests.java should not be null", res);
				copy.setMappedResources(new IResource[] {res});
				copy.doSave();
				assertNotNull("a resource mapping should have been added", copy.getMappedResources());
			}
			copy.setMappedResources(null);
			copy.doSave();
			assertNull("the mapped resources should have been removed", copy.getMappedResources());
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
		assertNotNull("the configuration cannot be null", config);
		ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
		IResource[] mapped = copy.getMappedResources();
		try {
			if(mapped == null) {
				//map some
				IResource res = getResource("MigrationTests.java");
				assertNotNull("the resource MigrationTests.java should not be null", res);
				copy.setMappedResources(new IResource[] {res});
				copy.doSave();
				assertNotNull("a resource mapping should have been added", copy.getMappedResources());
			}
			copy.setMappedResources(new IResource[0]);
			copy.doSave();
			assertNull("the mapped resources should have been removed", copy.getMappedResources());
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
		assertNotNull("the configuration cannot be null", config);
		ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
		IResource res = getResource("MigrationTests.java");
		assertNotNull("the resource MigrationTests.java should not be null", res);
		IResource[] oldmapped = copy.getMappedResources();
		try {
			copy.setMappedResources(new IResource[] {res});
			copy.doSave();
			IResource[] mapped = copy.getMappedResources();
			assertEquals("there should only be one resource mapped", 1, mapped.length);
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
	 */
	public void testSetMappedResources() throws CoreException {
		ILaunchConfiguration config = getLaunchConfiguration("MigrationTests");
		assertNotNull("the configuration cannot be null", config);
		ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
		IResource res = getResource("MigrationTests.java"),
				res2 = getResource("MigrationTests2.java");
		assertNotNull("the resource MigrationTests.java should not be null", res);
		assertNotNull("the resource MigrationTests2.java should not be null", res2);
		IResource[] oldmapped = copy.getMappedResources();
		try {
			copy.setMappedResources(new IResource[] {res, res2});
			copy.doSave();
			IResource[] mapped = copy.getMappedResources();
			assertEquals("there should be two resources mapped", 2, mapped.length);
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
