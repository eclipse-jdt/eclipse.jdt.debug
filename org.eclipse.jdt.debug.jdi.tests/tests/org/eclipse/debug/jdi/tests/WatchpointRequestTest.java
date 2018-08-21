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

import com.sun.jdi.request.WatchpointRequest;

/**
 * Tests for JDI com.sun.jdi.request.WatchpointRequest.
 */
public class WatchpointRequestTest extends AbstractJDITest {

	private WatchpointRequest fAccessWatchpointRequest,
		fModificationWatchpointRequest;
	/**
	 * Creates a new test .
	 */
	public WatchpointRequestTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get an acces watchpoint request
		fAccessWatchpointRequest = getAccessWatchpointRequest();

		// Get a modification watchpoint request
		fModificationWatchpointRequest = getModificationWatchpointRequest();
	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		// Delete the watchpoint requests we created in this test
		fVM.eventRequestManager().deleteEventRequest(fAccessWatchpointRequest);
		fVM.eventRequestManager().deleteEventRequest(
			fModificationWatchpointRequest);
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new WatchpointRequestTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.request.WatchpointRequest";
	}
	/**
	 * Test JDI field().
	 */
	public void testJDIField() {
		assertEquals("1", getField("fBool"), fAccessWatchpointRequest.field());
		assertEquals(
			"2",
			getField("fBool"),
			fModificationWatchpointRequest.field());
	}
}
