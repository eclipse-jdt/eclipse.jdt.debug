/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 */
public class GenericSignature {
	
	private static final char C_BOOLEAN= 'Z';
	private static final char C_BYTE= 'B';
	private static final char C_CHAR= 'C';
	private static final char C_SHORT= 'S';
	private static final char C_DOUBLE= 'D';
	private static final char C_FLOAT= 'F';
	private static final char C_INT= 'I';
	private static final char C_LONG= 'J';
	private static final char C_CLASS_TYPE= 'L';
	private static final char C_TYPE_VARIABLE= 'T';
	private static final char C_ARRAY= '[';
	private static final char C_VOID= 'V';
	private static final char C_WILDCARD_PLUS= '+';
	private static final char C_WILDCARD_MINUS= '-';
	
	private static final char C_TYPE_END= ';';
	private static final char C_PARAMETERS_START= '(';
	private static final char C_PARAMETERS_END= ')';
	private static final char C_EXCEPTION_START= '^';
	private static final char C_TYPE_ARGUMENTS_START= '<';
	private static final char C_TYPE_ARGUMENTS_END= '>';
	private static final char C_TYPE_PARAMETERS_START= '<';
	private static final char C_TYPE_PARAMETERS_END= '>';
	private static final char C_TYPE_PARAMETERS_COLON= ':';
	
	public static String getReturnType(String methodSignature) {
		int parametersEnd= methodSignature.lastIndexOf(C_PARAMETERS_END);
		if (parametersEnd == -1) {
			throw new IllegalArgumentException();
		}
		int exceptionStart= methodSignature.indexOf(C_EXCEPTION_START, parametersEnd + 1);
		if (exceptionStart == -1) {
			return methodSignature.substring(parametersEnd + 1);
		} else {
			return methodSignature.substring(parametersEnd + 1, exceptionStart);
		}
	}
	
	public static List getArgumentsSignature(String methodSignature) {
		int parameterStart= methodSignature.indexOf(C_PARAMETERS_START);
		int parametersEnd= methodSignature.lastIndexOf(C_PARAMETERS_END);
		if (parameterStart == -1 || parametersEnd == -1) {
			throw new IllegalArgumentException();
		}
		return getTypeSignatureList(methodSignature.substring(parameterStart + 1, parametersEnd));
	}
	
	public static List getTypeParameters(String genericClassSignature) {
		List parameters= new ArrayList();
		if (genericClassSignature.charAt(0) == C_TYPE_PARAMETERS_START) {
			int pos= 1;
			while (genericClassSignature.charAt(pos) != C_TYPE_PARAMETERS_END) {
				int identEnd= genericClassSignature.indexOf(C_TYPE_PARAMETERS_COLON, pos);
				parameters.add(genericClassSignature.substring(pos, identEnd));
				pos= identEnd + 1;
				// jump class bound, can be empty
				if (genericClassSignature.charAt(pos) != C_TYPE_PARAMETERS_COLON) {
					pos+= nextTypeSignatureLength(genericClassSignature, pos);
				}
				// jump interface bounds
				while (genericClassSignature.charAt(pos) == C_TYPE_PARAMETERS_COLON) {
					pos+= nextTypeSignatureLength(genericClassSignature, ++pos);
				}
			}
		}
		
		return parameters;
	}
	
