package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;

public interface ClassUnloadEvent extends com.sun.jdi.event.Event {
	public String className();
	public String classSignature();
}
