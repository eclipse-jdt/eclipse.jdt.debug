package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

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
	public void localSetUp() {
	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	public void localTearDown() {
		// Finish the shut down
		shutDownTarget();

		// Start up again
		launchTargetAndStartProgram();
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(String[] args) {
		new VirtualMachineExitTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 */
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
			Thread.currentThread().sleep(200);
			assertTrue("2", !vmIsRunning());
			fVM.allThreads();
			assertTrue("3", false);
		} catch (VMDisconnectedException e) {
		} catch (InterruptedException e) {
		}
	}
}