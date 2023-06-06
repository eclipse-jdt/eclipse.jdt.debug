/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
package org.eclipse.jdi.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class VerboseWriter {
	/** Length of verbose description. */
	public static final int VERBOSE_DESCRIPTION_LENGTH = 21;
	/** Number of hexadecimal verbose bytes per line. */
	public static final int VERBOSE_HEX_BYTES_PER_LINE = 16;
	/** Width of hex dump. */
	public static final int VERBOSE_HEX_WIDTH = 16 * 3 + 2;

	/**
	 * Number extra verbose lines. These are caused by hex dumps that span more
	 * than one line.
	 */
	volatile int fExtraVerboseLines = 0;

	/** PrintWriter that is written to. */
	private final PrintWriter fOutput;
	/** Buffer for output: one StringBuilder entry per line. */
	private final List<StringBuffer> fLineBuffer;
	/** Position from where buffer is written to. */
	private volatile int fPosition;
	/** True if the current line has not yet been written to. */
	private volatile boolean fNewLine = true;

	/**
	 * Creates new VerboseWriter that writes to the given PrintWriter. Output is
	 * buffered and previous entries in the buffer can be rewritten.
	 */
	public VerboseWriter(PrintWriter out) {
		fOutput = out;
		fLineBuffer = new Vector<>();
		fPosition = 0;
		fLineBuffer.add(new StringBuffer());
	}

	/**
	 * Terminate the current line by writing the line separator string. If
	 * autoflush is set and there are extra vebose lines caused by printHex,
	 * these lines are also printed.
	 */
	public synchronized void println() {
		while (fExtraVerboseLines > 0) {
			fExtraVerboseLines--;
			markLn();
		}

		markLn();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, byte value) {
		printDescription(description);
		printHex(value);
		println();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, short value) {
		printDescription(description);
		printHex(value);
		println();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, int value) {
		printDescription(description);
		printHex(value);
		println();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, long value) {
		printDescription(description);
		printHex(value);
		println();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, byte value, Map<Integer, String> valueToString) {
		printDescription(description);
		printHex(value);
		printValue(value, valueToString);
		println();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, short value, Map<Integer, String> valueToString) {
		printDescription(description);
		printHex(value);
		printValue(value, valueToString);
		println();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, int value, Map<Integer, String> valueToString) {
		printDescription(description);
		printHex(value);
		printValue(value, valueToString);
		println();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, byte value, String[] bitNames) {
		printDescription(description);
		printHex(value);
		printValue(value, bitNames);
		println();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, short value, String[] bitNames) {
		printDescription(description);
		printHex(value);
		printValue(value, bitNames);
		println();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, int value, String[] bitNames) {
		printDescription(description);
		printHex(value);
		printValue(value, bitNames);
		println();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, String value) {
		printDescription(description);
		printHex(value);
		print(value);
		println();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, boolean value) {
		printDescription(description);
		printHex(value);
		print(Boolean.toString(value));
		println();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, char value) {
		printDescription(description);
		printHex(value);
		print(value);
		println();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, double value) {
		printDescription(description);
		printHex(value);
		print(Double.toString(value));
		println();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, float value) {
		printDescription(description);
		printHex(value);
		print(Float.toString(value));
		println();
	}

	/**
	 * Prints verbose line.
	 */
	public synchronized void println(String description, byte[] value) {
		printDescription(description);
		printHex(value);
		println();
	}

	/**
	 * Prints string with right size.
	 */
	public synchronized void printWidth(String str, int width) {
		print(str);
		int spaces = width - str.length();
		if (spaces > 0) {
			for (int i = 0; i < spaces; i++) {
				print(' ');
			}
		}
	}

	/**
	 * Prints description string with right size plus its seperator spaces.
	 */
	public synchronized void printDescription(String str) {
		printWidth(str, VERBOSE_DESCRIPTION_LENGTH);
	}

	/**
	 * Prints hex substitution string with right size plus its seperator spaces.
	 */
	public synchronized void printHexSubstitution(String str) {
		// Note that bytes also start with a space.
		print(' ');
		printWidth(str, VERBOSE_HEX_WIDTH - 1);
	}

	/**
	 * Appends hex representation of given byte to an array.
	 */
	private static void appendHexByte(byte b, char[] buffer, int pos) {
		int count = 2;

		int abspos = 3 * pos;
		buffer[abspos] = ' ';
		do {
			int t = b & 15;
			if (t > 9) {
				t = t - 10 + 'a';
			} else {
				t += '0';
			}
			buffer[count-- + abspos] = (char) t;
			b >>>= 4;
		} while (count > 0);
	}

	/**
	 * Appends remaining spaces to hex dump.
	 */
	private static void appendHexSpaces(char[] buffer, int pos) {
		for (int i = 3 * pos; i <= VERBOSE_HEX_WIDTH - 3; i += 3) {
			buffer[i] = ' ';
			buffer[i + 1] = ' ';
			buffer[i + 2] = ' ';
		}

		// Two extra spaces as seperator
		buffer[VERBOSE_HEX_WIDTH - 1] = ' ';
		buffer[VERBOSE_HEX_WIDTH - 2] = ' ';
	}

	/**
	 * Prints hex representation of a byte.
	 */
	public synchronized void printHex(byte b) {
		char buffer[] = new char[VERBOSE_HEX_WIDTH];
		appendHexByte(b, buffer, 0);
		appendHexSpaces(buffer, 1);
		print(buffer);
	}

	/**
	 * Prints hex representation of an int.
	 */
	public synchronized void printHex(short s) {
		char buffer[] = new char[VERBOSE_HEX_WIDTH];
		for (int i = 1; i >= 0; i--) {
			appendHexByte((byte) (s >>> i * 8), buffer, 1 - i);
		}
		appendHexSpaces(buffer, 2);
		print(buffer);
	}

	/**
	 * Prints hex representation of an int.
	 */
	public synchronized void printHex(int integer) {
		char buffer[] = new char[VERBOSE_HEX_WIDTH];
		for (int i = 3; i >= 0; i--) {
			appendHexByte((byte) (integer >>> i * 8), buffer, 3 - i);
		}
		appendHexSpaces(buffer, 4);
		print(buffer);
	}

	/**
	 * Prints hex representation of a long.
	 */
	public synchronized void printHex(long l) {
		char buffer[] = new char[VERBOSE_HEX_WIDTH];
		for (int i = 7; i >= 0; i--) {
			appendHexByte((byte) (l >>> i * 8), buffer, 7 - i);
		}
		appendHexSpaces(buffer, 8);
		print(buffer);
	}

	/**
	 * Prints hex representation of a long.
	 * @param b the boolean
	 */
	public synchronized void printHex(boolean b) {
		printHexSubstitution("<boolean>"); //$NON-NLS-1$
	}

	/**
	 * Prints hex representation of a long.
	 * @param c the char
	 */
	public synchronized void printHex(char c) {
		printHexSubstitution("<char>"); //$NON-NLS-1$
	}

	/**
	 * Prints hex representation of a long.
	 * @param d the double
	 */
	public synchronized void printHex(double d) {
		printHexSubstitution("<double>"); //$NON-NLS-1$
	}

	/**
	 * Prints hex representation of a long.
	 * @param f the float
	 */
	public synchronized void printHex(float f) {
		printHexSubstitution("<float>"); //$NON-NLS-1$
	}

	/**
	 * Prints hex representation of a String.
	 * @param str the string
	 */
	public synchronized void printHex(String str) {
		printHexSubstitution("<string>"); //$NON-NLS-1$
	}

	/**
	 * Prints hex representation of a byte array. Note that this can span more
	 * than one line, but is considered to be part of one 'verbose line'.
	 * Therefore, a println after a printHex can result in more than one line
	 * being printed to the PrintWriter.
	 */
	public synchronized void printHex(byte[] bytes) {
		int startPosition = position();
		char linebuf[] = new char[VERBOSE_HEX_WIDTH];
		int extraLines = 0;
		int byteOnLine = 0;

		for (byte b : bytes) {
			if (byteOnLine == VERBOSE_HEX_BYTES_PER_LINE) {
				appendHexSpaces(linebuf, VERBOSE_HEX_BYTES_PER_LINE);
				if (extraLines++ > 0) {
					printDescription(""); //$NON-NLS-1$
				}
				print(linebuf);
				markLn();
				byteOnLine = 0;
			}
			appendHexByte(b, linebuf, byteOnLine++);
		}
		appendHexSpaces(linebuf, byteOnLine);
		if (extraLines > 0) {
			printDescription(""); //$NON-NLS-1$
		}

		fExtraVerboseLines += extraLines;
		print(linebuf);
		if (extraLines > 0) {
			gotoPosition(startPosition);
		}
	}

	/**
	 * Prints string representation of a value given a Map from values to
	 * strings.
	 */
	public synchronized void printValue(int value, Map<Integer, String> valueToString) {
		Integer val = Integer.valueOf(value);
		if (valueToString == null) {
			print(val.toString());
			return;
		}
		String result = valueToString.get(val);
		if (result == null) {
			print(val.toString() + JDIMessages.VerboseWriter___unknown_value__1);
		} else {
			print(result);
		}
	}

	/**
	 * Prints string representation of a value given a Vector with the names of
	 * the bits.
	 */
	public synchronized void printValue(byte value, String[] bitNames) {
		printValue(value & 0xff, bitNames);
	}

	/**
	 * Prints string representation of a value given a Vector with the names of
	 * the bits.
	 */
	public synchronized void printValue(short value, String[] bitNames) {
		printValue(value & 0xffff, bitNames);
	}

	/**
	 * Prints string representation of a value given a Vector with the names of
	 * the bits.
	 */
	public synchronized void printValue(int value, String[] bitNames) {
		Integer val = Integer.valueOf(value);
		if (bitNames == null) {
			print(val.toString());
			return;
		}

		boolean bitsSet = false;

		for (int i = 0; i < bitNames.length; i++) {
			// Test if bit is set in value.
			if ((1 << i & value) == 0) {
				continue;
			}

			// See if we have a desciption for the bit.
			String bitString = bitNames[i];
			if (bitString == null) {
				bitString = JDIMessages.VerboseWriter__unknown_bit__2;
			}

			if (!bitsSet) {
				print(bitString);
			} else {
				print(" & "); //$NON-NLS-1$
				print(bitString);
			}
			bitsSet = true;
		}

		if (!bitsSet) {
			print(JDIMessages.VerboseWriter__none__4);
		}
	}

	/**
	 * Checks if a new line is written to. If so, first erase any data on that
	 * line. Line is marked 'not new' after this command.
	 */
	private void checkForNewLine() {
		if (fNewLine) {
			getCurrentBuffer().setLength(0);
			fNewLine = false;
		}
	}

	/**
	 * Print a String.
	 */
	public synchronized void print(String str) {
		checkForNewLine();
		getCurrentBuffer().append(str);
	}

	/**
	 * Print a Character.
	 */
	public synchronized void print(char c) {
		checkForNewLine();
		getCurrentBuffer().append(c);
	}

	private StringBuffer getCurrentBuffer() {
		return fLineBuffer.get(nextPosition());
	}

	private int nextPosition() {
		int pos = fPosition;
		if (pos >= fLineBuffer.size()) {
			pos = fLineBuffer.size() - 1;
		}
		if (pos < 0) {
			pos = 0;
		}
		return pos;
	}

	/**
	 * Print array of Characters.
	 */
	public synchronized void print(char[] c) {
		checkForNewLine();
		getCurrentBuffer().append(c);
	}

	/**
	 * Print a String and then terminate the line.
	 */
	public synchronized void println(String str) {
		print(str);
		println();
	}

	/**
	 * Flush buffer. If autoflush is off, this method is synchronized on the
	 * PrintWriter given in the constructor.
	 */
	public synchronized void flush() {
		int bufSize = fLineBuffer.size();
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		for (int i = 0; i < bufSize - 1; i++) {
			pw.println(fLineBuffer.get(i));
		}

		// The last line should be printed without an extra newline
		StringBuffer lastLine = fLineBuffer.get(bufSize - 1);
		if (lastLine.length() > 0) {
			pw.print(lastLine);
		}
		fLineBuffer.clear();
		fPosition = 0;
		fLineBuffer.add(new StringBuffer());
		// sync on fOutput to avoid writing from multiple writer instances
		synchronized (fOutput) {
			fOutput.print(sw.toString());
			fOutput.flush();
		}
	}

	public synchronized void printStackTrace(Throwable t) {
		t.printStackTrace(fOutput);
	}

	/**
	 * Go to the given position in the buffer. If the given position is smaller
	 * than the current position, subsequent print commands overwrite existing
	 * lines in the buffer. Else, new lines are added to the buffer.
	 */
	public synchronized void gotoPosition(int pos) {
		int delta = pos - fPosition;
		if (delta < 0) {
			fPosition = pos;
		} else {
			while (delta-- > 0) {
				println();
			}
		}
	}

	/**
	 * Prints given number of lines.
	 */
	public synchronized void printLines(int lines) {
		gotoPosition(fPosition + lines);
	}

	/**
	 * @return Returns current position in buffer.
	 */
	public int position() {
		return fPosition;
	}

	/**
	 * Terminate the current line by writing the line separator string, start at
	 * end of next line.
	 */
	public synchronized void markLn() {
		if (++fPosition == fLineBuffer.size()) {
			fLineBuffer.add(new StringBuffer());
		}

		fNewLine = true;
	}
}
