package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
public abstract interface ClassObjectReference extends ObjectReference {
	public abstract ReferenceType reflectedType();
}