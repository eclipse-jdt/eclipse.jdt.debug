package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface Location extends Mirror , Comparable {
	public long codeIndex();
	public ReferenceType declaringType();
	public boolean equals(Object arg1);
	public int hashCode();
	public int lineNumber();
	public Method method();
   	public String sourceName() throws AbsentInformationException;
   	public int lineNumber(String stratum);
   	public String sourceName(String stratum) throws AbsentInformationException;
   	public String sourcePath(String stratum) throws AbsentInformationException;
   	public String sourcePath() throws AbsentInformationException;
}
