package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface TypeComponent extends com.sun.jdi.Mirror , com.sun.jdi.Accessible {
	public com.sun.jdi.ReferenceType declaringType();
	public boolean isFinal();
	public boolean isStatic();
	public boolean isSynthetic();
	public String name();
	public String signature();
}
