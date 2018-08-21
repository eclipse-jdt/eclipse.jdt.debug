/*******************************************************************************
 *  Copyright (c) 2005, 2011 IBM Corporation and others.
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

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Class to test the migration delegate for java type launch configuration migrations
 * Java types in this context only include Local Java Applications, Java Applets, and Remote Java Applications
 *
 * @since 3.2
 *
 */
public class MigrationDelegateTests extends AbstractDebugTest {

	/**
	 * constructor
	 * @param name the name of the test
	 */
	public MigrationDelegateTests(String name) {
		super(name);
	}

	/**
	 * Runs a normal migration with no problems
	 * @throws Exception
	 */
	public void testStandardMigration() throws Exception {
		createLaunchConfiguration("MigrationTests"); //$NON-NLS-1$
		ILaunchConfiguration config = getLaunchConfiguration("MigrationTests"); //$NON-NLS-1$
		try{
			assertTrue("LC: "+config.getName()+" should be a candidate for migration", config.isMigrationCandidate()); //$NON-NLS-1$ //$NON-NLS-2$
			config.migrate();
			IResource[] mappedResources = config.getMappedResources();
			assertEquals("Wrong number of mapped resources", 1, mappedResources.length); //$NON-NLS-1$
			assertEquals("Wrong mapped resources", get14Project().findType("MigrationTests").getUnderlyingResource(), mappedResources[0]); //$NON-NLS-1$ //$NON-NLS-2$
		}
		finally {
			config = null;
		}
	}

	/**
	 * Tests to see if the previously migrated launch configurations are still considered candidates
	 * @throws Exception
	 */
	public void testMigrationAlreadyPerformed() throws Exception {
		createLaunchConfiguration("MigrationTests2"); //$NON-NLS-1$
		ILaunchConfiguration config = getLaunchConfiguration("MigrationTests2"); //$NON-NLS-1$
		try{
			assertTrue("LC: "+config.getName()+" should be a candidate for migration", config.isMigrationCandidate()); //$NON-NLS-1$ //$NON-NLS-2$
			config.migrate();
			assertTrue("LC: "+config.getName()+" should not be a candidate for migration", !config.isMigrationCandidate()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		finally {
			config = null;
		}
	}

}
