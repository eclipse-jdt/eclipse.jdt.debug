package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface ArrayType extends com.sun.jdi.ReferenceType {
	public String componentSignature();
	public com.sun.jdi.Type componentType() throws ClassNotLoadedException;
	public String componentTypeName();
	public com.sun.jdi.ArrayReference newInstance(int arg1);
}
