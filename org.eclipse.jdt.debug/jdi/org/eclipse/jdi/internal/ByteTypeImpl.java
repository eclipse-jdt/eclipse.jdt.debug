package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.ByteType;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class ByteTypeImpl extends PrimitiveTypeImpl implements ByteType {
	/**
	 * Creates new instance.
	 */
	public ByteTypeImpl(VirtualMachineImpl vmImpl) {
		super("ByteType", vmImpl, "byte" , "B");
	}
	
	/**
	 * @returns primitive type tag.
	 */
	public byte tag() {
		return ByteValueImpl.tag;
	}
	
	/**
	 * @return Create a null value instance of the type.
	 */
	public Value createNullValue() {
		return virtualMachineImpl().mirrorOf((byte)0);
	}
}
