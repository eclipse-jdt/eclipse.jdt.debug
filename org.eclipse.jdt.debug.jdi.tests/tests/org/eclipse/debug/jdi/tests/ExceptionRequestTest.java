package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import com.sun.jdi.request.ExceptionRequest;

/**
 * Tests for JDI com.sun.jdi.request.ExceptionRequest.
 */
public class ExceptionRequestTest extends AbstractJDITest {

	private ExceptionRequest fRequest;
	/**
	 * Creates a new test .
	 */
	public ExceptionRequestTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	public void localSetUp() {
		// Get the exception request
		fRequest = getExceptionRequest();
	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	public void localTearDown() {
		// Delete the exception request we created in this test
		fVM.eventRequestManager().deleteEventRequest(fRequest);
	}
	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new ExceptionRequestTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 */
	public String getName() {
		return "com.sun.jdi.request.ExceptionRequest";
	}
	/**
	 * Test JDI exception().
	 */
	public void testJDIException() {
		assertTrue("1", fRequest.exception() == null);
	}
}
