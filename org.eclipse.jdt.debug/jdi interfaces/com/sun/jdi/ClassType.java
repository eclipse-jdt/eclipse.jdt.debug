package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;

public interface ClassType extends ReferenceType {
	public static final int INVOKE_SINGLE_THREADED = 1;
	public List allInterfaces();
	public Method concreteMethodByName(String arg1, String arg2);
	public List interfaces();
	public Value invokeMethod(ThreadReference arg1, Method arg2, List arg3, int arg4) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException;
	public ObjectReference newInstance(ThreadReference arg1, Method arg2, List arg3, int arg4) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException;
	public void setValue(Field arg1, Value arg2) throws InvalidTypeException, ClassNotLoadedException;
	public List subclasses();
	public ClassType superclass();
}
