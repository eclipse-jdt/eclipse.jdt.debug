package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import com.sun.jdi.event.VMDisconnectEvent;
/**
 * Listen for VMDisconnectEvent.
 */
public class VMDisconnectEventWaiter extends EventWaiter {
	/**
	 * Creates a VMDisconnectEventWaiter.
	 */
	public VMDisconnectEventWaiter(
		com.sun.jdi.request.EventRequest request,
		boolean shouldGo) {
		super(request, shouldGo);
	}
	public boolean vmDisconnect(VMDisconnectEvent event) {
		notifyEvent(event);
		return fShouldGo;
	}
}
