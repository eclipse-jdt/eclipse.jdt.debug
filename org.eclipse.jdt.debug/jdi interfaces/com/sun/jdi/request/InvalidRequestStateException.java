package com.sun.jdi.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;

public class InvalidRequestStateException extends RuntimeException {
	public InvalidRequestStateException() {
	}
	
	public InvalidRequestStateException(String arg1) {
		super(arg1);
	}
}