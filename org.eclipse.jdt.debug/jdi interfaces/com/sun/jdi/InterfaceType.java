package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;

public interface InterfaceType extends ReferenceType {
	public List implementors();
	public List subinterfaces();
	public List superinterfaces();
}
