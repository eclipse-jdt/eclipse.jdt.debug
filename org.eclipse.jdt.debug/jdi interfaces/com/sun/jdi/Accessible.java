package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface Accessible {
	public boolean isPackagePrivate();
	public boolean isPrivate();
	public boolean isProtected();
	public boolean isPublic();
}
