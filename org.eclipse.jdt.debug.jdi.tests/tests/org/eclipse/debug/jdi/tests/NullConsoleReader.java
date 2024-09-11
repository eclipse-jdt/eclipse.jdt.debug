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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * A null console reader that continuously reads from the VM input stream
 * so that the VM doesn't block when the program writes to the stout.
 */

public class NullConsoleReader extends AbstractReader {
	private final InputStream fInput;
	private volatile PrintStream out;

	/**
	 * Constructor
	 *
	 * @param out
	 */
	public NullConsoleReader(String name, InputStream input, PrintStream out) {
		super(name);
		fInput = input;
		this.out = out;
	}
	/**
	 * Continuously reads events that are coming from the event queue.
	 */
	@Override
	protected void readerLoop() {
		try {
			int size = 0;
			byte[] buffer = new byte[1024];
			while (!fIsStopping && (size = fInput.read(buffer)) != -1) {
				if (out instanceof PrintStream o) {
					o.write(buffer, 0, size);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shutUp() {
		this.out = null;
	}

}
