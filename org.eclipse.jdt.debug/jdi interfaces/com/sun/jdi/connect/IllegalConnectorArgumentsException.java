package com.sun.jdi.connect;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public class IllegalConnectorArgumentsException extends Exception 
{
	java.util.List fNames;
	
	public IllegalConnectorArgumentsException(String arg1, java.util.List arg2) {
		super(arg1);
		fNames = arg2;
	}
	
	public IllegalConnectorArgumentsException(String arg1, String arg2) {
		super(arg1);
		fNames = new java.util.Vector();
		fNames.add(arg1);
	}
	
	public java.util.List argumentNames() { 
		return fNames;
	}
}
