package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A null console reader that continuously reads from the VM input stream
 * so that the VM doesn't block when the program writes to the stout.
 */

public class NullConsoleReader extends AbstractReader {
	private InputStream fInput;
	/*
	 * Creates a new console reader that will read from the given input stream.
	 */
	public NullConsoleReader(String name, InputStream input) {
		super(name);
		fInput = input;
	}
	/**
	 * Continuously reads events that are coming from the event queue.
	 */
	protected void readerLoop() {
		java.io.BufferedReader input =
			new BufferedReader(new InputStreamReader(fInput));
		try {
			int read = 0;
			while (!fIsStopping && read != -1) {
				read = input.read();
			}
		} catch (IOException e) {
		}
	}
}