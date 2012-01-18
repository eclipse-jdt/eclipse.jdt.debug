/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * Copy of org.eclipse.jdt.core.Signature. The class is copied here solely for
 * the purpose of commenting out the line: CharOperation.replace(result,
 * C_DOLLAR, C_DOT); in the method toCharArray(char[]). See Bug 22165
 */
public class RuntimeSignature {
	public static final char C_BOOLEAN = 'Z';
	public static final char C_BYTE = 'B';
	public static final char C_CHAR = 'C';
	public static final char C_DOUBLE = 'D';
	public static final char C_FLOAT = 'F';
	public static final char C_INT = 'I';
	public static final char C_SEMICOLON = ';';
	public static final char C_LONG = 'J';
	public static final char C_SHORT = 'S';
	public static final char C_VOID = 'V';
	public static final char C_DOT = '.';
	public static final char C_DOLLAR = '$';
	public static final char C_ARRAY = '[';
	public static final char C_RESOLVED = 'L';
	public static final char C_UNRESOLVED = 'Q';
	public static final char C_NAME_END = ';';
	public static final char C_PARAM_START = '(';
	public static final char C_PARAM_END = ')';
	public static final String SIG_BOOLEAN = "Z"; //$NON-NLS-1$
	public static final String SIG_BYTE = "B"; //$NON-NLS-1$
	public static final String SIG_CHAR = "C"; //$NON-NLS-1$
	public static final String SIG_DOUBLE = "D"; //$NON-NLS-1$
	public static final String SIG_FLOAT = "F"; //$NON-NLS-1$
	public static final String SIG_INT = "I"; //$NON-NLS-1$
	public static final String SIG_LONG = "J"; //$NON-NLS-1$
	public static final String SIG_SHORT = "S"; //$NON-NLS-1$
	public static final String SIG_VOID = "V"; //$NON-NLS-1$
	private static final char[] NO_CHAR = new char[0];
	private static final char[] BOOLEAN = { 'b', 'o', 'o', 'l', 'e', 'a', 'n' };
	private static final char[] BYTE = { 'b', 'y', 't', 'e' };
	private static final char[] CHAR = { 'c', 'h', 'a', 'r' };
	private static final char[] DOUBLE = { 'd', 'o', 'u', 'b', 'l', 'e' };
	private static final char[] FLOAT = { 'f', 'l', 'o', 'a', 't' };
	private static final char[] INT = { 'i', 'n', 't' };
	private static final char[] LONG = { 'l', 'o', 'n', 'g' };
	private static final char[] SHORT = { 's', 'h', 'o', 'r', 't' };
	private static final char[] VOID = { 'v', 'o', 'i', 'd' };

	public static String toString(String signature)
			throws IllegalArgumentException {
		return new String(toCharArray(signature.toCharArray()));
	}

	public static char[] toCharArray(char[] signature)
			throws IllegalArgumentException {
		try {
			int sigLength = signature.length;

			if (sigLength == 0 || signature[0] == C_PARAM_START) {
				return toCharArray(signature, NO_CHAR, null, true, true);
			}

			// compute result length
			int resultLength = 0;
			int index = -1;
			while (signature[++index] == C_ARRAY) {
				resultLength += 2; // []
			}
			switch (signature[index]) {
			case C_BOOLEAN:
				resultLength += BOOLEAN.length;
				break;
			case C_BYTE:
				resultLength += BYTE.length;
				break;
			case C_CHAR:
				resultLength += CHAR.length;
				break;
			case C_DOUBLE:
				resultLength += DOUBLE.length;
				break;
			case C_FLOAT:
				resultLength += FLOAT.length;
				break;
			case C_INT:
				resultLength += INT.length;
				break;
			case C_LONG:
				resultLength += LONG.length;
				break;
			case C_SHORT:
				resultLength += SHORT.length;
				break;
			case C_VOID:
				resultLength += VOID.length;
				break;
			case C_RESOLVED:
			case C_UNRESOLVED:
				int end = CharOperation.indexOf(C_SEMICOLON, signature, index);
				if (end == -1)
					throw new IllegalArgumentException();
				int start = index + 1;
				resultLength += end - start;
				break;
			default:
				throw new IllegalArgumentException();
			}

			char[] result = new char[resultLength];
			copyType(signature, 0, result, 0, true);

			/**
			 * Converts '$' separated type signatures into '.' separated type
			 * signature. NOTE: This assumes that the type signature is an inner
			 * type signature. This is true in most cases, but someone can
			 * define a non-inner type name containing a '$'. However to tell
			 * the difference, we would have to resolve the signature, which
			 * cannot be done at this point.
			 */
			// CharOperation.replace(result, C_DOLLAR, C_DOT);

			return result;
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException();
		}
	}

