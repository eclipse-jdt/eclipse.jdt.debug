package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface Location extends com.sun.jdi.Mirror , java.lang.Comparable {
	public long codeIndex();
	public ReferenceType declaringType();
	public boolean equals(Object arg1);
	public int hashCode();
	public int lineNumber();
	public com.sun.jdi.Method method();
   	public String sourceName() throws AbsentInformationException;
   	public int lineNumber(String stratum) throws AbsentInformationException;
   	public String sourceName(String stratum) throws AbsentInformationException;
   	public String sourcePath(String stratum) throws AbsentInformationException;
   	public String sourcePath() throws AbsentInformationException;
}
