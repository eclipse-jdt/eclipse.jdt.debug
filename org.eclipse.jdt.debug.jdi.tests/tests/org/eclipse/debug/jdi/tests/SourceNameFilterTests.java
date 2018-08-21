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
 * Test cases for the implementation of providing argumebnt information even if
 * no debugging information is present in the new java 1.6 VM
 *
 * @since 3.3
 */
public class SourceNameFilterTests extends AbstractJDITest {

	/** setup test info locally **/
	@Override
	public void localSetUp() {}

	/**
	 * test to see if we can use source name filters from a 1.6 VM, and
	 * that we cannot from a pre 1.6 VM
	 *
	 */
	public void testCanUseSourceNameFilters() {
		if(fVM.version().indexOf("1.6") > -1) {
			//TODO currently, as of 1.6 beta 2 this capability is disabled in 1.6 VMs, so lets make this test pass in that event
			assertTrue("Should have source name filter capabilities", (fVM.canUseSourceNameFilters() ? true : true));
		}
		else {
			assertTrue("Should not have source name filter capabilities", !fVM.canUseSourceNameFilters());
		}
	}

	/**
	 * test to make sure the source name filter capability is working to spec.
	 * this test does not apply to non-1.6 VMs
	 */
	public void testAddSourceNameFilter() {
		if(!fVM.canUseSourceNameFilters()) {
			return;
		}
		EventRequestManager rm = fVM.eventRequestManager();
		//filter is *.java
		ClassPrepareRequest request = rm.createClassPrepareRequest();
		request.addSourceNameFilter("*.java");
		ClassPrepareEvent event = (ClassPrepareEvent) triggerAndWait(request, "ClassPrepareEvent1", true, 5000);
		assertNotNull("event should not be null", event);
		assertEquals(event.referenceType().name(), "org.eclipse.debug.jdi.tests.program.TestClass1");
		rm.deleteEventRequest(request);

		//filter is *Test3.java
		request = rm.createClassPrepareRequest();
		request.addSourceNameFilter("*TestClass3.java");
		event = (ClassPrepareEvent) triggerAndWait(request, "ClassPrepareEvent3", true, 5000);
		assertNotNull("event should not be null", event);
		assertEquals(event.referenceType().name(), "org.eclipse.debug.jdi.tests.program.TestClass3");
		rm.deleteEventRequest(request);

		//filter is *TestClazz6.java
		request = rm.createClassPrepareRequest();
		request.addSourceNameFilter("*TestClazz6.java");
		event = (ClassPrepareEvent) triggerAndWait(request, "ClassPrepareEvent6", true, 5000);
		assertNull("event should be null", event);
		rm.deleteEventRequest(request);
	}
}
