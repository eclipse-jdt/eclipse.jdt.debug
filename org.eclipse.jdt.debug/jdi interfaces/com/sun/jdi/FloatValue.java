package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface FloatValue extends com.sun.jdi.PrimitiveValue , java.lang.Comparable {
	public boolean equals(Object arg1);
	public int hashCode();
	public float value();
}
