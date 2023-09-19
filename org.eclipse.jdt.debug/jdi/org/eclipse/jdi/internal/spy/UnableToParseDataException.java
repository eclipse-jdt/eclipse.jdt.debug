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
package org.eclipse.jdi.internal.spy;

/**
 * Exception throws when the spy have not enough information form correctly
 * parse the data.
 */
public class UnableToParseDataException extends Exception {

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private final byte[] fRemainingData;

	public UnableToParseDataException(String message, byte[] remainingData) {
		super(message);
		fRemainingData = remainingData;
	}

	public byte[] getRemainingData() {
		return fRemainingData;
	}

}
