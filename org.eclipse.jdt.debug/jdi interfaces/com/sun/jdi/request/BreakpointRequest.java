package com.sun.jdi.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.Locatable;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;

public interface BreakpointRequest extends EventRequest , Locatable {
	public void addThreadFilter(ThreadReference arg1);
	public Location location();
	public void addInstanceFilter(ObjectReference instance);
}
