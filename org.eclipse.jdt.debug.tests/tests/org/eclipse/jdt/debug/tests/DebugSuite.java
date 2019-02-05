/*******************************************************************************
 *  Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Paul Pazderski  - Bug 544133: signal end of test runner in any case
 *******************************************************************************/
package org.eclipse.jdt.debug.tests;

import java.util.Enumeration;

import org.eclipse.swt.widgets.Display;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Debug test suite framework that runs test in a non UI thread.
 */
public abstract class DebugSuite extends TestSuite {

	/**
	 * Flag that indicates test are in progress
	 */
	protected boolean fTesting = true;

	/**
	 * Construct the test suite.
	 */
	public DebugSuite() {
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
					try {
						for (Enumeration<Test> e= tests(); e.hasMoreElements(); ) {
							if (result.shouldStop()) {
								break;
							}
							Test test= e.nextElement();
							runTest(test, result);
						}
					} finally {
						fTesting = false;
						display.wake();
					}
				}
			};
			thread = new Thread(r, "Test Runner");
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
