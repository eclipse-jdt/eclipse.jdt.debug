package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;

public interface ClassPrepareEvent extends Event {
	public ReferenceType referenceType();
	public ThreadReference thread();
}
