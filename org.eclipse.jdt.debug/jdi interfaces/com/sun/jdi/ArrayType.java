package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface ArrayType extends ReferenceType {
	public String componentSignature();
	public Type componentType() throws ClassNotLoadedException;
	public String componentTypeName();
	public ArrayReference newInstance(int arg1);
}
