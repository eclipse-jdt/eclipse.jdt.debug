/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sun.jdi.connect.spi;

import java.io.IOException;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/connect/spi/Connection.html
 */
public abstract class Connection {
	public abstract void close() throws IOException;

	public abstract boolean isOpen();

	public abstract byte[] readPacket() throws IOException;

	public abstract void writePacket(byte[] arg1) throws IOException;
}
