package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.Method;
import com.sun.jdi.ThreadReference;

public interface MethodExitEvent extends LocatableEvent {
	public Method method();
	public ThreadReference thread();
}
