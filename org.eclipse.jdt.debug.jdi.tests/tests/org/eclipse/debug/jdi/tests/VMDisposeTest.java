package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

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
	public void localSetUp() {
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new VMDisposeTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 */
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