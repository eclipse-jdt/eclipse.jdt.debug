package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;
import java.util.Map;

public interface StackFrame extends Mirror , Locatable {
	public Value getValue(LocalVariable arg1);
	public Map getValues(List arg1);
	public Location location();
	public void setValue(LocalVariable arg1, Value arg2) throws InvalidTypeException, ClassNotLoadedException;
	public ObjectReference thisObject();
	public ThreadReference thread();
	public LocalVariable visibleVariableByName(String arg1) throws AbsentInformationException;
	public List visibleVariables() throws AbsentInformationException;
}
