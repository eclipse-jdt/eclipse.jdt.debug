package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;

public interface ClassLoaderReference extends ObjectReference {
	public List definedClasses();
	public List visibleClasses();
}
