package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface ArrayReference extends com.sun.jdi.ObjectReference {
	public com.sun.jdi.Value getValue(int arg1);
	public java.util.List getValues();
	public java.util.List getValues(int arg1, int arg2);
	public int length();
	public void setValue(int arg1, com.sun.jdi.Value arg2) throws InvalidTypeException, ClassNotLoadedException;
	public void setValues(int arg1, java.util.List arg2, int arg3, int arg4) throws InvalidTypeException, ClassNotLoadedException;
	public void setValues(java.util.List arg1) throws InvalidTypeException, ClassNotLoadedException;
}
