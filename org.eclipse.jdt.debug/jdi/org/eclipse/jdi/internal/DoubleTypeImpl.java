package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.DoubleType;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class DoubleTypeImpl extends PrimitiveTypeImpl implements DoubleType {
	/**
	 * Creates new instance.
	 */
	public DoubleTypeImpl(VirtualMachineImpl vmImpl) {
		super("DoubleType", vmImpl, "double" , "D");
	}
	
	/**
	 * @returns primitive type tag.
	 */
	public byte tag() {
		return DoubleValueImpl.tag;
	}
	
	/**
	 * @return Create a null value instance of the type.
	 */
	public Value createNullValue() {
		return virtualMachineImpl().mirrorOf((double)0);
	}
}
