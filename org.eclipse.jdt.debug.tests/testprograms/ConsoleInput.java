/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Reads and echos input from standard in
 */
public class ConsoleInput {
	
	public static void main(String[] args) {
		InputStreamReader reader = new InputStreamReader(System.in);
		boolean done = false;
		while (!done) {
			char[] buffer = new char[100];
			try {
				int length = reader.read(buffer);
				if (length > 0) {
					String input = new String(buffer, 0, length);
					System.out.print(input);
					if (input.startsWith("exit")) {
						done = true;
					}
				} else {
					done = true;
					System.out.println("EOF");
				}
			} catch (IOException e) {
			}
		}
	}

}
