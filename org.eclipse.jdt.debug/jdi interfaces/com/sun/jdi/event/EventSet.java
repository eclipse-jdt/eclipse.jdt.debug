package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Collection;

import com.sun.jdi.Mirror;

public interface EventSet extends Mirror , Collection {
	public EventIterator eventIterator();
	public int suspendPolicy();
	public void resume();
}
