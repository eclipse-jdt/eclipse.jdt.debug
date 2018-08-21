/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sun.jdi;

/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/InvalidLineNumberException.html
 * @deprecated This exception is no longer thrown.
 */
@Deprecated
public class InvalidLineNumberException extends RuntimeException {

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated no longer thrown
	 */
	@Deprecated
	public InvalidLineNumberException() {
	}

	/**
	 * @param arg1
	 *            the message
	 * @deprecated no longer thrown
	 */
	@Deprecated
	public InvalidLineNumberException(String arg1) {
		super(arg1);
	}
}
