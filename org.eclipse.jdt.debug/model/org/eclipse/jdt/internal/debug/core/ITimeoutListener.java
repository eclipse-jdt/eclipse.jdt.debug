package org.eclipse.jdt.internal.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
 
/**
 * A timeout listener is notified when a timer expires.
 */
public interface ITimeoutListener {

	/**
	 * Notifies this listener that its timeout request has expired.
	 */
	public void timeout();
}