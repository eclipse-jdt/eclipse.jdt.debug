package com.sun.jdi.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.ThreadReference;

public interface ThreadDeathRequest extends EventRequest {
	public void addThreadFilter(ThreadReference arg1);
}
