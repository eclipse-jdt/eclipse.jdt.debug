package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.Mirror;

public interface EventQueue extends Mirror {
	public EventSet remove() throws InterruptedException;
	public EventSet remove(long arg1) throws InterruptedException;
}
