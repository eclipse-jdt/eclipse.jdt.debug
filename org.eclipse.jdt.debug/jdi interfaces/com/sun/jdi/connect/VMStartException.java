package com.sun.jdi.connect;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class VMStartException extends Exception {
	Process fProcess;
	
	public VMStartException(Process proc) {
		fProcess = proc;
	}
	
	public VMStartException(String str, Process proc) {
		super(str);
		fProcess = proc;
	}
	
	public Process process() { 
		return fProcess;
	}
}
