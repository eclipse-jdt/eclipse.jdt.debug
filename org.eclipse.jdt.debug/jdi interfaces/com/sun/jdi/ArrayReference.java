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

public interface ArrayReference extends ObjectReference {
	public Value getValue(int arg1);
	public List getValues();
	public List getValues(int arg1, int arg2);
	public int length();
	public void setValue(int arg1, Value arg2) throws InvalidTypeException, ClassNotLoadedException;
	public void setValues(int arg1, List arg2, int arg3, int arg4) throws InvalidTypeException, ClassNotLoadedException;
	public void setValues(List arg1) throws InvalidTypeException, ClassNotLoadedException;
}