	public static char[] toCharArray(char[] methodSignature, char[] methodName,
			char[][] parameterNames, boolean fullyQualifyTypeNames,
			boolean includeReturnType) {
		try {
			int firstParen = CharOperation.indexOf(C_PARAM_START,
					methodSignature);
			if (firstParen == -1)
				throw new IllegalArgumentException();

			int sigLength = methodSignature.length;

			// compute result length

			// method signature
			int paramCount = 0;
			int lastParen = -1;
			int resultLength = 0;
			signature: for (int i = firstParen; i < sigLength; i++) {
				switch (methodSignature[i]) {
				case C_ARRAY:
					resultLength += 2; // []
					continue signature;
				case C_BOOLEAN:
					resultLength += BOOLEAN.length;
					break;
				case C_BYTE:
					resultLength += BYTE.length;
					break;
				case C_CHAR:
					resultLength += CHAR.length;
					break;
				case C_DOUBLE:
					resultLength += DOUBLE.length;
					break;
				case C_FLOAT:
					resultLength += FLOAT.length;
					break;
				case C_INT:
					resultLength += INT.length;
					break;
				case C_LONG:
					resultLength += LONG.length;
					break;
				case C_SHORT:
					resultLength += SHORT.length;
					break;
				case C_VOID:
					resultLength += VOID.length;
					break;
				case C_RESOLVED:
				case C_UNRESOLVED:
					int end = CharOperation.indexOf(C_SEMICOLON,
							methodSignature, i);
					if (end == -1)
						throw new IllegalArgumentException();
					int start;
					if (fullyQualifyTypeNames) {
						start = i + 1;
					} else {
						start = CharOperation.lastIndexOf(C_DOT,
								methodSignature, i, end) + 1;
						if (start == 0)
							start = i + 1;
					}
					resultLength += end - start;
					i = end;
					break;
				case C_PARAM_START:
					// add space for "("
					resultLength++;
					continue signature;
				case C_PARAM_END:
					lastParen = i;
					if (includeReturnType) {
						if (paramCount > 0) {
							// remove space for ", " that was added with last
							// parameter and remove space that is going to be
							// added for ", " after return type
							// and add space for ") "
							resultLength -= 2;
						} // else
							// remove space that is going to be added for ", "
							// after return type
							// and add space for ") "
							// -> noop

						// decrement param count because it is going to be added
						// for return type
						paramCount--;
						continue signature;
					}
					if (paramCount > 0) {
						// remove space for ", " that was added with last
						// parameter and add space for ")"
						resultLength--;
					} else {
						// add space for ")"
						resultLength++;
					}
					break signature;
				default:
					throw new IllegalArgumentException();
				}
				resultLength += 2; // add space for ", "
				paramCount++;
			}

			// parameter names
			if(parameterNames != null) {
				for (int i = 0; i < parameterNames.length; i++) {
					resultLength += parameterNames[i].length + 1;
					// parameter name + space
				}
			}

			// selector
			int selectorLength = methodName == null ? 0 : methodName.length;
			resultLength += selectorLength;

			// create resulting char array
			char[] result = new char[resultLength];

			// returned type
			int index = 0;
			if (includeReturnType) {
				long pos = copyType(methodSignature, lastParen + 1, result,
						index, fullyQualifyTypeNames);
				index = (int) (pos >>> 32);
				result[index++] = ' ';
			}

			// selector
			if (methodName != null) {
				System.arraycopy(methodName, 0, result, index, selectorLength);
				index += selectorLength;
			}

			// parameters
			result[index++] = C_PARAM_START;
			int sigPos = firstParen + 1;
			for (int i = 0; i < paramCount; i++) {
				long pos = copyType(methodSignature, sigPos, result, index,
						fullyQualifyTypeNames);
				index = (int) (pos >>> 32);
				sigPos = (int) pos;
				if (parameterNames != null) {
					result[index++] = ' ';
					char[] parameterName = parameterNames[i];
					int paramLength = parameterName.length;
					System.arraycopy(parameterName, 0, result, index,
							paramLength);
					index += paramLength;
				}
				if (i != paramCount - 1) {
					result[index++] = ',';
					result[index++] = ' ';
				}
			}
			if (sigPos >= sigLength) {
				throw new IllegalArgumentException();
				// should be on last paren
			}
			result[index++] = C_PARAM_END;

			return result;
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException();
		}
	}

	private static long copyType(char[] signature, int sigPos, char[] dest,
			int index, boolean fullyQualifyTypeNames) {
		int arrayCount = 0;
		loop: while (true) {
			switch (signature[sigPos++]) {
			case C_ARRAY:
				arrayCount++;
				break;
			case C_BOOLEAN:
				int length = BOOLEAN.length;
				System.arraycopy(BOOLEAN, 0, dest, index, length);
				index += length;
				break loop;
			case C_BYTE:
				length = BYTE.length;
				System.arraycopy(BYTE, 0, dest, index, length);
				index += length;
				break loop;
			case C_CHAR:
				length = CHAR.length;
				System.arraycopy(CHAR, 0, dest, index, length);
				index += length;
				break loop;
			case C_DOUBLE:
				length = DOUBLE.length;
				System.arraycopy(DOUBLE, 0, dest, index, length);
				index += length;
				break loop;
			case C_FLOAT:
				length = FLOAT.length;
				System.arraycopy(FLOAT, 0, dest, index, length);
				index += length;
				break loop;
			case C_INT:
				length = INT.length;
				System.arraycopy(INT, 0, dest, index, length);
				index += length;
				break loop;
			case C_LONG:
				length = LONG.length;
				System.arraycopy(LONG, 0, dest, index, length);
				index += length;
				break loop;
			case C_SHORT:
				length = SHORT.length;
				System.arraycopy(SHORT, 0, dest, index, length);
				index += length;
				break loop;
			case C_VOID:
				length = VOID.length;
				System.arraycopy(VOID, 0, dest, index, length);
				index += length;
				break loop;
			case C_RESOLVED:
			case C_UNRESOLVED:
				int end = CharOperation.indexOf(C_SEMICOLON, signature, sigPos);
				if (end == -1)
					throw new IllegalArgumentException();
				int start;
				if (fullyQualifyTypeNames) {
					start = sigPos;
				} else {
					start = CharOperation.lastIndexOf(C_DOT, signature, sigPos,
							end) + 1;
					if (start == 0)
						start = sigPos;
				}
				length = end - start;
				System.arraycopy(signature, start, dest, index, length);
				sigPos = end + 1;
				index += length;
				break loop;
			}
		}
		while (arrayCount-- > 0) {
			dest[index++] = '[';
			dest[index++] = ']';
		}
		return (((long) index) << 32) + sigPos;
	}
}
