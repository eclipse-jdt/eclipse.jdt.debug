package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
public interface TypeComponent extends Mirror , Accessible {
	public ReferenceType declaringType();
	public boolean isFinal();
	public boolean isStatic();
	public boolean isSynthetic();
	public String name();
	public String signature();
}
