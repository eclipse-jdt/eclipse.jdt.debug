/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class to perform operations on generic signatures
 */
public class GenericSignature {

	private static final char C_CLASS_TYPE = 'L';
	private static final char C_TYPE_VARIABLE = 'T';
	private static final char C_ARRAY = '[';
	private static final char C_WILDCARD_PLUS = '+';
	private static final char C_WILDCARD_MINUS = '-';

	private static final char C_TYPE_END = ';';
	private static final char C_PARAMETERS_START = '(';
	private static final char C_PARAMETERS_END = ')';
	private static final char C_TYPE_ARGUMENTS_START = '<';
	private static final char C_TYPE_ARGUMENTS_END = '>';

	public static List<String> getParameterTypes(String methodSignature) {
		int parameterStart = methodSignature.indexOf(C_PARAMETERS_START);
		int parametersEnd = methodSignature.lastIndexOf(C_PARAMETERS_END);
		if (parameterStart == -1 || parametersEnd == -1) {
			// used to throw illegal argument exception, but now return empty
			// list if we can't parse it
			return Collections.EMPTY_LIST;
		}
		return getTypeSignatureList(methodSignature.substring(parameterStart + 1, parametersEnd));
	}

	private static List<String> getTypeSignatureList(String typeSignatureList) {
		List<String> list = new ArrayList<String>();
		int pos = 0;
		while (pos < typeSignatureList.length()) {
			int signatureLength = nextTypeSignatureLength(typeSignatureList, pos);
			list.add(typeSignatureList.substring(pos, pos += signatureLength));
		}
		return list;
	}

	private static int nextTypeSignatureLength(String signature, int startPos) {
		int inclusionLevel = 0;
		for (int i = startPos, length = signature.length(); i < length; i++) {
			if (inclusionLevel == 0) {
				switch (signature.charAt(i)) {
				case C_CLASS_TYPE:
				case C_TYPE_VARIABLE:
				case C_WILDCARD_PLUS:
				case C_WILDCARD_MINUS:
					inclusionLevel = 1;
					break;
				case C_ARRAY:
					break;
				default:
					return i - startPos + 1;
				}
			} else {
				switch (signature.charAt(i)) {
				case C_TYPE_END:
					if (inclusionLevel == 1) {
						return i - startPos + 1;
					}
					break;
				case C_TYPE_ARGUMENTS_START:
					inclusionLevel++;
					break;
				case C_TYPE_ARGUMENTS_END:
					inclusionLevel--;
					break;
				}
			}
		}
		throw new IllegalArgumentException();
	}
}
