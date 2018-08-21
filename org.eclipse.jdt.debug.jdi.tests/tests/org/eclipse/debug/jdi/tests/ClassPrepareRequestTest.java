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

import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;

/**
 * Tests for JDI com.sun.jdi.request.ClassPrepareRequest.
 */
public class ClassPrepareRequestTest extends AbstractJDITest {
	/**
	 * Creates a new test.
	 */
	public ClassPrepareRequestTest() {
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
	public static void main(String[] args) {
		new ClassPrepareRequestTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.request.ClassPrepareRequest";
	}
	/**
	 * Test JDI addClassFilter(String).
	 */
	public void testJDIAddClassFilterString() {
		EventRequestManager requestManager = fVM.eventRequestManager();

		// Filter is "org.eclipse.debug.jdi.tests.program.*" and loads org.eclipse.debug.jdi.tests.program.TestClass1
		ClassPrepareRequest request =
			requestManager.createClassPrepareRequest();
		request.addClassFilter("org.eclipse.debug.jdi.tests.program.*");
		ClassPrepareEvent event =
			(ClassPrepareEvent) triggerAndWait(request,
				"ClassPrepareEvent1",
				true,
				5000);
		assertTrue("1", event != null);
		assertEquals(
			"2",
			"org.eclipse.debug.jdi.tests.program.TestClass1",
			event.referenceType().name());
		requestManager.deleteEventRequest(request);

		// Filter is "bogus.*" and loads org.eclipse.debug.jdi.tests.program.TestClass2
		request = requestManager.createClassPrepareRequest();
		request.addClassFilter("bogus.*");
		event =
			(ClassPrepareEvent) triggerAndWait(request,
				"ClassPrepareEvent2",
				true,
				5000);
		assertTrue("3", event == null);
		requestManager.deleteEventRequest(request);

		// Filter is "*.TestClass3" and loads org.eclipse.debug.jdi.tests.program.TestClass3
		request = requestManager.createClassPrepareRequest();
		request.addClassFilter("*.TestClass3");
		event =
			(ClassPrepareEvent) triggerAndWait(request,
				"ClassPrepareEvent3",
				true,
				5000);
		assertTrue("4", event != null);
		assertEquals(
			"5",
			"org.eclipse.debug.jdi.tests.program.TestClass3",
			event.referenceType().name());
		requestManager.deleteEventRequest(request);

		// Filter is "*.eclipse.*.jdi.tests.*4" and loads org.eclipse.debug.jdi.tests.program.TestClass4
		request = requestManager.createClassPrepareRequest();
		request.addClassFilter("*.eclipse.*.jdi.tests.program.*4");
		event =
			(ClassPrepareEvent) triggerAndWait(request,
				"ClassPrepareEvent4",
				true,
				5000);
		assertTrue("6", event == null);
		requestManager.deleteEventRequest(request);

		// Filter is "*.eclipse.debug.jdi.tests.program.*" and loads org.eclipse.debug.jdi.tests.program.TestClass5
		request = requestManager.createClassPrepareRequest();
		request.addClassFilter("*.eclipse.debug.jdi.tests.program.*");
		event =
			(ClassPrepareEvent) triggerAndWait(request,
				"ClassPrepareEvent5",
				true,
				5000);
		assertTrue("7", event == null);
		requestManager.deleteEventRequest(request);

		// Filter is "org.eclipse.debug.jdi.tests.program.TestClass6" and loads org.eclipse.debug.jdi.tests.program.TestClass6
		request = requestManager.createClassPrepareRequest();
		request.addClassFilter(
			"org.eclipse.debug.jdi.tests.program.TestClass6");
		event =
			(ClassPrepareEvent) triggerAndWait(request,
				"ClassPrepareEvent6",
				true,
				5000);
		assertTrue("8", event != null);
		assertEquals(
			"9",
			"org.eclipse.debug.jdi.tests.program.TestClass6",
			event.referenceType().name());
		requestManager.deleteEventRequest(request);
	}
	/**
	 * Test JDI addClassExclusionFilter(String).
	 */
	public void testJDIAddClassExclusionFilterString() {
		EventRequestManager requestManager = fVM.eventRequestManager();

		// Filter is "org.eclipse.debug.jdi.tests.program.TestClass*" and loads org.eclipse.debug.jdi.tests.program.TestClass7 and org.eclipse.debug.jdi.tests.program.TestClazz8.
		ClassPrepareRequest request =
			requestManager.createClassPrepareRequest();
		request.addClassExclusionFilter(
			"org.eclipse.debug.jdi.tests.program.TestClass*");
		ClassPrepareEvent event =
			(ClassPrepareEvent) triggerAndWait(request,
				"ClassPrepareEvent7",
				true,
				5000);
		assertTrue("1", event != null);
		assertEquals(
			"2",
			"org.eclipse.debug.jdi.tests.program.TestClazz8",
			event.referenceType().name());
		requestManager.deleteEventRequest(request);

		// Filter is "org.eclipse.debug.jdi.tests.program.TestClazz9" and loads org.eclipse.debug.jdi.tests.program.TestClazz9 and org.eclipse.debug.jdi.tests.program.TestClazz10.
		request = requestManager.createClassPrepareRequest();
		request.addClassExclusionFilter(
			"org.eclipse.debug.jdi.tests.program.TestClazz9");
		event =
			(ClassPrepareEvent) triggerAndWait(request,
				"ClassPrepareEvent8",
				true,
				5000);
		assertTrue("3", event != null);
		assertEquals(
			"4",
			"org.eclipse.debug.jdi.tests.program.TestClazz10",
			event.referenceType().name());
		requestManager.deleteEventRequest(request);

	}
}
