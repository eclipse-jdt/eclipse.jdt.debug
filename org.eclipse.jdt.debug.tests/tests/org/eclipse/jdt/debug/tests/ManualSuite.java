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

import java.util.Enumeration;

import org.eclipse.jdt.debug.tests.core.RemoteJavaApplicationTests;
import org.eclipse.swt.widgets.Display;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Tests that are to be run manually by the debug team, in addition to
 * automated tests.
 */
public class ManualSuite extends TestSuite {

	/**
	 * Flag that indicates test are in progress
	 */
	protected boolean fTesting = true;

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

	/**
	 * Runs the tests and collects their result in a TestResult.
	 * The debug tests cannot be run in the UI thread or the event
	 * waiter blocks the UI when a resource changes.
	 * @see junit.framework.TestSuite#run(junit.framework.TestResult)
	 */
	@Override
	public void run(final TestResult result) {
		final Display display = Display.getCurrent();
		Thread thread = null;
		try {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					for (Enumeration<Test> e= tests(); e.hasMoreElements(); ) {
				  		if (result.shouldStop() ) {
							break;
						}
						Test test= e.nextElement();
						runTest(test, result);
					}
					fTesting = false;
					display.wake();
				}
			};
			thread = new Thread(r);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (fTesting) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

}

