package org.eclipse.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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