/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sun.jdi;


import java.util.List;

public interface ClassType extends ReferenceType {
	public static final int INVOKE_SINGLE_THREADED = 1;
	public List allInterfaces();
	public Method concreteMethodByName(String arg1, String arg2);
	public List interfaces();
	public Value invokeMethod(ThreadReference arg1, Method arg2, List arg3, int arg4) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException;
	public boolean isEnum();
	public ObjectReference newInstance(ThreadReference arg1, Method arg2, List arg3, int arg4) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException;
	public void setValue(Field arg1, Value arg2) throws InvalidTypeException, ClassNotLoadedException;
	public List subclasses();
	public ClassType superclass();
}
