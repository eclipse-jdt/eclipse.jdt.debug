package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
public interface Field extends TypeComponent , Comparable {
	public boolean equals(Object arg1);
	public int hashCode();
	public boolean isTransient();
	public boolean isVolatile();
	public Type type() throws ClassNotLoadedException;
	public String typeName();
}
