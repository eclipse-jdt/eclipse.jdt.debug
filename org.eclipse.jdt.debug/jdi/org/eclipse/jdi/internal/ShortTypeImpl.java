package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.ShortType;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class ShortTypeImpl extends PrimitiveTypeImpl implements ShortType {
	/**
	 * Creates new instance.
	 */
	public ShortTypeImpl(VirtualMachineImpl vmImpl) {
		super("ShortType", vmImpl, "short" , "S");
	}
	
	/**
	 * @returns primitive type tag.
	 */
	public byte tag() {
		return ShortValueImpl.tag;
	}
	
	/**
	 * @return Create a null value instance of the type.
	 */
	public Value createNullValue() {
		return virtualMachineImpl().mirrorOf((short)0);
	}
}
