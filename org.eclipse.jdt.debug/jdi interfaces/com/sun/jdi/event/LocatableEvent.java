package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;

public abstract interface LocatableEvent extends com.sun.jdi.event.Event, com.sun.jdi.Locatable {
	public abstract ThreadReference thread();
}