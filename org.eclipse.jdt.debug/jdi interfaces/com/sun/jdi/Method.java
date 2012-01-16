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
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/Method.html
 */
public interface Method extends TypeComponent, Locatable, Comparable<Method> {
	public List<Location> allLineLocations() throws AbsentInformationException;
	public List<Location> allLineLocations(String arg1, String arg2) throws AbsentInformationException;
	public List<LocalVariable> arguments() throws AbsentInformationException;
	public List<String> argumentTypeNames();
	public List<Type> argumentTypes() throws ClassNotLoadedException;
	public byte[] bytecodes();
	public boolean equals(Object arg1);
	public int hashCode();
	public boolean isAbstract();
	public boolean isBridge();
	public boolean isConstructor();
	public boolean isNative();
	public boolean isObsolete();
	public boolean isStaticInitializer();
	public boolean isSynchronized();
	public boolean isVarArgs();
	public Location locationOfCodeIndex(long arg1);
	public List<Location> locationsOfLine(int arg1) throws AbsentInformationException;
	public List<Location> locationsOfLine(String arg1, String arg2, int arg3)	throws AbsentInformationException;
	public Type returnType() throws ClassNotLoadedException;
	public String returnTypeName();
	public List<LocalVariable> variables() throws AbsentInformationException;
	public List<LocalVariable> variablesByName(String arg1) throws AbsentInformationException;
}
