package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;

public interface ExceptionEvent extends LocatableEvent {
	public Location catchLocation();
	public ObjectReference exception();
	public ThreadReference thread();
}
