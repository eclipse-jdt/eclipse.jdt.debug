package org.eclipse.jdi.internal.spy;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintWriter;
import java.util.Vector;

public class BufWriter {
	/** PrintWriter that is written to. */
	private PrintWriter fOutput;
	/** Buffer for output: one StringBuffer entry per line. */
	private Vector fLineBuffer;
	/** Position from where buffer is written to. */
	private int fPosition;
	/** True if the current line has not yet been written to. */
	private boolean fNewLine = true;

	/**
	 * Creates new BufWriter that writes to the given PrintWriter.
	 * Output is buffered and previous entries in the buffer can be rewritten.
	 */
	public BufWriter(PrintWriter out) {
		fOutput = out;
		fLineBuffer = new Vector();
		fPosition = 0;
		fLineBuffer.add(new StringBuffer());
	}
	
	/**
	 * Checks if a new line is written to. If so, first erase any data on that line.
	 * Line is marked 'not new' after this command.
	 */
	private void checkForNewLine() {
		if (fNewLine) {
			((StringBuffer)(fLineBuffer.elementAt(fPosition))).setLength(0);
			fNewLine = false;
		}
	}
	
	/**
	 * Print a String.
	 */
	public void print(String str) {
		checkForNewLine();
		((StringBuffer)(fLineBuffer.elementAt(fPosition))).append(str);
	}

	/**
	 * Print a Character.
	 */
	public void print(char c) {
		checkForNewLine();
		((StringBuffer)(fLineBuffer.elementAt(fPosition))).append(c);
	}

	/**
	 * Print array of Characters.
	 */
	public void print(char[] c) {
		checkForNewLine();
		((StringBuffer)(fLineBuffer.elementAt(fPosition))).append(c);
	}

	/**
	 * Print a String and then terminate the line.
	 */
	public void println(String str) {
		print(str);
		println();
	}

	/**
	 * Terminate the current line by writing the line separator string, start at end of next line.
	 */
	public void println() {
		if (++fPosition == fLineBuffer.size())
			fLineBuffer.add(new StringBuffer());
			
		fNewLine = true;
	}

	/**
	 * Flush buffer.
	 * If autoflush is off, this method is synchronized on the PrintWriter given in the constructor.
	 */
	public void flush() {
		synchronized(fOutput) {
			int bufSize = fLineBuffer.size();
				
			for (int i = 0; i < bufSize - 1; i++)
				fOutput.println(new String((StringBuffer)fLineBuffer.elementAt(i)));
	
			// The last line should be printed without an extra newline
			StringBuffer lastLine = (StringBuffer)fLineBuffer.elementAt(bufSize - 1);
			if (lastLine.length() > 0)
				fOutput.print(new String(lastLine));
	
			fOutput.flush();
			fLineBuffer.clear();
			fPosition = 0;
			fLineBuffer.add(new StringBuffer());
		}
	}
	
	/**
	 * Go to the given position in the buffer.
	 * If the given position is smaller than the current position,
	 * subsequent print commands overwrite existing lines in the buffer.
	 * Else, new lines are added to the buffer.
	 */
	public void gotoPosition(int pos) {
		int delta = pos - fPosition;
		if (delta < 0) {
			fPosition = pos;
		} else {
			while (delta-- > 0)
				println();
		}
	}

	/**
	 * Prints given number of lines.
	 */
	public void printLines(int lines) {
		gotoPosition(fPosition + lines);
	}

	/**
	 * @return Returns current position in buffer.
	 */
	public int position() {
		return fPosition;
	}
}