package com.sun.jdi.connect;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

public class IllegalConnectorArgumentsException extends Exception {
	List fNames;
	
	public IllegalConnectorArgumentsException(String arg1, List arg2) {
		super(arg1);
		fNames = arg2;
	}
	
	public IllegalConnectorArgumentsException(String arg1, String arg2) {
		super(arg1);
		fNames = new ArrayList(1);
		fNames.add(arg1);
	}
	
	public List argumentNames() { 
		return fNames;
	}
}
