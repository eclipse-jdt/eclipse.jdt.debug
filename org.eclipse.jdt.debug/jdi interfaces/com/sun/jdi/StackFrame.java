package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface StackFrame extends com.sun.jdi.Mirror , com.sun.jdi.Locatable {
	public com.sun.jdi.Value getValue(com.sun.jdi.LocalVariable arg1);
	public java.util.Map getValues(java.util.List arg1);
	public com.sun.jdi.Location location();
	public void setValue(com.sun.jdi.LocalVariable arg1, com.sun.jdi.Value arg2) throws InvalidTypeException, ClassNotLoadedException;
	public com.sun.jdi.ObjectReference thisObject();
	public com.sun.jdi.ThreadReference thread();
	public com.sun.jdi.LocalVariable visibleVariableByName(String arg1) throws AbsentInformationException;
	public java.util.List visibleVariables() throws AbsentInformationException;
}
