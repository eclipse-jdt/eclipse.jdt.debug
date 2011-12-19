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
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/connect/spi/TransportService.html
 */
public abstract class TransportService {
	/**
	 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/connect/spi/TransportService.Capabilities.html
	 */
	public abstract static class Capabilities {
		public abstract boolean supportsAcceptTimeout();

		public abstract boolean supportsAttachTimeout();

		public abstract boolean supportsHandshakeTimeout();

		public abstract boolean supportsMultipleConnections();
	}
	/**
	 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/connect/spi/TransportService.ListenKey.html
	 */
	public abstract static class ListenKey {
		public abstract String address();
	}

	public abstract Connection accept(ListenKey listenKey, long attachTimeout,
			long handshakeTimeout) throws IOException;

	public abstract Connection attach(String address, long attachTimeout,
			long handshakeTimeout) throws IOException;

	public abstract TransportService.Capabilities capabilities();

	public abstract String description();

	public abstract String name();

	public abstract TransportService.ListenKey startListening()
			throws IOException;

	public abstract TransportService.ListenKey startListening(String arg1)
			throws IOException;

	public abstract void stopListening(TransportService.ListenKey arg1)
			throws IOException;
}
