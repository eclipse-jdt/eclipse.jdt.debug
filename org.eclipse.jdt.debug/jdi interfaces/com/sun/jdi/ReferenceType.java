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
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/ReferenceType.html
 */
public interface ReferenceType extends Type, Comparable<ReferenceType>, Accessible {
	public List<Field> allFields();
	public List<Location> allLineLocations() throws AbsentInformationException;
	public List<Location> allLineLocations(String arg1, String arg2) throws AbsentInformationException;
	public List<Method> allMethods();
	public List<String> availableStrata();
	public ClassLoaderReference classLoader();
	public ClassObjectReference classObject();
	public String defaultStratum();
	public boolean equals(Object arg1);
	public boolean failedToInitialize();
	public Field fieldByName(String arg1);
	public List<Field> fields();
	public String genericSignature();
	public Value getValue(Field arg1);
	public Map<Field, Value> getValues(List<? extends Field> arg1);
	public int hashCode();
	public boolean isAbstract();
	public boolean isFinal();
	public boolean isInitialized();
	public boolean isPrepared();
	public boolean isStatic();
	public boolean isVerified();
	public List<Location> locationsOfLine(int arg1) throws AbsentInformationException;
	public List<Location> locationsOfLine(String arg1, String arg2, int arg3)	throws AbsentInformationException;
	public List<Method> methods();
	public List<Method> methodsByName(String arg1);
	public List<Method> methodsByName(String arg1, String arg2);
	public String name();
	public List<ReferenceType> nestedTypes();
	public String sourceDebugExtension() throws AbsentInformationException;
	public String sourceName() throws AbsentInformationException;
	public List<String> sourceNames(String arg1) throws AbsentInformationException;
	public List<String> sourcePaths(String arg1) throws AbsentInformationException;
	public List<Field> visibleFields();
	public List<Method> visibleMethods();
	public List<ObjectReference> instances(long arg1);
	public int majorVersion();
	public int minorVersion();
	public int constantPoolCount();
	public byte[] constantPool();
}
