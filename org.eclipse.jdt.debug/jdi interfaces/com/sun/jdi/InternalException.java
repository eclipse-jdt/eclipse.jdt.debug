package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class InternalException extends RuntimeException {
	public InternalException() { }
	
	public InternalException(int errorCode) {
		error = errorCode;
	}
	
	public InternalException(java.lang.String s) {
		super(s);
	}
	
	public InternalException(java.lang.String s, int errorCode) {
		super(s);
		error = errorCode;
	}
	
	public int errorCode() { 
		return error; 
	}
	
	private int error;
}