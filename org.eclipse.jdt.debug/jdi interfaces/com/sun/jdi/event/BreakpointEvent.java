package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.ThreadReference;

public interface BreakpointEvent extends LocatableEvent {
	public ThreadReference thread();
}
