package org.eclipse.jdi;/*
 * JDI Interface Specification class. 
 *
 * (BB)
 * (C) Copyright IBM Corp. 2000
 */


public interface VirtualMachine {
	/**
	 * Sets request timeout in ms.
	 */
	public void setRequestTimeout(int timeout);
	
	/**
	 * @return Returns request timeout in ms.
	 */
	public int getRequestTimeout();
}