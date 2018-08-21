/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
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
 *******************************************************************************/package org.eclipse.debug.jdi.tests;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;

 /**
  * Test cases for the implementation of providing argument information even if
  * no debugging information is present in the new java 1.6 VM
  *
  * @since 3.3
  */
public class ForceEarlyReturnTests extends AbstractJDITest {

	/** setup test info locally **/
	@Override
	public void localSetUp() {}

	/**
	 * test to see if forcing early return is supported or not
	 */
	public void testCanForceEarlyReturn() {
		if(is16OrGreater()) {
			assertTrue("Should have force early return capabilities", fVM.canForceEarlyReturn());
		}
		else {
			assertFalse("Should not have force early return capabilities", fVM.canForceEarlyReturn());
		}
	}

	/**
	 * test for the specifying the return type for a forced return to make sure the new return value works
	 */
	public void testForceEarlyReturnIntType() {
		if(!fVM.canForceEarlyReturn()) {
			return;
		}
		try {
			Method method = getMethod("foo", "()Ljava/lang/String;");
			BreakpointRequest br = getBreakpointRequest(method.location());
			br.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			br.enable();
			EventWaiter waiter = new EventWaiter(br, true);
			fEventReader.addEventListener(waiter);
			triggerEvent("forcereturn2");
			BreakpointEvent bpe = (BreakpointEvent) waiter.waitEvent(10000);
			ThreadReference tref = bpe.thread();
			fEventReader.removeEventListener(waiter);
			if(tref.isSuspended()) {
				if(tref.isAtBreakpoint()) {
					method = getMethod("printNumber", "(Ljava/io/OutputStream;I)I");
					br = getBreakpointRequest(method.locationsOfLine(200).get(0));
					br.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
					br.enable();
					waiter = new EventWaiter(br, true);
					fEventReader.addEventListener(waiter);
					tref.forceEarlyReturn(fVM.mirrorOf("bar"));
					tref.resume();
					bpe = (BreakpointEvent) waiter.waitEvent(10000);
					assertNotNull("Timed out waiting for a breakpoint event during force return", bpe);
					tref = bpe.thread();
					LocalVariable lv = tref.frame(0).visibleVariables().get(2);
					Value val = tref.frame(0).getValue(lv);
					System.out.println(val);
					assertTrue("value should be a StringReference", val instanceof StringReference);
					fEventReader.removeEventListener(waiter);
					//TODO make sure this works with the newest versions of the 1.6VM
					assertTrue("values should be 'foobar'", ((StringReference)val).value().equals("foobar"));
				}
			}
		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
		} catch (InvalidTypeException e) {
			e.printStackTrace();
		} catch (ClassNotLoadedException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (AbsentInformationException e) {
			e.printStackTrace();
		}
	}

}
