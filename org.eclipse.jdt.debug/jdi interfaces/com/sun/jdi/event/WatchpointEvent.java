package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;

public interface WatchpointEvent extends com.sun.jdi.event.LocatableEvent {
	public com.sun.jdi.Field field();
	public com.sun.jdi.ObjectReference object();
	public com.sun.jdi.ThreadReference thread();
	public com.sun.jdi.Value valueCurrent();
}
