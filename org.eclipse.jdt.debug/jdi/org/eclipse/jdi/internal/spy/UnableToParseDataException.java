/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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

	private byte[] fRemainingData;

	public UnableToParseDataException(String message, byte[] remainingData) {
		super(message);
		fRemainingData= remainingData;
	}
	
	public byte[] getRemainingData() {
		return fRemainingData;
	}

}
