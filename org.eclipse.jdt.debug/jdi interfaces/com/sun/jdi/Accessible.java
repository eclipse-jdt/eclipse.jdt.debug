package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface Accessible {
	public boolean isPackagePrivate();
	public boolean isPrivate();
	public boolean isProtected();
	public boolean isPublic();
}
