package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class InvocationException extends Exception {
	private ObjectReference exception;
	public InvocationException(ObjectReference arg1) {
		exception = arg1;
	}
	public ObjectReference exception() {
		return exception; 
	}
}
