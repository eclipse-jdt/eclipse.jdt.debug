package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;

public interface ArrayReference extends ObjectReference {
	public Value getValue(int arg1);
	public List getValues();
	public List getValues(int arg1, int arg2);
	public int length();
	public void setValue(int arg1, Value arg2) throws InvalidTypeException, ClassNotLoadedException;
	public void setValues(int arg1, List arg2, int arg3, int arg4) throws InvalidTypeException, ClassNotLoadedException;
	public void setValues(List arg1) throws InvalidTypeException, ClassNotLoadedException;
}
