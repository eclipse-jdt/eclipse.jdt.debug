package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public abstract interface PathSearchingVirtualMachine extends VirtualMachine {
	public abstract java.util.List classPath();
	public abstract java.util.List bootClassPath();
	public abstract String baseDirectory();
}