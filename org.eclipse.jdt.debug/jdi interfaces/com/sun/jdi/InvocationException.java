package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public class InvocationException extends Exception 
{
	private com.sun.jdi.ObjectReference exception;
	public InvocationException(com.sun.jdi.ObjectReference arg1) {
		exception = arg1;
	}
	public ObjectReference exception() { return exception; }
}
