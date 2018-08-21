/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import java.util.ArrayList;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.util.Util;

public class SignatureExt {

	public static char[][] getTypeSuperClassInterfaces(char[] typeSignature)
			throws IllegalArgumentException {
		try {
			int length = typeSignature.length;
			if (length == 0)
				return CharOperation.NO_CHAR_CHAR;
			int i = 0;
			if (typeSignature[0] == Signature.C_GENERIC_START) {
				i++; // leading '<'
				while (i < length
						&& typeSignature[i] != Signature.C_GENERIC_END) {
					i = CharOperation.indexOf(Signature.C_COLON, typeSignature,
							i);
					if (i < 0 || i >= length)
						throw new IllegalArgumentException();
					// iterate over bounds
					nextBound: while (typeSignature[i] == ':') {
						i++; // skip colon
						if (typeSignature[i] == ':') {
							continue nextBound; // empty bound
						}
						i = Util.scanTypeSignature(typeSignature, i);
						i++; // position at start of next param if any
					}
				}
				if (i < 0 || i >= length)
					throw new IllegalArgumentException();
				i++; // trailing '>'
			}
			ArrayList<char[]> superList = new ArrayList<>();
			while (i < length) {
				int superStart = i;
				i = Util.scanTypeSignature(typeSignature, i);
				i++;
				superList.add(CharOperation.subarray(typeSignature, superStart,
						i));
			}
			char[][] result;
			superList.toArray(result = new char[superList.size()][]);
			return result;
		} catch (ArrayIndexOutOfBoundsException e) {
			// invalid signature, fall through
		}
		throw new IllegalArgumentException();
	}

	public static String[] getTypeSuperClassInterfaces(String typeSignature)
			throws IllegalArgumentException {
		char[][] params = getTypeSuperClassInterfaces(typeSignature
				.toCharArray());
		return CharOperation.toStrings(params);
	}
}
