package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.Mirror;
import com.sun.jdi.request.EventRequest;

public interface Event extends Mirror {
	public EventRequest request();
}
