package org.eclipse.jdt.internal.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * A timeout listener is notified when a timer expires.
 * @see Timer
 */
public interface ITimeoutListener {

	/**
	 * Notifies this listener that its timeout request has expired.
	 */
	public void timeout();
}