package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface ReferenceType extends com.sun.jdi.Type , java.lang.Comparable , com.sun.jdi.Accessible {
	public java.util.List allFields();
	public java.util.List allMethods();
	public java.util.List allLineLocations() throws AbsentInformationException;
	public com.sun.jdi.ClassLoaderReference classLoader();
	public com.sun.jdi.ClassObjectReference classObject();
	public boolean equals(Object arg1);
	public boolean failedToInitialize();
	public com.sun.jdi.Field fieldByName(String arg1);
	public java.util.List fields();
	public com.sun.jdi.Value getValue(com.sun.jdi.Field arg1);
	public java.util.Map getValues(java.util.List arg1);
	public int hashCode();
	public boolean isAbstract();
	public boolean isFinal();
	public boolean isInitialized();
	public boolean isPrepared();
	public boolean isStatic();
	public boolean isVerified();
	public java.util.List locationsOfLine(int arg1) throws AbsentInformationException;
	public java.util.List methods();
	public java.util.List methodsByName(String arg1);
	public java.util.List methodsByName(String arg1, String arg2);
	public String name();
	public java.util.List nestedTypes();
	public String sourceName() throws AbsentInformationException;
	public java.util.List visibleFields();
	public java.util.List visibleMethods();
	public java.util.List sourceNames(String arg1) throws AbsentInformationException;
	public java.util.List sourcePaths(String arg1) throws AbsentInformationException;
	public String sourceDebugExtension() throws AbsentInformationException;
	public java.util.List allLineLocations(String arg1, String arg2) throws AbsentInformationException;
	public java.util.List locationsOfLine(String arg1, String arg2, int arg3) throws AbsentInformationException;
	public java.util.List availableStrata();
	public String defaultStratum();
}
