package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

public interface WatchpointEvent extends LocatableEvent {
	public Field field();
	public ObjectReference object();
	public ThreadReference thread();
	public Value valueCurrent();
}
