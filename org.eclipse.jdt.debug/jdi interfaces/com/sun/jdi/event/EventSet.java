package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;

public interface EventSet extends com.sun.jdi.Mirror , java.util.Collection {
	public com.sun.jdi.event.EventIterator eventIterator();
	public int suspendPolicy();
	public void resume();
}
