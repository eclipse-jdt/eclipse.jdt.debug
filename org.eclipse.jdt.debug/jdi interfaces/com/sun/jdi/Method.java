package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;

public interface Method extends TypeComponent , Locatable , Comparable {
	public List allLineLocations() throws AbsentInformationException;
	public List arguments() throws AbsentInformationException;
	public List argumentTypeNames();
	public List argumentTypes() throws ClassNotLoadedException;
	public byte[] bytecodes();
	public boolean equals(Object arg1);
	public int hashCode();
	public boolean isAbstract();
	public boolean isConstructor();
	public boolean isNative();
	public boolean isStaticInitializer();
	public boolean isSynchronized();
	public boolean isObsolete();
	public Location locationOfCodeIndex(long arg1);
	public List locationsOfLine(int arg1) throws AbsentInformationException;
	public Type returnType() throws ClassNotLoadedException;
	public String returnTypeName();
	public List variables() throws AbsentInformationException;
	public List variablesByName(String arg1) throws AbsentInformationException;
	public List allLineLocations(String arg1, String arg2) throws AbsentInformationException;
	public List locationsOfLine(String arg1, String arg2, int arg3) throws AbsentInformationException;
}
