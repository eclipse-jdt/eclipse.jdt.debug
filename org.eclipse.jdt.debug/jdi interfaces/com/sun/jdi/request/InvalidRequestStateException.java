package com.sun.jdi.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class InvalidRequestStateException extends RuntimeException {
	public InvalidRequestStateException() {
	}
	
	public InvalidRequestStateException(String arg1) {
		super(arg1);
	}
}