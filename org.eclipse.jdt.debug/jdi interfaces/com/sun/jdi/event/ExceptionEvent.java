package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;

public interface ExceptionEvent extends com.sun.jdi.event.LocatableEvent {
	public com.sun.jdi.Location catchLocation();
	public com.sun.jdi.ObjectReference exception();
	public com.sun.jdi.ThreadReference thread();
}
