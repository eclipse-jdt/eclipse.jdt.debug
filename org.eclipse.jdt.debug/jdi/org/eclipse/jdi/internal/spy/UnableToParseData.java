package org.eclipse.jdi.internal.spy;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
*********************************************************************/

/**
 * Exception throws when the spy have not enough information form correctly
 * parse the data.
 */
public class UnableToParseData extends Exception {

	private byte[] fRemainingData;

	public UnableToParseData(String message, byte[] remainingData) {
		super(message);
		fRemainingData= remainingData;
	}
	
	public byte[] getRemainingData() {
		return fRemainingData;
	}

}
