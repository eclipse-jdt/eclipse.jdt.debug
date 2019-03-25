/*******************************************************************************
 * Copyright (c) 2019 Paul Pazderski and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Paul Pazderski - initial API and implementation
 *******************************************************************************/

import java.nio.charset.Charset;

/**
 * This snippet prints a configurable number of one byte characters followed by a configurable number of two byte characters.
 */
public class ConsoleOutputUmlaut {
	public static void main(String[] args) {
		if (!"utf-8".equalsIgnoreCase(Charset.defaultCharset().name()) && !"utf8".equalsIgnoreCase(Charset.defaultCharset().name())) {
			System.err.println("The programm's output must be UTF-8 encoded.");
			System.exit(2);
		}

		int numAscii = 1;
		int numUmlaut = 4200;
		int repetitions = 1;

		if (args.length > 0) {
			try {
				numAscii = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
			}
		}
		if (args.length > 1) {
			try {
				numUmlaut = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
			}
		}
		if (args.length > 2) {
			try {
				repetitions = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
			}
		}

		StringBuilder sb = new StringBuilder(numAscii + numUmlaut + 2);
		for (int i = 0; i < numAscii; i++) {
			sb.append('0');
		}
		for (int i = 0; i < numUmlaut; i++) {
			sb.append('\u00FC'); // ü
		}
		sb.append("\r\n");

		String testString = sb.toString();
		for (int i = 0; i < repetitions; i++) {
			System.out.print(testString);
		}
	}
}
