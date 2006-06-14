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

import org.eclipse.jdi.internal.StringReferenceImpl;
import org.eclipse.jdi.internal.VoidValueImpl;

import com.sun.jdi.Method;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodExitRequest;

/**
 * Test cases for the implementation of providing argumebnt information even if 
 * no debugging information is present in the new java 1.6 VM
 * 
 * @since 3.3
 */
public class MethodReturnValuesTests extends AbstractJDITest {
	
	MethodExitRequest req = null;
	Value val = null;
	MethodExitEvent event = null;
	EventRequestManager erm = null;
	Method method = null;
	BreakpointRequest br = null;
	EventWaiter waiter = null;
	BreakpointEvent bpe = null;
	ThreadReference tref = null;
	
	
	/** setup test info locally **/
	public void localSetUp() {
		erm = fVM.eventRequestManager();
	}
	
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
	 * test getting a void return value.
	 * not applicable to non 1.6 VMs
	 */
	public void testGetVoidMethodReturnValue() {
		if(!fVM.canGetMethodReturnValues()) {
			return;
		}
		try {
			//test VoidValueImpl return type
			method = getMethod("print", "(Ljava/io/OutputStream;)V");
			br = getBreakpointRequest(method.location());
			br.setSuspendPolicy(BreakpointRequest.SUSPEND_EVENT_THREAD);
			br.enable();
			waiter = new EventWaiter(br, true);
			fEventReader.addEventListener(waiter);
			triggerEvent("forcereturn");
			bpe = (BreakpointEvent) waiter.waitEvent(5000);
			tref = bpe.thread();
			fEventReader.removeEventListener(waiter);
			if(tref.isSuspended()) {
				req = erm.createMethodExitWithReturnValueRequest();
				req.addClassFilter("org.eclipse.debug.jdi.tests.program.MainClass");
				req.addThreadFilter(tref);
				req.enable();
				waiter = new EventWaiter(req, true);
				fEventReader.addEventListener(waiter);
				tref.resume();
				event = (MethodExitEvent)waiter.waitEvent(5000);
				fEventReader.removeEventListener(waiter);
				assertNotNull("event should not be null", event);
				assertEquals(req, event.request());
				val = event.returnValue();
				assertNotNull("value should not be null", val);
				assertTrue("return value must be VoidValueImpl", val instanceof VoidValueImpl);
				erm.deleteEventRequest(req);
			}
		}
		catch (InterruptedException e) {
			assertTrue("thrown exception mean failure", false);
		}
	}
	
	/**
	 * test to make sure that returnValue is working to spec.
	 * test is not applicable to non 1.6 VMs
	 */
	public void testGetStringMethodReturnValue() {
		try {
			//test non void return types, in the case IntegerValueImpl
			method = getMethod("foo", "()Ljava/lang/String;");
			br = getBreakpointRequest(method.location());
			br.setSuspendPolicy(BreakpointRequest.SUSPEND_EVENT_THREAD);
			br.enable();
			waiter = new EventWaiter(br, true);
			fEventReader.addEventListener(waiter);
			triggerEvent("fooreturn");
			bpe = (BreakpointEvent) waiter.waitEvent(5000);
			fEventReader.removeEventListener(waiter);
			tref = bpe.thread();
			if(tref.isSuspended()) {
				req = erm.createMethodExitWithReturnValueRequest();
				req.addClassFilter("org.eclipse.debug.jdi.tests.program.MainClass");
				req.addThreadFilter(tref);
				req.enable();
				waiter = new EventWaiter(req, true);
				fEventReader.addEventListener(waiter);
				tref.resume();
				event = (MethodExitEvent)waiter.waitEvent(5000);
				fEventReader.removeEventListener(waiter);
				assertNotNull("event should not be null", event);
				assertEquals(req, event.request());
				val = event.returnValue();
				assertNotNull("value should not be null", val);
				assertTrue("return value must be StringReferenceImpl", val instanceof StringReferenceImpl);
				erm.deleteEventRequest(req);
			}
		} 
		catch (InterruptedException e) {
			assertTrue("thrown exception mean failure", false);
		}
	}
}
