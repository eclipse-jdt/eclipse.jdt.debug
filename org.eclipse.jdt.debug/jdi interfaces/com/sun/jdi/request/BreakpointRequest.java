package com.sun.jdi.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;

public interface BreakpointRequest extends com.sun.jdi.request.EventRequest , com.sun.jdi.Locatable {
	public void addThreadFilter(com.sun.jdi.ThreadReference arg1);
	public com.sun.jdi.Location location();
	public void addInstanceFilter(ObjectReference instance);
}
