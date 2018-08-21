/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.debug.jdi.tests;

import com.sun.jdi.VMDisconnectedException;

/**
 * Tests for JDI com.sun.jdi.event.VMDisconnectEvent.
 */
public class VMDisposeTest extends AbstractJDITest {
	/**
	 * Creates a new test.
	 */
	public VMDisposeTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new VMDisposeTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.VirtualMachine.dispose";
	}
	/**
	 * Test that we received the event.
	 */
	public void testJDIVMDispose() {
		fVM.dispose();
		try {
			fVM.allThreads();
			assertTrue("1", false);
		} catch (VMDisconnectedException e) {
		}

		try {
			// Reconnect to running VM.
			connectToVM();
			fVM.allThreads();
		} catch (VMDisconnectedException e) {
			assertTrue("3", false);
		}
	}
}
