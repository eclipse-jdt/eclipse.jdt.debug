/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
