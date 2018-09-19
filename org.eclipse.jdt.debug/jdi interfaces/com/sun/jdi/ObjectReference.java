/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sun.jdi;

import java.util.List;
import java.util.Map;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/ObjectReference.html
 */
public interface ObjectReference extends com.sun.jdi.Value {
	public static final int INVOKE_SINGLE_THREADED = 1;
	public static final int INVOKE_NONVIRTUAL = 2;
	public void disableCollection();
	public void enableCollection();
	public int entryCount() throws IncompatibleThreadStateException;
	@Override
	public boolean equals(Object arg1);
	public Value getValue(Field arg1);
	public Map<Field, Value> getValues(List<? extends Field> arg1);
	@Override
	public int hashCode();
	public Value invokeMethod(ThreadReference arg1, Method arg2, List<? extends Value> arg3,	int arg4) throws InvalidTypeException, ClassNotLoadedException,	IncompatibleThreadStateException, InvocationException;
	public boolean isCollected();
	public ThreadReference owningThread() throws IncompatibleThreadStateException;
	public ReferenceType referenceType();
	public void setValue(Field arg1, Value arg2) throws InvalidTypeException, ClassNotLoadedException;
	public long uniqueID();
	public List<ThreadReference> waitingThreads() throws IncompatibleThreadStateException;
	public List<ObjectReference> referringObjects(long arg1);
}
