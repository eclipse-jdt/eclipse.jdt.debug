package org.eclipse.debug.jdi.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileConsoleReader extends AbstractReader {
	private InputStream fInput;
	private FileOutputStream fFileOutputStream;
	/*
	 * Creates a new console reader that will read from the given input stream.
	 */
	public FileConsoleReader(String name, String fileName, InputStream input) {
		super(name);
		fInput = input;
		try {
			fFileOutputStream = new FileOutputStream(new File(fileName));
		} catch (IOException e) {
			System.out.println("Got exception: " + e.getMessage());
		}
	}
	/**
	 * Continuously reads events that are coming from the event queue.
	 */
	protected void readerLoop() {
		BufferedReader input = new BufferedReader(new InputStreamReader(fInput));
		try {
			int read = 0;
			while (!fIsStopping && read != -1) {
				read = input.read();
				if (read != -1) {
					fFileOutputStream.write(read);
				}
				fFileOutputStream.flush();
			}
		} catch (IOException e) {
		}
	}

	public void stop() {
		try {
			fFileOutputStream.close();
		} catch (IOException e) {
			System.out.println("Got exception: " + e.getMessage());
		}
	}
}