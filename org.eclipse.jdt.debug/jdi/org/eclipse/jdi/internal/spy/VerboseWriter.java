package org.eclipse.jdi.internal.spy;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;
import java.io.*;

public class VerboseWriter extends BufWriter {
	/** Length of verbose description. */
	public static final int VERBOSE_DESCRIPTION_LENGTH = 21;
	/** Number of hexadecimal verbose bytes per line. */
	public static final int VERBOSE_HEX_BYTES_PER_LINE = 16;
	/** Width of hex dump. */
	public static final int VERBOSE_HEX_WIDTH = 16*3+2;
	
	/** Number extra verbose lines. These are caused by hex dumps that span more than one line. */
	int fExtraVerboseLines = 0;
	
	/**
	 * Creates new VerboseWriter.
	 */
	public VerboseWriter(PrintWriter out) {
		super(out);
	}
	
	/**
	 * Terminate the current line by writing the line separator string.
	 * If autoflush is set and there are extra vebose lines caused by printHex, these lines are
	 * also printed.
	 */
	public void println() {
		while (fExtraVerboseLines > 0) {
			fExtraVerboseLines--;
			super.println();
		}
		
		super.println();
	}

	/**
	 * Prints verbose line.
	 */
	public void println(String description, byte value) {
		printDescription(description);
		printHex(value);
		println();
	}
	
	/**
	 * Prints verbose line.
	 */
	public void println(String description, short value) {
		printDescription(description);
		printHex(value);
		println();
	}
	
	/**
	 * Prints verbose line.
	 */
	public void println(String description, int value) {
		printDescription(description);
		printHex(value);
		println();
	}
	
	/**
	 * Prints verbose line.
	 */
	public void println(String description, long value) {
		printDescription(description);
		printHex(value);
		println();
	}
	
	/**
	 * Prints verbose line.
	 */
	public void println(String description, byte value, Map valueToString) {
		printDescription(description);
		printHex(value);
		printValue(value, valueToString);
		println();
	}
	
	/**
	 * Prints verbose line.
	 */
	public void println(String description, short value, Map valueToString) {
		printDescription(description);
		printHex(value);
		printValue(value, valueToString);
		println();
	}
	
	/**
	 * Prints verbose line.
	 */
	public void println(String description, int value, Map valueToString) {
		printDescription(description);
		printHex(value);
		printValue(value, valueToString);
		println();
	}
	
	/**
	 * Prints verbose line.
	 */
	public void println(String description, byte value, Vector bitNames) {
		printDescription(description);
		printHex(value);
		printValue(value, bitNames);
		println();
	}
	
	/**
	 * Prints verbose line.
	 */
	public void println(String description, short value, Vector bitNames) {
		printDescription(description);
		printHex(value);
		printValue(value, bitNames);
		println();
	}
	
	/**
	 * Prints verbose line.
	 */
	public void println(String description, int value, Vector bitNames) {
		printDescription(description);
		printHex(value);
		printValue(value, bitNames);
		println();
	}
	
	/**
	 * Prints verbose line.
	 */
	public void println(String description, String value) {
		printDescription(description);
		printHex(value);
		print(value);
		println();
	}
	
	/**
	 * Prints verbose line.
	 */
	public void println(String description, boolean value) {
		printDescription(description);
		printHex(value);
		print(new Boolean(value).toString());
		println();
	}
	
	/**
	 * Prints verbose line.
	 */
	public void println(String description, char value) {
		printDescription(description);
		printHex(value);
		print(value);
		println();
	}
	
	/**
	 * Prints verbose line.
	 */
	public void println(String description, double value) {
		printDescription(description);
		printHex(value);
		print(new Double(value).toString());
		println();
	}
	
	/**
	 * Prints verbose line.
	 */
	public void println(String description, float value) {
		printDescription(description);
		printHex(value);
		print(new Float(value).toString());
		println();
	}
	
	/**
	 * Prints verbose line.
	 */
	public void println(String description, byte[] value) {
		printDescription(description);
		printHex(value);
		println();
	}
	
	/**
	 * Prints string with right size.
	 */
	public void printWidth(String str, int width) {
		print(str);
		int spaces = width - str.length();
		if (spaces > 0) {
			for (int i = 0; i < spaces; i++)
				print(' ');
		}
	}
	
	/**
	 * Prints description string with right size plus its seperator spaces.
	 */
	public void printDescription(String str) {
		printWidth(str, VERBOSE_DESCRIPTION_LENGTH);
	}
	
	/**
	 * Prints hex substitution string with right size plus its seperator spaces.
	 */
	public void printHexSubstitution(String str) {
		// Note that bytes also start with a space.
		print(' ');
		printWidth(str, VERBOSE_HEX_WIDTH - 1);
	}
	
	/**
	 * Appends hex representation of given byte to an array.
	 */
	private static void appendHexByte(byte b, char[] buffer, int pos) {
		int count = 2;
	
		int abspos = 3*pos;
		buffer[abspos] = ' ';
		do {
			int t = b & 15;
			if (t > 9)
				t = t - 10 + (int) 'a';
			else
				t += (int) '0';
			buffer[count-- + abspos] = (char) t;
			b >>>= 4;
		} while (count > 0);
	}
	
