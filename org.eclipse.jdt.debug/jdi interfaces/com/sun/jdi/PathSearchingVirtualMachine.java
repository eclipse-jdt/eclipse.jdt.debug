package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.List;

public abstract interface PathSearchingVirtualMachine extends VirtualMachine {
	public abstract List classPath();
	public abstract List bootClassPath();
	public abstract String baseDirectory();
}