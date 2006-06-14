/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.jdi.tests;

import org.eclipse.jdi.internal.VoidValueImpl;

import com.sun.jdi.Value;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.request.MethodExitRequest;

/**
 * Test cases for the implementation of providing argumebnt information even if 
 * no debugging information is present in the new java 1.6 VM
 * 
 * @since 3.3
 */
public class MethodReturnValuesTests extends AbstractJDITest {
	
	/** setup test info locally **/
	public void localSetUp() {}

	/**
	 * test to make sure 1.6 VM supports method return values 
	 */
	public void testCanGetMethodReturnValues() {
		if(fVM.version().indexOf("1.6") > -1) {
			assertTrue("Should have method return values capabilities", fVM.canGetMethodReturnValues());
		}
		else {
			assertTrue("Should not have method return values capabilities", !fVM.canGetMethodReturnValues());
		}
	}
	
	/**
	 * test to make sure that returnValue is working to spec.
	 * test is not applicable to non 1.6 VMs
	 */
	public void testGetMethodReturnValue() {
		if(!fVM.canGetMethodReturnValues()) {
			return;
		}
		MethodExitRequest request = fVM.eventRequestManager().createMethodExitWithReturnValueRequest();
		MethodExitEvent event = (MethodExitEvent) triggerAndWait(request, "BreakpointEvent", true);
		assertNotNull("event should not be null", event);
		assertEquals(request, event.request());
		Value val = event.returnValue();
		assertNotNull("value should not be null", val);
		assertTrue("return value must be VoidValueImpl", val instanceof VoidValueImpl);
		fVM.eventRequestManager().deleteEventRequest(request);
	}
}