	/**
	 * Appends remaining spaces to hex dump.
	 */
	private static void appendHexSpaces(char[] buffer, int pos) {
		for (int i = 3*pos; i <= VERBOSE_HEX_WIDTH - 3; i+=3) {
			buffer[i] = ' ';
			buffer[i+1] = ' ';
			buffer[i+2] = ' ';
		}
		
		// Two extra spaces as seperator
		buffer[VERBOSE_HEX_WIDTH - 1] = ' ';
		buffer[VERBOSE_HEX_WIDTH - 2] = ' ';
	}
	
	/**
	 * Prints hex representation of a byte.
	 */
	public void printHex(byte b) {
		char buffer[] = new char[VERBOSE_HEX_WIDTH];
		appendHexByte((byte)b, buffer, 0);
		appendHexSpaces(buffer, 1);
		print(buffer);
	}
	
	/**
	 * Prints hex representation of an int.
	 */
	public void printHex(short s) {
		char buffer[] = new char[VERBOSE_HEX_WIDTH];
		for (int i = 1; i >= 0; i--)
			appendHexByte((byte)(s >>> i*8), buffer, 1 - i);
		appendHexSpaces(buffer, 2);
		print(buffer);
	}
	
	/**
	 * Prints hex representation of an int.
	 */
	public void printHex(int integer) {
		char buffer[] = new char[VERBOSE_HEX_WIDTH];
		for (int i = 3; i >= 0; i--)
			appendHexByte((byte)(integer >>> i*8), buffer, 3 - i);
		appendHexSpaces(buffer, 4);
		print(buffer);
	}
	
	/**
	 * Prints hex representation of a long.
	 */
	public void printHex(long l) {
		char buffer[] = new char[VERBOSE_HEX_WIDTH];
		for (int i = 7; i >= 0; i--)
			appendHexByte((byte)(l >>> i*8), buffer, 7 - i);
		appendHexSpaces(buffer, 8);
		print(buffer);
	}
	
	/**
	 * Prints hex representation of a long.
	 */
	public void printHex(boolean b) {
		printHexSubstitution("<boolean>");
	}
	
	/**
	 * Prints hex representation of a long.
	 */
	public void printHex(char c) {
		printHexSubstitution("<char>");
	}
	
	/**
	 * Prints hex representation of a long.
	 */
	public void printHex(double d) {
		printHexSubstitution("<double>");
	}
	
	/**
	 * Prints hex representation of a long.
	 */
	public void printHex(float f) {
		printHexSubstitution("<float>");
	}
	
	/**
	 * Prints hex representation of a String.
	 */
	public void printHex(String str) {
		printHexSubstitution("<string>");
	}
	
	/**
	 * Prints hex representation of a byte array.
	 * Note that this can span more than one line, but is considered to be part of one
	 * 'verbose line'. Therefore, a println after a printHex can result in more than one line
	 * being printed to the PrintWriter.
	 */
	public void printHex(byte[] bytes) {
		int startPosition = position();
		char linebuf[] = new char[VERBOSE_HEX_WIDTH];
		int extraLines = 0;
		int byteOnLine = 0;

		for (int i = 0; i < bytes.length; i++) {
			if (byteOnLine == VERBOSE_HEX_BYTES_PER_LINE) {
				appendHexSpaces(linebuf, VERBOSE_HEX_BYTES_PER_LINE);
				if (extraLines++ > 0)
					printDescription("");
				print(linebuf);
				super.println();
				byteOnLine = 0;
			}
			appendHexByte(bytes[i], linebuf, byteOnLine++);
		}
		appendHexSpaces(linebuf, byteOnLine);
		if (extraLines > 0)
			printDescription("");
			
		fExtraVerboseLines += extraLines;
		print(linebuf);
		if (extraLines > 0)
			gotoPosition(startPosition);
	}

	/**
	 * Prints string representation of a value given a Map from values to strings.
	 */
	public void printValue(int value, Map valueToString) {
		Integer val = new Integer(value);
		if (valueToString == null)
			print(val.toString());

		String result = (String)valueToString.get(val);
		if (result == null)
			print(val.toString() + " <unknown value>");
		else
			print(result);
	}
	
	/**
	 * Prints string representation of a value given a Vector with the names of the bits.
	 */
	public void printValue(byte value, Vector bitNames) {
		printValue(value & 0xff, bitNames);
	}

	/**
	 * Prints string representation of a value given a Vector with the names of the bits.
	 */
	public void printValue(short value, Vector bitNames) {
		printValue(value & 0xffff, bitNames);
	}

	/**
	 * Prints string representation of a value given a Vector with the names of the bits.
	 */
	public void printValue(int value, Vector bitNames) {
		Integer val = new Integer(value);
		if (bitNames == null)
			print(val.toString());
			
		boolean bitsSet = false;
			
		for (int i = 0; i < bitNames.size(); i++) {
			// Test if bit is set in value.
			if ((1 << i & value) == 0)
				continue;

			// See if we have a desciption for the bit.
			String bitString = (String)bitNames.elementAt(i);
			if (bitString == null)
				bitString = "<unknown bit>";

			if (!bitsSet) {
				print(bitString);
			} else {
				print(" & ");
				print(bitString);
			}
			bitsSet = true;
		}

		if (!bitsSet)
			print("<none>");
	}
	
	/**
	 * @return Returns string without XXX_ prefix.
	 */
	public static String removePrefix(String str) {
		int i = str.indexOf('_');
		if (i < 0)
			return str;
		else
			return str.substring(i + 1);
	}
}