package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;

public interface EventQueue extends com.sun.jdi.Mirror {
	public EventSet remove() throws InterruptedException;
	public EventSet remove(long arg1) throws InterruptedException;
}
