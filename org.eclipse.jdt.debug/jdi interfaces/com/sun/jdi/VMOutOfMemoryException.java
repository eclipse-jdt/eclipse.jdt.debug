package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class VMOutOfMemoryException extends RuntimeException {
	public VMOutOfMemoryException() { }
	public VMOutOfMemoryException(String s) {
	   	super(s);
	}
}