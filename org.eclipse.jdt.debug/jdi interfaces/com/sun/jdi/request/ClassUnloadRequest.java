package com.sun.jdi.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;

public interface ClassUnloadRequest extends com.sun.jdi.request.EventRequest {
	public void addClassExclusionFilter(String arg1);
	public void addClassFilter(String arg1);
}
