package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public class ClassNotLoadedException extends Exception 
{
	public ClassNotLoadedException(String className) {
		name = className;
	}
		
	public ClassNotLoadedException(String className, String msg) {
		super(msg);
		name = className;
	}
		
	public String className() { return name; }
	private String name;
}
