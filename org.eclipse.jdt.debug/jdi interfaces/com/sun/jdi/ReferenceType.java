package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;
import java.util.Map;

public interface ReferenceType extends Type , Comparable , Accessible {
	public List allFields();
	public List allMethods();
	public List allLineLocations() throws AbsentInformationException;
	public ClassLoaderReference classLoader();
	public ClassObjectReference classObject();
	public boolean equals(Object arg1);
	public boolean failedToInitialize();
	public Field fieldByName(String arg1);
	public List fields();
	public Value getValue(Field arg1);
	public Map getValues(List arg1);
	public int hashCode();
	public boolean isAbstract();
	public boolean isFinal();
	public boolean isInitialized();
	public boolean isPrepared();
	public boolean isStatic();
	public boolean isVerified();
	public List locationsOfLine(int arg1) throws AbsentInformationException;
	public List methods();
	public List methodsByName(String arg1);
	public List methodsByName(String arg1, String arg2);
	public String name();
	public List nestedTypes();
	public String sourceName() throws AbsentInformationException;
	public List visibleFields();
	public List visibleMethods();
	public List sourceNames(String arg1) throws AbsentInformationException;
	public List sourcePaths(String arg1) throws AbsentInformationException;
	public String sourceDebugExtension() throws AbsentInformationException;
	public List allLineLocations(String arg1, String arg2) throws AbsentInformationException;
	public List locationsOfLine(String arg1, String arg2, int arg3) throws AbsentInformationException;
	public List availableStrata();
	public String defaultStratum();
}
