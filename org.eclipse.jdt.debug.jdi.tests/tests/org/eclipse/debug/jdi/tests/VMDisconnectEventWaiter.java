/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.jdi.tests;

import com.sun.jdi.event.VMDisconnectEvent;
/**
 * Listen for VMDisconnectEvent.
 */
public class VMDisconnectEventWaiter extends EventWaiter {
	
	/**
	 * Constructor
	 * @param request
	 * @param shouldGo
	 */
	public VMDisconnectEventWaiter(com.sun.jdi.request.EventRequest request, boolean shouldGo) {
		super(request, shouldGo);
	}
	/**
	 * @see org.eclipse.debug.jdi.tests.EventWaiter#vmDisconnect(com.sun.jdi.event.VMDisconnectEvent)
	 */
	@Override
	public boolean vmDisconnect(VMDisconnectEvent event) {
		notifyEvent(event);
		return fShouldGo;
	}
}
