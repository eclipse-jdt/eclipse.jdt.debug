package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.request.EventRequest;
/**
 * Listen for ClassPrepareEvent for a specific class.
 */
public class ClassPrepareEventWaiter extends EventWaiter {
	protected String fClassName;
	/**
	 * Creates a new ClassPrepareEventWaiter that waits for the given class to be loaded.
	 */
	public ClassPrepareEventWaiter(EventRequest request, boolean shouldGo, String className) {
		super(request, shouldGo);
		fClassName = className;
	}
	public boolean classPrepare(ClassPrepareEvent event) {
		if (event.referenceType().name().equals(fClassName)) {
			notifyEvent(event);
			return fShouldGo;
		} else
			return true;
	}
}
