package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface ClassLoaderReference extends com.sun.jdi.ObjectReference {
	public java.util.List definedClasses();
	public java.util.List visibleClasses();
}
