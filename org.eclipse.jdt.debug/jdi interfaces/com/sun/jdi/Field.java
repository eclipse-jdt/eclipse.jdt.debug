package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface Field extends com.sun.jdi.TypeComponent , java.lang.Comparable {
	public boolean equals(Object arg1);
	public int hashCode();
	public boolean isTransient();
	public boolean isVolatile();
	public com.sun.jdi.Type type() throws ClassNotLoadedException;
	public String typeName();
}
