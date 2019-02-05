/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests;

import org.eclipse.jdt.debug.tests.core.RemoteJavaApplicationTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests that are to be run manually by the debug team, in addition to
 * automated tests.
 */
public class ManualSuite extends DebugSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 * @return the test suite
	 */
	public static Test suite() {
		return new ManualSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public ManualSuite() {
		addTest(new TestSuite(ProjectCreationDecorator.class));

		/**
		 * This test appears in the manual suite as wee need to be able to specify ports
		 * and security settings to make sure the client can connect
		 */
		addTest(new TestSuite(RemoteJavaApplicationTests.class));

	}
}

