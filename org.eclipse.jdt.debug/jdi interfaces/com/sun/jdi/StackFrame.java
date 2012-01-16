/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
import java.util.Map;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/StackFrame.html
 */
public interface StackFrame extends Mirror, Locatable {
	public Value getValue(LocalVariable arg1);
	public Map<LocalVariable, Value> getValues(List<? extends LocalVariable> arg1);
	public Location location();
	public void setValue(LocalVariable arg1, Value arg2) throws InvalidTypeException, ClassNotLoadedException;
	public ObjectReference thisObject();
	public ThreadReference thread();
	public LocalVariable visibleVariableByName(String arg1) throws AbsentInformationException;
	public List<LocalVariable> visibleVariables() throws AbsentInformationException;
	public List<Value> getArgumentValues();
}
