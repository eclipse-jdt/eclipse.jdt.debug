package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.Locatable;
import com.sun.jdi.ThreadReference;

public abstract interface LocatableEvent extends Event, Locatable {
	public abstract ThreadReference thread();
}