package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public class ObjectCollectedException extends RuntimeException 
{
	public ObjectCollectedException() { }
	public ObjectCollectedException(String arg1) {
	   	super(arg1);
	}
}
