package com.sun.jdi.event;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface ClassUnloadEvent extends Event {
	public String className();
	public String classSignature();
}