	public static String signatureToName(String typeSignature) {
		String name= null;
		char char0= typeSignature.charAt(0);
		int length= typeSignature.length();
		switch (char0) {
			case C_BOOLEAN:
				checkLength(typeSignature, 1);
				name= "boolean"; //$NON-NLS-1$
				break;
			case C_BYTE:
				checkLength(typeSignature, 1);
				name= "byte"; //$NON-NLS-1$
				break;
			case C_CHAR:
				checkLength(typeSignature, 1);
				name= "char"; //$NON-NLS-1$
				break;
			case C_SHORT:
				checkLength(typeSignature, 1);
				name= "short";  //$NON-NLS-1$
				break;
			case C_DOUBLE:
				checkLength(typeSignature, 1);
				name= "double"; //$NON-NLS-1$
				break;
			case C_FLOAT:
				checkLength(typeSignature, 1);
				name= "float"; //$NON-NLS-1$
				break;
			case C_INT:
				checkLength(typeSignature, 1);
				name= "int"; //$NON-NLS-1$
				break;
			case C_LONG:
				checkLength(typeSignature, 1);
				name= "long"; //$NON-NLS-1$
				break;
			case C_VOID:
				checkLength(typeSignature, 1);
				name= "void"; //$NON-NLS-1$
				break;
			case C_ARRAY:
				name= signatureToName(typeSignature.substring(1)) + "[]"; //$NON-NLS-1$
				break;
			case C_WILDCARD_PLUS:
				name= "? extends " + signatureToName(typeSignature.substring(1)); //$NON-NLS-1$
				break;
			case C_WILDCARD_MINUS:
				name= "? super " + signatureToName(typeSignature.substring(1)); //$NON-NLS-1$
				break;
			case C_CLASS_TYPE:
				checkChar(typeSignature, length - 1, C_TYPE_END);
				int argumentStart= typeSignature.indexOf(C_TYPE_ARGUMENTS_START, 1);
				if (argumentStart != -1) {
					checkChar(typeSignature, length - 2, C_TYPE_ARGUMENTS_END);
					name= typeSignature.substring(1, argumentStart).replace('/', '.') + C_TYPE_ARGUMENTS_START + nameListToString(listSignaturetoListName(getTypeSignatureList(typeSignature.substring(argumentStart + 1, length - 2)))) + C_TYPE_ARGUMENTS_END;
				} else {
					name= typeSignature.substring(1, length - 1).replace('/', '.');
				}
				break;
			case C_TYPE_VARIABLE:
				checkChar(typeSignature, length - 1, C_TYPE_END);
				name= typeSignature.substring(1, length - 1);
				break;
			default:
				throw new IllegalArgumentException();
		}
		
		return name;
	}
	
	public static String nameListToString(List names) {
		StringBuffer string= new StringBuffer();
		Iterator iterator= names.iterator();
		if (iterator.hasNext()) {
			string.append(iterator.next());
			while (iterator.hasNext()) {
				string.append(',').append(iterator.next());
			}
		}
		return string.toString();
	}
	
	public static List listSignaturetoListName(List signatures) {
		List names= new ArrayList(signatures.size());
		for (Iterator iterator= signatures.iterator(); iterator.hasNext();) {
			names.add(signatureToName((String)iterator.next()));
		}
		return names;
	}
	
	private static List getTypeSignatureList(String typeSignatureList) {
		List list= new ArrayList();
		int pos= 0;
		while (pos < typeSignatureList.length()) {
			int signatureLength= nextTypeSignatureLength(typeSignatureList, pos);
			list.add(typeSignatureList.substring(pos, pos+= signatureLength));
		}
		return list;
	}
	
	private static int nextTypeSignatureLength(String signature, int startPos) {
		int inclusionLevel= 0;
		for (int i= startPos, length= signature.length(); i < length; i++) {
			if (inclusionLevel == 0) {
				switch (signature.charAt(i)) {
					case C_CLASS_TYPE:
					case C_TYPE_VARIABLE:
					case C_WILDCARD_PLUS:
					case C_WILDCARD_MINUS:
						inclusionLevel= 1;
						break;
					case C_ARRAY:
						break;
					default:
						return 1;
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
	
	private static void checkChar(String string, int index, char character) {
		if (string.charAt(index) != character) {
			throw new IllegalArgumentException();
		}
	}
	
	private static void checkLength(String string, int length) {
		if (string.length() != length) {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * @returns Returns true if signature is a class signature.
	 */
	public static boolean isClassSignature(String signature) {
		return signature.charAt(0) == C_CLASS_TYPE;
	}

	/**
	 * @returns Returns true if signature is an array signature.
	 */
	public static boolean isArraySignature(String signature) {
		return signature.charAt(0) == C_ARRAY;
	}

	/**
	 * @returns Returns true if signature is an primitive signature.
	 */
	public static boolean isPrimitiveSignature(String signature) {
		switch (signature.charAt(0)) {
			case C_BOOLEAN:
			case C_BYTE:
			case C_CHAR:
			case C_SHORT:
			case C_INT:
			case C_LONG:
			case C_FLOAT:
			case C_DOUBLE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * @returns Returns true if signature is void signature.
	 */
	public static boolean isVoidSignature(String signature) {
		return signature.charAt(0) == C_VOID;
	}
	
}
