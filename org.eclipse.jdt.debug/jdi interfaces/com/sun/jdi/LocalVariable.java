package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
public interface LocalVariable extends Mirror , Comparable {
	public boolean equals(Object arg1);
	public int hashCode();
	public boolean isArgument();
	public boolean isVisible(StackFrame arg1);
	public String name();
	public String signature();
	public com.sun.jdi.Type type() throws ClassNotLoadedException;
	public String typeName();
}
