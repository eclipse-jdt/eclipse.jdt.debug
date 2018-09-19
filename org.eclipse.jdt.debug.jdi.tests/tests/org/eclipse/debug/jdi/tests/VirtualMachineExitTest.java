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
 * Tests for JDI com.sun.jdi.VirtualMachine.exit().
 */
public class VirtualMachineExitTest extends AbstractJDITest {
	/**
	 * Creates a new test .
	 */
	public VirtualMachineExitTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		// Finish the shut down
		shutDownTarget();

		// Start up again
		launchTargetAndStartProgram();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(String[] args) {
		new VirtualMachineExitTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.VirtualMachine.exit(int)";
	}
	/**
	 * Test JDI exit().
	 */
	public void testJDIExit() {
		try {
			fVM.exit(0);
		} catch (VMDisconnectedException e) {
			assertTrue("1", false);
		}

		try {
			Thread.sleep(200);
			assertTrue("2", !vmIsRunning());
			fVM.allThreads();
			assertTrue("3", false);
		} catch (VMDisconnectedException e) {
		} catch (InterruptedException e) {
		}
	}
}
