/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.jdi.tests;

/**
 * Specialized exception class for testing
 */
public class NotYetImplementedException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	/**
	 * NotYetImplemented constructor comment.
	 */
	public NotYetImplementedException() {
		super();
	}
	/**
	 * NotYetImplemented constructor comment.
	 * @param s java.lang.String
	 */
	public NotYetImplementedException(String s) {
		super(s);
	}
}
