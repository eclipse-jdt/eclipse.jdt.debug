package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface Method extends com.sun.jdi.TypeComponent , com.sun.jdi.Locatable , java.lang.Comparable {
	public java.util.List allLineLocations() throws AbsentInformationException;
	public java.util.List arguments() throws AbsentInformationException;
	public java.util.List argumentTypeNames();
	public java.util.List argumentTypes() throws ClassNotLoadedException;
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
	public java.util.List locationsOfLine(int arg1) throws AbsentInformationException;
	public Type returnType() throws ClassNotLoadedException;
	public String returnTypeName();
	public java.util.List variables() throws AbsentInformationException;
	public java.util.List variablesByName(String arg1) throws AbsentInformationException;
	public java.util.List allLineLocations(String arg1, String arg2) throws AbsentInformationException;
	public java.util.List locationsOfLine(String arg1, String arg2, int arg3) throws AbsentInformationException;
}
