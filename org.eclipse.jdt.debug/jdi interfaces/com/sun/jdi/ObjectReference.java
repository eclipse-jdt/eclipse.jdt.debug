package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface ObjectReference extends com.sun.jdi.Value {
	public static final int INVOKE_SINGLE_THREADED = 1;
	public static final int INVOKE_NONVIRTUAL = 2;
	public void disableCollection();
	public void enableCollection();
	public int entryCount() throws IncompatibleThreadStateException;
	public boolean equals(Object arg1);
	public com.sun.jdi.Value getValue(com.sun.jdi.Field arg1);
	public java.util.Map getValues(java.util.List arg1);
	public int hashCode();
	public com.sun.jdi.Value invokeMethod(com.sun.jdi.ThreadReference arg1, com.sun.jdi.Method arg2, java.util.List arg3, int arg4) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException;
	public boolean isCollected();
	public com.sun.jdi.ThreadReference owningThread() throws IncompatibleThreadStateException;	public com.sun.jdi.ReferenceType referenceType();
	public void setValue(com.sun.jdi.Field arg1, com.sun.jdi.Value arg2) throws InvalidTypeException, ClassNotLoadedException;
	public long uniqueID();
	public java.util.List waitingThreads() throws IncompatibleThreadStateException;
}
