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
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/StackFrame.html
 */
public interface StackFrame extends Mirror, Locatable {
	public Value getValue(LocalVariable arg1);
	public Map<LocalVariable, Value> getValues(List<? extends LocalVariable> arg1);
	@Override
	public Location location();
	public void setValue(LocalVariable arg1, Value arg2) throws InvalidTypeException, ClassNotLoadedException;
	public ObjectReference thisObject();
	public ThreadReference thread();
	public LocalVariable visibleVariableByName(String arg1) throws AbsentInformationException;
	public List<LocalVariable> visibleVariables() throws AbsentInformationException;
	public List<Value> getArgumentValues();
}
