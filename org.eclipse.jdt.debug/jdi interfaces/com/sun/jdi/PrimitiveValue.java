package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface PrimitiveValue extends com.sun.jdi.Value {
	public boolean booleanValue();
	public byte byteValue();
	public char charValue();
	public double doubleValue();
	public float floatValue();
	public int intValue();
	public long longValue();
	public short shortValue();
}
