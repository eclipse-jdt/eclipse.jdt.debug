/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal, dependency free JSON reader used for the optional JRE configuration file.
 * <p>
 * Produces plain Java values: {@link Map} (insertion ordered) for objects, {@link List} for arrays, {@link String},
 * {@link Number} ({@link Long} or {@link Double}), {@link Boolean} and <code>null</code>.
 * </p>
 * <p>
 * In addition to strict JSON the reader tolerates <code>//</code> and <code>/* ... *&#47;</code> comments as well as
 * trailing commas so that fragments copied from editor settings files (JSONC) can be used unchanged.
 * </p>
 *
 * @since 3.25
 */
public final class SimpleJsonParser {

	/**
	 * Thrown when the input is not well formed.
	 */
	public static final class JsonException extends Exception {
		private static final long serialVersionUID = 1L;

		JsonException(String message, int line, int column) {
			super(message + " (line " + line + ", column " + column + ')'); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private final String fText;
	private int fPos;
	private int fLine = 1;
	private int fLineStart;

	private SimpleJsonParser(String text) {
		fText = text;
	}

	/**
	 * Parses the given JSON text.
	 *
	 * @param text JSON text
	 * @return the parsed value
	 * @throws JsonException if the text is not well formed
	 */
	public static Object parse(String text) throws JsonException {
		SimpleJsonParser parser = new SimpleJsonParser(text);
		parser.skipWhitespace();
		Object value = parser.readValue();
		parser.skipWhitespace();
		if (parser.fPos < text.length()) {
			throw parser.error("Unexpected trailing content"); //$NON-NLS-1$
		}
		return value;
	}

	private Object readValue() throws JsonException {
		if (fPos >= fText.length()) {
			throw error("Unexpected end of input"); //$NON-NLS-1$
		}
		char c = fText.charAt(fPos);
		switch (c) {
			case '{':
				return readObject();
			case '[':
				return readArray();
			case '"':
				return readString();
			case 't':
				expectLiteral("true"); //$NON-NLS-1$
				return Boolean.TRUE;
			case 'f':
				expectLiteral("false"); //$NON-NLS-1$
				return Boolean.FALSE;
			case 'n':
				expectLiteral("null"); //$NON-NLS-1$
				return null;
			default:
				if (c == '-' || (c >= '0' && c <= '9')) {
					return readNumber();
				}
				throw error("Unexpected character '" + c + '\''); //$NON-NLS-1$
		}
	}

	private Map<String, Object> readObject() throws JsonException {
		Map<String, Object> result = new LinkedHashMap<>();
		fPos++; // '{'
		skipWhitespace();
		if (peek() == '}') {
			fPos++;
			return result;
		}
		while (true) {
			skipWhitespace();
			if (peek() == '}') { // trailing comma
				fPos++;
				return result;
			}
			if (peek() != '"') {
				throw error("Expected string key"); //$NON-NLS-1$
			}
			String key = readString();
			skipWhitespace();
			if (peek() != ':') {
				throw error("Expected ':'"); //$NON-NLS-1$
			}
			fPos++;
			skipWhitespace();
			result.put(key, readValue());
			skipWhitespace();
			char c = peek();
			if (c == ',') {
				fPos++;
			} else if (c == '}') {
				fPos++;
				return result;
			} else {
				throw error("Expected ',' or '}'"); //$NON-NLS-1$
			}
		}
	}

	private List<Object> readArray() throws JsonException {
		List<Object> result = new ArrayList<>();
		fPos++; // '['
		skipWhitespace();
		if (peek() == ']') {
			fPos++;
			return result;
		}
		while (true) {
			skipWhitespace();
			if (peek() == ']') { // trailing comma
				fPos++;
				return result;
			}
			result.add(readValue());
			skipWhitespace();
			char c = peek();
			if (c == ',') {
				fPos++;
			} else if (c == ']') {
				fPos++;
				return result;
			} else {
				throw error("Expected ',' or ']'"); //$NON-NLS-1$
			}
		}
	}

	private String readString() throws JsonException {
		StringBuilder buffer = new StringBuilder();
		fPos++; // opening quote
		while (true) {
			if (fPos >= fText.length()) {
				throw error("Unterminated string"); //$NON-NLS-1$
			}
			char c = fText.charAt(fPos++);
			if (c == '"') {
				return buffer.toString();
			}
			if (c == '\\') {
				if (fPos >= fText.length()) {
					throw error("Unterminated escape sequence"); //$NON-NLS-1$
				}
				char e = fText.charAt(fPos++);
				switch (e) {
					case '"':
					case '\\':
					case '/':
						buffer.append(e);
						break;
					case 'b':
						buffer.append('\b');
						break;
					case 'f':
						buffer.append('\f');
						break;
					case 'n':
						buffer.append('\n');
						break;
					case 'r':
						buffer.append('\r');
						break;
					case 't':
						buffer.append('\t');
						break;
					case 'u':
						if (fPos + 4 > fText.length()) {
							throw error("Invalid unicode escape"); //$NON-NLS-1$
						}
						try {
							buffer.append((char) Integer.parseInt(fText.substring(fPos, fPos + 4), 16));
						} catch (NumberFormatException ex) {
							throw error("Invalid unicode escape"); //$NON-NLS-1$
						}
						fPos += 4;
						break;
					default:
						throw error("Invalid escape sequence '\\" + e + '\''); //$NON-NLS-1$
				}
			} else {
				if (c == '\n') {
					fLine++;
					fLineStart = fPos;
				}
				buffer.append(c);
			}
		}
	}

	private Number readNumber() throws JsonException {
		int start = fPos;
		boolean floating = false;
		while (fPos < fText.length()) {
			char c = fText.charAt(fPos);
			if ((c >= '0' && c <= '9') || c == '-' || c == '+') {
				fPos++;
			} else if (c == '.' || c == 'e' || c == 'E') {
				floating = true;
				fPos++;
			} else {
				break;
			}
		}
		String token = fText.substring(start, fPos);
		try {
			if (floating) {
				return Double.valueOf(token);
			}
			return Long.valueOf(token);
		} catch (NumberFormatException e) {
			throw error("Invalid number '" + token + '\''); //$NON-NLS-1$
		}
	}

	private void expectLiteral(String literal) throws JsonException {
		if (!fText.startsWith(literal, fPos)) {
			throw error("Expected '" + literal + '\''); //$NON-NLS-1$
		}
		fPos += literal.length();
	}

	private void skipWhitespace() throws JsonException {
		while (fPos < fText.length()) {
			char c = fText.charAt(fPos);
			if (c == '\n') {
				fLine++;
				fPos++;
				fLineStart = fPos;
			} else if (c == ' ' || c == '\t' || c == '\r' || c == '\uFEFF') {
				fPos++;
			} else if (c == '/' && fPos + 1 < fText.length() && fText.charAt(fPos + 1) == '/') {
				while (fPos < fText.length() && fText.charAt(fPos) != '\n') {
					fPos++;
				}
			} else if (c == '/' && fPos + 1 < fText.length() && fText.charAt(fPos + 1) == '*') {
				int end = fText.indexOf("*/", fPos + 2); //$NON-NLS-1$
				if (end < 0) {
					throw error("Unterminated comment"); //$NON-NLS-1$
				}
				for (int i = fPos; i < end; i++) {
					if (fText.charAt(i) == '\n') {
						fLine++;
						fLineStart = i + 1;
					}
				}
				fPos = end + 2;
			} else {
				return;
			}
		}
	}

	private char peek() throws JsonException {
		if (fPos >= fText.length()) {
			throw error("Unexpected end of input"); //$NON-NLS-1$
		}
		return fText.charAt(fPos);
	}

	private JsonException error(String message) {
		return new JsonException(message, fLine, fPos - fLineStart + 1);
	}
}
