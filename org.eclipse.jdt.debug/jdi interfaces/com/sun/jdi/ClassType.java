package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface ClassType extends com.sun.jdi.ReferenceType {
	public static final int INVOKE_SINGLE_THREADED = 1;
	public java.util.List allInterfaces();
	public com.sun.jdi.Method concreteMethodByName(String arg1, String arg2);
	public java.util.List interfaces();
	public com.sun.jdi.Value invokeMethod(com.sun.jdi.ThreadReference arg1, com.sun.jdi.Method arg2, java.util.List arg3, int arg4) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException;
	public com.sun.jdi.ObjectReference newInstance(com.sun.jdi.ThreadReference arg1, com.sun.jdi.Method arg2, java.util.List arg3, int arg4) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException;
	public void setValue(com.sun.jdi.Field arg1, com.sun.jdi.Value arg2) throws InvalidTypeException, ClassNotLoadedException;
	public java.util.List subclasses();
	public com.sun.jdi.ClassType superclass();
}
