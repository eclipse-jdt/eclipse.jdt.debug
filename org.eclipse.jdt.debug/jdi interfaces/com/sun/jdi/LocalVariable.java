package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface LocalVariable extends com.sun.jdi.Mirror , java.lang.Comparable {
	public boolean equals(Object arg1);
	public int hashCode();
	public boolean isArgument();
	public boolean isVisible(com.sun.jdi.StackFrame arg1);
	public String name();
	public String signature();
	public com.sun.jdi.Type type() throws ClassNotLoadedException;
	public String typeName();
}
