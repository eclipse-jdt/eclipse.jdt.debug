package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface BooleanValue extends PrimitiveValue {
	public boolean equals(Object arg1);
	public int hashCode();
	public boolean value();
}
