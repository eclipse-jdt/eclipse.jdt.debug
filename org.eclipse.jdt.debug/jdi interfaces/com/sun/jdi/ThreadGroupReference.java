package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;

public interface ThreadGroupReference extends ObjectReference {
	public String name();
	public ThreadGroupReference parent();
	public void resume();
	public void suspend();
	public List threadGroups();
	public List threads();
}
