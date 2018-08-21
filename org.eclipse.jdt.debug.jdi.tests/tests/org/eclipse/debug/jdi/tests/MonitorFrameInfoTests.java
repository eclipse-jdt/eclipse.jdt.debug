/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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

import java.util.List;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Method;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;

/**
 * Test cases for the implementation of providing argument information even if
 * no debugging information is present in the new java 1.6 VM
 *
 * @since 3.3
 */
public class MonitorFrameInfoTests extends AbstractJDITest {

	/** setup test info locally **/
	@Override
	public void localSetUp() {}

	/**
	 * test to see if a the 1.6 VM can get monitor frame info and that
	 * a non-1.6VM cannot.
	 */
	public void testCanGetMonitorFrameInfo() {
		if(is16OrGreater()) {
			assertTrue("Should have monitor frame info", fVM.canGetMonitorFrameInfo());
		}
		else {
			assertTrue("Should not have monitor frame info", !fVM.canGetMonitorFrameInfo());
		}
	}

	/**
	 * test to make sure the proper frames and monitors are collected for the corresponding thread ref.
	 * this test has no effect in a non-1.6VM
	 */
	public void testOwnedMonitorsAndFrames() {
		if(!fVM.canGetMonitorFrameInfo()) {
			return;
		}
		try {
			Method method = getMethod("sync", "()V");
			BreakpointRequest br = fVM.eventRequestManager().createBreakpointRequest(method.location());
			br.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			br.enable();
			EventWaiter waiter = new EventWaiter(br, true);
			fEventReader.addEventListener(waiter);
			triggerEvent("monitorinfo");
			BreakpointEvent bpe = (BreakpointEvent) waiter.waitEvent(10000);
			ThreadReference tref = bpe.thread();
			List<?> list = tref.ownedMonitorsAndFrames();
			assertNotNull("list cannot be null", list);
			assertTrue("there should be one monitor", list.size() == 1);
			fEventReader.removeEventListener(waiter);
			tref.resume();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
		}
	}

}
