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
	@Override
	public void localSetUp() {
		// Get the exception request
		fRequest = getExceptionRequest();
	}
	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		// Delete the exception request we created in this test
		fVM.eventRequestManager().deleteEventRequest(fRequest);
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new ExceptionRequestTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
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
