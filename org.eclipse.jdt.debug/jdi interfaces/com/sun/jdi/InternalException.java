/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sun.jdi;
/**
 * http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/InternalException.html
 */
public class InternalException extends RuntimeException {

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	public InternalException() {
	}

	public InternalException(int errorCode) {
		error = errorCode;
	}

	public InternalException(java.lang.String s) {
		super(s);
	}

	public InternalException(java.lang.String s, int errorCode) {
		super(s);
		error = errorCode;
	}

	public int errorCode() {
		return error;
	}

	private int error;
}
