package com.sun.jdi.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;

public interface WatchpointRequest extends com.sun.jdi.request.EventRequest {
	public void addClassExclusionFilter(String arg1);
	public void addClassFilter(com.sun.jdi.ReferenceType arg1);
	public void addClassFilter(String arg1);
	public void addThreadFilter(com.sun.jdi.ThreadReference arg1);
	public com.sun.jdi.Field field();
	public void addInstanceFilter(ObjectReference instance);
}
