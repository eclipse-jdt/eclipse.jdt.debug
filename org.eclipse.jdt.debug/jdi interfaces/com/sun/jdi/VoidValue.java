package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface VoidValue extends com.sun.jdi.Value {
	public boolean equals(Object arg1);
	public int hashCode();
}
