package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.Value;

public interface ModificationWatchpointEvent extends WatchpointEvent {
	public Value valueToBe();
}
