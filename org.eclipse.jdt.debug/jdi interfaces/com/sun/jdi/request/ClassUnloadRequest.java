package com.sun.jdi.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface ClassUnloadRequest extends EventRequest {
	public void addClassExclusionFilter(String arg1);
	public void addClassFilter(String arg1);
}
