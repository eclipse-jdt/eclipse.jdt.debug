/*******************************************************************************
 *  Copyright (c) 2003, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.util.NLS;

import com.sun.jdi.AbsentInformationException;

public class SourceDebugExtensionParser {

	private static class Lexer {

		static final int UNKNOWN = 0;
		static final int SMAP = 1;
		static final int NON_ASTERISK_STRING = 2;
		static final int NUMBER = 3;
		static final int CR = 4;
		static final int ASTERISK_CHAR = 5;
		static final int ASTERISK_C = 6;
		static final int ASTERISK_E = 7;
		static final int ASTERISK_F = 8;
		static final int ASTERISK_L = 9;
		static final int ASTERISK_O = 10;
		static final int ASTERISK_S = 11;
		static final int ASTERISK_V = 12;
		// never used
		// static final int WHITE_SPACE= 13;
		static final int COLON = 14;
		static final int COMMA = 15;
		static final int SHARP = 16;
		static final int PLUS = 17;

		private final char[] fSmap;
		private int fPointer;
		private char fChar;

		private char[] fLexem;
		private int fLexemType;

		private boolean fEOF;

		public Lexer(String smap) {
			fSmap = smap.toCharArray();
			fLexemType = UNKNOWN;
			fPointer = -1;
			nextChar();
		}

		/**
		 * Compute the next lexem.
		 *
		 * @return the type of the next lexem.
		 */
		public int nextLexem() throws AbsentInformationException {
			if (fEOF) {
				throw new AbsentInformationException(
						JDIMessages.SourceDebugExtensionParser_0);
			}
			startWith();
			return fLexemType;
		}

		private char nextChar() {
			if (++fPointer == fSmap.length) {
				fEOF = true;
				return '\000';
			}
			fChar = fSmap[fPointer];
			return fChar;
		}

		private void startWith() throws AbsentInformationException {
			switch (fChar) {
			case '\n':
			case '\r':
				startWithCR();
				break;
			case '*':
				startWithAsterisk();
				break;
			case ':':
				fLexem = new char[] { ':' };
				fLexemType = COLON;
				nextChar();
				break;
			case ',':
				fLexem = new char[] { ',' };
				fLexemType = COMMA;
				nextChar();
				break;
			case '#':
				fLexem = new char[] { '#' };
				fLexemType = SHARP;
				nextChar();
				break;
			case '+':
				fLexem = new char[] { '+' };
				fLexemType = PLUS;
				nextChar();
				break;
			default:
				startWithOtherChar();
				break;
			}
		}

		private void startWithOtherChar() {
			int lexemStart = fPointer;
			consumeWhiteSpace();
			if (fChar >= '0' && fChar <= '9') { // a number
				number(lexemStart);
			} else {
				nonAsteriskString(lexemStart);
			}
		}

		private void nonAsteriskString(int lexemStart) {
			while (fChar != '\n' && fChar != '\r' && !fEOF) {
				nextChar();
			}
			int length = fPointer - lexemStart;
			fLexem = new char[length];
			System.arraycopy(fSmap, lexemStart, fLexem, 0, length);
			if (length == 4 && fLexem[0] == 'S' && fLexem[1] == 'M'
					&& fLexem[2] == 'A' && fLexem[3] == 'P') {
				fLexemType = SMAP;
			} else {
				fLexemType = NON_ASTERISK_STRING;
			}
		}

		private void number(int lexemStart) {
			while (fChar >= '0' && fChar <= '9') {
				nextChar();
			}
			consumeWhiteSpace();
			fLexemType = NUMBER;
			int length = fPointer - lexemStart;
			fLexem = new char[length];
			System.arraycopy(fSmap, lexemStart, fLexem, 0, length);
		}

		private void startWithAsterisk() throws AbsentInformationException {
			nextChar();
			if (fEOF) {
				throw new AbsentInformationException(
						JDIMessages.SourceDebugExtensionParser_0);
			}
			switch (fChar) {
			case 'C':
				fLexemType = ASTERISK_C;
				break;
			case 'E':
				fLexemType = ASTERISK_E;
				break;
			case 'F':
				fLexemType = ASTERISK_F;
				break;
			case 'L':
				fLexemType = ASTERISK_L;
				break;
			case 'O':
				fLexemType = ASTERISK_O;
				break;
			case 'S':
				fLexemType = ASTERISK_S;
				break;
			case 'V':
				fLexemType = ASTERISK_V;
				break;
			default:
				fLexemType = ASTERISK_CHAR;
				break;
			}
			fLexem = new char[] { '*', fChar };
			nextChar();
		}

		private void startWithCR() {
			if (fChar == '\r') {
				if (nextChar() == '\n') {
					fLexem = new char[] { '\r', '\n' };
					nextChar();
				} else {
					fLexem = new char[] { '\r' };
				}
			} else {
				fLexem = new char[] { fChar };
				nextChar();
			}
			fLexemType = CR;
		}

		private void consumeWhiteSpace() {
			while (fChar == ' ' || fChar == '\t') {
				nextChar();
			}
		}

		/**
		 * @return the value of the current lexem.
		 */
		public char[] lexem() {
			return fLexem;
		}

		/**
		 * @return the type of the current lexem.
		 */
		public int lexemType() {
			return fLexemType;
		}

	}

	/**
	 * The reference type to which this source debug extension is associated.
	 */
	private final ReferenceTypeImpl fReferenceType;

	private final List<String> fDefinedStrata;

	// parser data;
	private ReferenceTypeImpl.Stratum fCurrentStratum;
	private boolean fFileSectionDefinedForCurrentStratum;
	private boolean fLineSectionDefinedForCurrentStratum;
	private int fCurrentLineFileId;

	public static void parse(String smap, ReferenceTypeImpl referenceType)
			throws AbsentInformationException {
		new SourceDebugExtensionParser(referenceType).parseSmap(smap);
	}

	/**
	 * SourceDebugExtension constructor.
	 */
	private SourceDebugExtensionParser(ReferenceTypeImpl referenceType) {
		fReferenceType = referenceType;
		fDefinedStrata = new ArrayList<>();
		fDefinedStrata.add(VirtualMachineImpl.JAVA_STRATUM_NAME);
	}

	private void parseSmap(String smap) throws AbsentInformationException {
		Lexer lexer = new Lexer(smap);
		parseHeader(lexer);
		parseSections(lexer);
		if (!fDefinedStrata.contains(fReferenceType.defaultStratum())) {
			throw new AbsentInformationException(
					JDIMessages.SourceDebugExtensionParser_2);
		}
	}

	private void parseHeader(Lexer lexer) throws AbsentInformationException {
		int lexemType = lexer.nextLexem();
		if (lexemType != Lexer.SMAP) {
			throw new AbsentInformationException(
					JDIMessages.SourceDebugExtensionParser_3);
		}
		if (lexer.nextLexem() != Lexer.CR) {
			throw new AbsentInformationException(
					JDIMessages.SourceDebugExtensionParser_4);
		}
		if (isAsteriskLexem(lexer.nextLexem())) {
			throw new AbsentInformationException(
					JDIMessages.SourceDebugExtensionParser_5);
		}
		fReferenceType.setOutputFileName(getNonAsteriskString(lexer));
		if (isAsteriskLexem(lexer.lexemType())) {
			throw new AbsentInformationException(
					JDIMessages.SourceDebugExtensionParser_6);
		}
		fReferenceType.setDefaultStratumId(getNonAsteriskString(lexer));
	}

	private void parseSections(Lexer lexer) throws AbsentInformationException {
		while (lexer.lexemType() != Lexer.ASTERISK_E) {
			parseStratumSection(lexer);
		}
	}

	private void parseStratumSection(Lexer lexer)
			throws AbsentInformationException {
		if (lexer.lexemType() != Lexer.ASTERISK_S) {
			throw new AbsentInformationException(
					JDIMessages.SourceDebugExtensionParser_7);
		}
		if (isAsteriskLexem(lexer.nextLexem())) {
			throw new AbsentInformationException(
					JDIMessages.SourceDebugExtensionParser_8);
		}
		String stratumId = getNonAsteriskString(lexer);
		if (fDefinedStrata.contains(stratumId)) {
			throw new AbsentInformationException(NLS.bind(
					JDIMessages.SourceDebugExtensionParser_9,
					stratumId));
		}
		fCurrentStratum = new ReferenceTypeImpl.Stratum(stratumId);
		fFileSectionDefinedForCurrentStratum = false;
		fLineSectionDefinedForCurrentStratum = false;
		int lexemType = lexer.lexemType();
		while (lexemType != Lexer.ASTERISK_E && lexemType != Lexer.ASTERISK_S) {
			switch (lexemType) {
			case Lexer.ASTERISK_F:
				if (fFileSectionDefinedForCurrentStratum) {
					throw new AbsentInformationException(NLS.bind(
							JDIMessages.SourceDebugExtensionParser_10,
							stratumId));
				}
				parseFileSection(lexer);
				fFileSectionDefinedForCurrentStratum = true;
				break;
			case Lexer.ASTERISK_L:
				if (fLineSectionDefinedForCurrentStratum) {
					throw new AbsentInformationException(NLS.bind(
							JDIMessages.SourceDebugExtensionParser_11,
							stratumId));
				}
				parseLineSection(lexer);
				fLineSectionDefinedForCurrentStratum = true;
				break;
			case Lexer.ASTERISK_V:
				parseVendorSection(lexer);
				break;
			case Lexer.ASTERISK_CHAR:
				parseFutureSection(lexer);
				break;
			default:
				throw new AbsentInformationException(NLS.bind(
						JDIMessages.SourceDebugExtensionParser_12,
						new String(lexer.lexem())));
			}
			lexemType = lexer.lexemType();
		}
		if (!fFileSectionDefinedForCurrentStratum) {
			throw new AbsentInformationException(NLS.bind(
					JDIMessages.SourceDebugExtensionParser_13,
					stratumId));
		}
		if (!fLineSectionDefinedForCurrentStratum) {
			throw new AbsentInformationException(NLS.bind(
					JDIMessages.SourceDebugExtensionParser_14,
					stratumId));
		}
		fDefinedStrata.add(stratumId);
		fReferenceType.addStratum(fCurrentStratum);
	}

	private void parseFileSection(Lexer lexer)
			throws AbsentInformationException {
		if (lexer.nextLexem() != Lexer.CR) {
			throw new AbsentInformationException(NLS.bind(
					JDIMessages.SourceDebugExtensionParser_12,
					new String(lexer.lexem())));
		}
		lexer.nextLexem();
		while (!isAsteriskLexem(lexer.lexemType())) {
			parseFileInfo(lexer);
		}
	}

	private void parseFileInfo(Lexer lexer) throws AbsentInformationException {
		int lexemType = lexer.lexemType();
		switch (lexemType) {
			case Lexer.NUMBER:
				int fileId = integerValue(lexer.lexem());
				if (isAsteriskLexem(lexer.nextLexem())) {
					throw new AbsentInformationException(
							JDIMessages.SourceDebugExtensionParser_16);
				}
				fCurrentStratum.addFileInfo(fileId, getNonAsteriskString(lexer));
				break;
			case Lexer.PLUS:
				if (lexer.nextLexem() != Lexer.NUMBER) {
					throw new AbsentInformationException(
							JDIMessages.SourceDebugExtensionParser_17);
				}
				fileId = integerValue(lexer.lexem());
				if (isAsteriskLexem(lexer.nextLexem())) {
					throw new AbsentInformationException(
							JDIMessages.SourceDebugExtensionParser_16);
				}
				String fileName = getNonAsteriskString(lexer);
				if (isAsteriskLexem(lexer.lexemType())) {
					throw new AbsentInformationException(
							JDIMessages.SourceDebugExtensionParser_19);
				}
				fCurrentStratum.addFileInfo(fileId, fileName, getNonAsteriskString(lexer));
				break;
			default:
				throw new AbsentInformationException(NLS.bind(JDIMessages.SourceDebugExtensionParser_12, new String(lexer.lexem())));
		}
	}

	private void parseLineSection(Lexer lexer)
			throws AbsentInformationException {
		fCurrentLineFileId = 0;
		if (lexer.nextLexem() != Lexer.CR) {
			throw new AbsentInformationException(NLS.bind(
					JDIMessages.SourceDebugExtensionParser_12,
					new String(lexer.lexem())));
		}
		lexer.nextLexem();
		while (!isAsteriskLexem(lexer.lexemType())) {
			parseLineInfo(lexer);
		}
	}

	private void parseLineInfo(Lexer lexer) throws AbsentInformationException {
		if (lexer.lexemType() != Lexer.NUMBER) {
			throw new AbsentInformationException(
					JDIMessages.SourceDebugExtensionParser_22);
		}
		int inputStartLine = integerValue(lexer.lexem());
		int lexemType = lexer.nextLexem();
		if (lexemType == Lexer.SHARP) {
			if (lexer.nextLexem() != Lexer.NUMBER) {
				throw new AbsentInformationException(
						JDIMessages.SourceDebugExtensionParser_23);
			}
			fCurrentLineFileId = integerValue(lexer.lexem());
			lexemType = lexer.nextLexem();
		}
		int repeatCount;
		if (lexemType == Lexer.COMMA) {
			if (lexer.nextLexem() != Lexer.NUMBER) {
				throw new AbsentInformationException(
						JDIMessages.SourceDebugExtensionParser_24);
			}
			repeatCount = integerValue(lexer.lexem());
			lexemType = lexer.nextLexem();
		} else {
			repeatCount = 1;
		}
		if (lexemType != Lexer.COLON) {
			throw new AbsentInformationException(
					JDIMessages.SourceDebugExtensionParser_25);
		}
		if (lexer.nextLexem() != Lexer.NUMBER) {
			throw new AbsentInformationException(
					JDIMessages.SourceDebugExtensionParser_26);
		}
		int outputStartLine = integerValue(lexer.lexem());
		lexemType = lexer.nextLexem();
		int outputLineIncrement;
		if (lexemType == Lexer.COMMA) {
			if (lexer.nextLexem() != Lexer.NUMBER) {
				throw new AbsentInformationException(
						JDIMessages.SourceDebugExtensionParser_27);
			}
			outputLineIncrement = integerValue(lexer.lexem());
			lexemType = lexer.nextLexem();
		} else {
			outputLineIncrement = 1;
		}
		if (lexemType != Lexer.CR) {
			throw new AbsentInformationException(
					JDIMessages.SourceDebugExtensionParser_28);
		}
		lexer.nextLexem();
		fCurrentStratum.addLineInfo(inputStartLine, fCurrentLineFileId,
				repeatCount, outputStartLine, outputLineIncrement);
	}

	private void parseVendorSection(Lexer lexer)
			throws AbsentInformationException {
		if (lexer.nextLexem() != Lexer.CR) {
			throw new AbsentInformationException(NLS.bind(
					JDIMessages.SourceDebugExtensionParser_12,
					new String(lexer.lexem())));
		}
		lexer.nextLexem();
		while (!isAsteriskLexem(lexer.lexemType())) {
			// do nothing in this case, just consume the lexems.
			getNonAsteriskString(lexer);
		}
	}

	private void parseFutureSection(Lexer lexer)
			throws AbsentInformationException {
		if (lexer.nextLexem() != Lexer.CR) {
			throw new AbsentInformationException(NLS.bind(
					JDIMessages.SourceDebugExtensionParser_12,
					new String(lexer.lexem())));
		}
		lexer.nextLexem();
		while (!isAsteriskLexem(lexer.lexemType())) {
			// do nothing in this case, just consume the lexems.
			getNonAsteriskString(lexer);
		}
	}

	private String getNonAsteriskString(Lexer lexer)
			throws AbsentInformationException {
		StringBuilder string = new StringBuilder();
		int lexemType = lexer.lexemType();
		while (lexemType != Lexer.CR) {
			string.append(lexer.lexem());
			lexemType = lexer.nextLexem();
		}
		lexer.nextLexem();
		// remove the leading white spaces
		int i = -1, length = string.length();
		char c;
		while (++i < length && ((c = string.charAt(i)) == ' ' || c == '\t')) {
			//continue until we chew up all the whitespace or hit the end of the line
		}
		return string.delete(0, i).toString();
	}

	private int integerValue(char[] lexem) {
		int i = 0;
		char c = lexem[0];
		while (c == ' ' || c == '\t') {
			c = lexem[++i];
		}
		int value = 0;
		while (c >= '0' && c <= '9') {
			value = value * 10 + c - '0';
			if (++i == lexem.length) {
				break;
			}
			c = lexem[i];
		}
		return value;
	}

	private boolean isAsteriskLexem(int lexemType) {
		switch (lexemType) {
		case Lexer.ASTERISK_C:
		case Lexer.ASTERISK_E:
		case Lexer.ASTERISK_F:
		case Lexer.ASTERISK_L:
		case Lexer.ASTERISK_O:
		case Lexer.ASTERISK_S:
		case Lexer.ASTERISK_V:
		case Lexer.ASTERISK_CHAR:
			return true;
		default:
			return false;
		}
	}

}
