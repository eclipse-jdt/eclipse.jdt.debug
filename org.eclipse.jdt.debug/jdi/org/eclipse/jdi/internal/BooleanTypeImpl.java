package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.BooleanType;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class BooleanTypeImpl extends PrimitiveTypeImpl implements BooleanType {
	/**
	 * Creates new instance.
	 */
	public BooleanTypeImpl(VirtualMachineImpl vmImpl) {
		super("BooleanType", vmImpl, "boolean" , "Z"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/**
	 * @returns primitive type tag.
	 */
	public byte tag() {
		return BooleanValueImpl.tag;
	}
	
	/**
	 * @return Create a null value instance of the type.
	 */
	public Value createNullValue() {
		return virtualMachineImpl().mirrorOf(false);
	}
}
