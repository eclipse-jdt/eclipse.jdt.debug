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

package org.eclipse.debug.jdi.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Tests continuous reading from a file and input stream
 */
public class FileConsoleReader extends AbstractReader {
	private InputStream fInput;
	private FileOutputStream fFileOutputStream;
	/**
	 * Creates a new console reader that will read from the given input stream.
	 * @param name
	 * @param fileName
	 * @param input
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
	@Override
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

	/**
	 * @see org.eclipse.debug.jdi.tests.AbstractReader#stop()
	 */
	@Override
	public void stop() {
		try {
			fFileOutputStream.close();
		} catch (IOException e) {
			System.out.println("Got exception: " + e.getMessage());
		}
	}
}
