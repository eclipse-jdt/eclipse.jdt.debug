package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface FloatValue extends PrimitiveValue, Comparable {
	public boolean equals(Object arg1);
	public int hashCode();
	public float value();
}
