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
package org.eclipse.jdt.launching;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

/**
 * Utility class to find a port to debug on.
 */
public class SocketUtil {
	private static final Random fgRandom= new Random(System.currentTimeMillis());
	
	/**
	 * Returns a free port number on the specified host within the given range,
	 * or if there are no free ports in the given range, returns any free port,
	 * or -1 if none found.
	 * 
	 * @param host name or IP addres of host on which to find a free port
	 * @param searchFrom the port number from which to start searching 
	 * @param searchTo the port number at which to stop searching
	 * @return a free port in the specified range, or any free port, or -1 of none found
	 */
	public static int findUnusedLocalPort(String host, int searchFrom, int searchTo) {
		for (int i= 0; i < 10; i++) {
			ServerSocket socket= null;
			try {
				int port= getRandomPort(searchFrom, searchTo);
				socket= new ServerSocket(port);
				return port;
			} catch (IOException e) { 
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
					}
				}
			}
		}
		ServerSocket socket= null;
		try {
			socket= new ServerSocket(0);
			return socket.getLocalPort();
		} catch (IOException e) { 
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		return -1;
	}
	
	private static int getRandomPort(int low, int high) {
		return (int)(fgRandom.nextFloat() * (high-low)) + low;
	}
}
