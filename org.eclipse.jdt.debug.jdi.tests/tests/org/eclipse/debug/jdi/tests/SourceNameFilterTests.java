/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public void localSetUp() {}
	
	/**
	 * test to see if we can use source name filters from a 1.6 VM, and 
	 * that we cannot from a pre 1.6 VM
	 * TODO Retest this once a final version of a 1.6VM is released, as of the beta 2 release source name filters do not work 
	 */
	public void testCanUseSourceNameFilters() {
		if(fVM.version().indexOf("1.6") > -1) {
			assertTrue("Should have source name filter capabilities", fVM.canUseSourceNameFilters());
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
		ClassPrepareRequest request = rm.createClassPrepareRequest();
		//filter is *.java
		request.addSourceNameFilter("*.java");
		ClassPrepareEvent event = (ClassPrepareEvent) triggerAndWait(request, "ClassPrepareEvent1", true, 5000);
		assertNotNull("event should not be null", event);
		assertEquals(event.referenceType().name(), "org.elcipse.debug.jdi.tests.program.TestClass1");
		rm.deleteEventRequest(request);
		
		//filter is Test*3.java
		request = rm.createClassPrepareRequest();
		request.addSourceNameFilter("Test*3.java");
		event = (ClassPrepareEvent) triggerAndWait(request, "ClassPrepareEvent3", true, 5000);
		assertNotNull("event should not be null", event);
		assertEquals(event.referenceType().name(), "org.elcipse.debug.jdi.tests.program.TestClass3");
		rm.deleteEventRequest(request);
		
		//filter is T*Clazz*.java
		request = rm.createClassPrepareRequest();
		request.addSourceNameFilter("T*Clazz*.java");
		event = (ClassPrepareEvent) triggerAndWait(request, "ClassPrepareEvent8", true, 5000);
		assertNotNull("event should not be null", event);
		assertEquals(event.referenceType().name(), "org.elcipse.debug.jdi.tests.program.TestClass3");
		rm.deleteEventRequest(request);
	}
}
