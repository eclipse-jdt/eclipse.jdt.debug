/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.model.IPrimitiveType;

/**
 * @version 	1.0
 * @author
 */
public class EvaluationPrimitiveType
//	extends EvaluationType
	implements IPrimitiveType {
		
	private String fName;
	private String fSignature;
	
	static private final IPrimitiveType byteType = new EvaluationPrimitiveType("byte", "B");
	
	static private final IPrimitiveType charType = new EvaluationPrimitiveType("char", "C");
	
	static private final IPrimitiveType doubleType = new EvaluationPrimitiveType("double", "D");
	
	static private final IPrimitiveType floatType = new EvaluationPrimitiveType("float", "F");
	
	static private final IPrimitiveType intType = new EvaluationPrimitiveType("int", "I");
	
	static private final IPrimitiveType longType = new EvaluationPrimitiveType("long", "J");
	
	static private final IPrimitiveType shortType = new EvaluationPrimitiveType("short", "S");
	
	static private final IPrimitiveType booleanType = new EvaluationPrimitiveType("boolean", "Z");
	
	
	
	static public IPrimitiveType getType(String signature) {
		switch (signature.charAt(0)) {
			case 'B':
				return byteType;
			case 'C':
				return charType;
			case 'D':
				return doubleType;
			case 'F':
				return floatType;
			case 'I':
				return intType;
			case 'J':
				return longType;
			case 'S':
				return shortType;
			case 'Z':
				return booleanType;
		}
		return null;
	}

	/**
	 * Constructor for EvaluationPrimitiveType.
	 * @param type
	 */
	protected EvaluationPrimitiveType(String name, String signature) {
		fName = name;
		fSignature = signature;
	}

	/*
	 * @see IType#getName()
	 */
	public String getName() throws CoreException {
		return fName;
	}

	/*
	 * @see IType#getSignature()
	 */
	public String getSignature() throws CoreException {
		return fSignature;
	}

}
