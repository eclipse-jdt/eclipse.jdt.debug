package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.LongType;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class LongTypeImpl extends PrimitiveTypeImpl implements LongType {
	/**
	 * Creates new instance.
	 */
	public LongTypeImpl(VirtualMachineImpl vmImpl) {
		super("LongType", vmImpl, "long" , "J"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/**
	 * @returns primitive type tag.
	 */
	public byte tag() {
		return LongValueImpl.tag;
	}
	
	/**
	 * @return Create a null value instance of the type.
	 */
	public Value createNullValue() {
		return virtualMachineImpl().mirrorOf((long)0);
	}
}
