package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Random;

public class SocketUtil {
	private static final Random fgRandom= new Random(System.currentTimeMillis());
	
	public static int findUnusedLocalPort(String host, int searchFrom, int searchTo) {

		for (int i= 0; i < 10; i++) {
			Socket s= null;
			int port= getRandomPort(searchFrom, searchTo);
			try {
				s= new Socket(host, port);
			} catch (ConnectException e) {
				return port;
			} catch (IOException e) {
			} finally {
				if (s != null) {
					try {
						s.close();
					} catch (IOException ioe) {
					}
				}
			}
		}
		return -1;
	}
	
	private static int getRandomPort(int low, int high) {
		return (int)(fgRandom.nextFloat() * (high-low)) + low;
	}
}