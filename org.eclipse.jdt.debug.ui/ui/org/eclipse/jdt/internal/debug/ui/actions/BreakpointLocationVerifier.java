package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.compiler.parser.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalSymbols;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class BreakpointLocationVerifier {

	/**
	 * Returns the line number closest to the given line number that represents a
	 * valid location for a breakpoint in the given document, or -1 if a valid location
	 * cannot be found.
	 */
	public int getValidBreakpointLocation(IDocument doc, int lineNumber) {

		Scanner scanner= new Scanner();
		boolean found= false;
		int start= 0, length= 0, token= 0, lastToken= 0;

		while (!found) {
			try {
				start= doc.getLineOffset(lineNumber);
				length= doc.getLineLength(lineNumber);
				char[] txt= doc.get(start, length).toCharArray();
				scanner.setSourceBuffer(txt);
				token= scanner.getNextToken();
				lastToken= 0;

				while (token != TerminalSymbols.TokenNameEOF) {
					if (isNonIdentifierValidToken(token)) {
						found= true;
						break;
					} else if (token == TerminalSymbols.TokenNameIdentifier) {
						if (lastToken == TerminalSymbols.TokenNameIdentifier || isPrimitiveTypeToken(lastToken)
						|| lastToken == TerminalSymbols.TokenNameRBRACKET) {
							//var declaration..is there initialization
							lastToken= token;
							token= scanner.getNextToken();
							if (token == TerminalSymbols.TokenNameSEMICOLON) {
								//no init
								break;
							} else {
								found= true;
								break;
							}
						}
					} else if (lastToken == TerminalSymbols.TokenNameIdentifier 
								&& token != TerminalSymbols.TokenNameLBRACKET) {
						found= true;
						break;
					} 
						
					lastToken= token;
					token= scanner.getNextToken();
				}
				if (!found) {
					lineNumber++;
				}
			} catch (BadLocationException ble) {
				return -1;
			} catch (InvalidInputException ie) {
				return -1;
			}
		}
		// add 1 to the line number - Document is 0 based, JDI is 1 based
		return lineNumber + 1;
	}
	
	
	protected boolean isPrimitiveTypeToken(int token) {
		return token == TerminalSymbols.TokenNameboolean ||
			token == TerminalSymbols.TokenNameint ||
			token == TerminalSymbols.TokenNamechar ||
			token == TerminalSymbols.TokenNamebyte ||
			token == TerminalSymbols.TokenNamefloat ||
			token == TerminalSymbols.TokenNamedouble ||
			token == TerminalSymbols.TokenNamelong ||
			token == TerminalSymbols.TokenNameshort;
	}
	
	protected boolean isNonIdentifierValidToken(int token) {
		return token == TerminalSymbols.TokenNamebreak ||
				token == TerminalSymbols.TokenNamecontinue ||
				token == TerminalSymbols.TokenNamereturn ||
				token == TerminalSymbols.TokenNamethis ||
				token == TerminalSymbols.TokenNamesuper;
	}
}
