package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.CharType;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class CharTypeImpl extends PrimitiveTypeImpl implements CharType {
	/**
	 * Creates new instance.
	 */
	public CharTypeImpl(VirtualMachineImpl vmImpl) {
		super("CharType", vmImpl, "char" , "C"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/**
	 * @returns primitive type tag.
	 */
	public byte tag() {
		return CharValueImpl.tag;
	}
	
	/**
	 * @return Create a null value instance of the type.
	 */
	public Value createNullValue() {
		return virtualMachineImpl().mirrorOf((char)0);
	}
}
