package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface ThreadGroupReference extends com.sun.jdi.ObjectReference {
	public String name();
	public com.sun.jdi.ThreadGroupReference parent();
	public void resume();
	public void suspend();
	public java.util.List threadGroups();
	public java.util.List threads();
}
