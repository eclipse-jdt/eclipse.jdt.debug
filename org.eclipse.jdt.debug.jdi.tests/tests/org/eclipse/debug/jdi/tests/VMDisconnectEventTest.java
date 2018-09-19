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

import com.sun.jdi.event.VMDisconnectEvent;

/**
 * Tests for JDI com.sun.jdi.event.VMDisconnectEvent.
 */
public class VMDisconnectEventTest extends AbstractJDITest {

	private VMDisconnectEvent fVMDisconnectEvent;
	/**
	 * Creates a new test.
	 */
	public VMDisconnectEventTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Prepare to receive the event
		VMDisconnectEventWaiter waiter =
			new VMDisconnectEventWaiter(null, true);
		fEventReader.addEventListener(waiter);

		// Trigger a vm death event by shutting down the VM
		killVM();

		// Wait for the event to come in
		fVMDisconnectEvent = (VMDisconnectEvent) waitForEvent(waiter, 10000);
		// Wait 10s max
		fEventReader.removeEventListener(waiter);
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
	public static void main(java.lang.String[] args) {
		new VMDisconnectEventTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.event.VMDeathEvent";
	}
	/**
	 * Test that we received the event.
	 */
	public void testJDIVMDeath() {
		assertTrue("1", fVMDisconnectEvent != null);
	}
}
