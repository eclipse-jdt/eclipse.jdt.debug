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

import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MonitorContendedEnterRequest;
import com.sun.jdi.request.MonitorContendedEnteredRequest;
import com.sun.jdi.request.MonitorWaitRequest;
import com.sun.jdi.request.MonitorWaitedRequest;

/**
 * Test cases for the implementation of providing argument information even if
 * no debugging information is present in the new java 1.6 VM
 *
 * @since 3.3
 */
public class ContendedMonitorTests extends AbstractJDITest {

	EventRequestManager erm = null;

	/** setup test info locally **/
	@Override
	public void localSetUp() {
		erm = fVM.eventRequestManager();
	}

	/**
	 * test to see if a the 1.6 VM can get monitor events info and that
	 * a non-1.6VM cannot.
	 */
	public void testCanRequestMonitorEvents() {
		if(is16OrGreater()) {
			assertTrue("Should have ability to request monitor events info", fVM.canRequestMonitorEvents());
		}
		else {
			assertTrue("Should not have ability to request monitor events info", !fVM.canRequestMonitorEvents());
		}
	}

	/**
	 * test getting monitor contended enter requests from the event request manager
	 * this test is not applicable to non 1.6 VMs
	 */
	public void testMonitorContendedEnterRequests() {
		if(!fVM.canRequestMonitorEvents()) {
			return;
		}
		MonitorContendedEnterRequest req = erm.createMonitorContendedEnterRequest();
		req.enable();
		List<?> list = erm.monitorContendedEnterRequests();
		assertNotNull("list should not be null", list);
		assertTrue("list should be of size 1", list.size() == 1);
		assertTrue("req should be enabled", ((MonitorContendedEnterRequest)list.get(0)).isEnabled());
	}

	/**
	 * test getting monitor contended entered requests from the event request manager
	 * this test is not applicable to non 1.6 VMs
	 */
	public void testMonitorContendedEnteredRequests() {
		if(!fVM.canRequestMonitorEvents()) {
			return;
		}
		MonitorContendedEnteredRequest req = erm.createMonitorContendedEnteredRequest();
		req.enable();
		List<?> list = erm.monitorContendedEnteredRequests();
		assertNotNull("list should not be null", list);
		assertTrue("list should be of size 1", list.size() == 1);
		assertTrue("req should be enabled", ((MonitorContendedEnteredRequest)list.get(0)).isEnabled());
	}

	/**
	 * test getting monitor wait requests from the event request manager
	 * this test is not applicable to non 1.6 VMs
	 */
	public void testMonitorWaitRequest() {
		if(!fVM.canRequestMonitorEvents()) {
			return;
		}
		MonitorWaitRequest req = erm.createMonitorWaitRequest();
		req.enable();
		List<?> list = erm.monitorWaitRequests();
		assertNotNull("list should not be null", list);
		assertTrue("list should be of size 1", list.size() == 1);
		assertTrue("req should be enabled", ((MonitorWaitRequest)list.get(0)).isEnabled());
	}

	/**
	 * test getting monitor waited requests from the event request manager
	 * this test is not applicable to non 1.6 VMs
	 */
	public void testMonitorWaitedRequest() {
		if(!fVM.canRequestMonitorEvents()) {
			return;
		}
		MonitorWaitedRequest req = erm.createMonitorWaitedRequest();
		req.enable();
		List<?> list = erm.monitorWaitedRequests();
		assertNotNull("list should not be null", list);
		assertTrue("list should be of size 1", list.size() == 1);
		assertTrue("req should be enabled", ((MonitorWaitedRequest)list.get(0)).isEnabled());
	}

}
