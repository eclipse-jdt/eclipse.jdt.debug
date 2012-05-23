/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;

import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;

/**
 * A primitive value on a Java debug target
 */
public class JDIPrimitiveValue extends JDIValue implements IJavaPrimitiveValue {

	/**
	 * Constructs a new primitive value.
	 * 
	 * @param target
	 *            the Java debug target
	 * @param value
	 *            the underlying JDI primitive value
	 */
	public JDIPrimitiveValue(JDIDebugTarget target, Value value) {
		super(target, value);
	}

	/**
	 * Returns this value's underlying primtive value
	 * 
	 * @return underlying primtive value
	 */
	protected PrimitiveValue getUnderlyingPrimitiveValue() {
		return (PrimitiveValue) getUnderlyingValue();
	}

	/*
	 * @see IJavaPrimitiveValue#getBooleanValue()
	 */
	public boolean getBooleanValue() {
		return getUnderlyingPrimitiveValue().booleanValue();
	}

	/*
	 * @see IJavaPrimitiveValue#getByteValue()
	 */
	public byte getByteValue() {
		return getUnderlyingPrimitiveValue().byteValue();
	}

	/*
	 * @see IJavaPrimitiveValue#getCharValue()
	 */
	public char getCharValue() {
		return getUnderlyingPrimitiveValue().charValue();
	}

	/*
	 * @see IJavaPrimitiveValue#getDoubleValue()
	 */
	public double getDoubleValue() {
		return getUnderlyingPrimitiveValue().doubleValue();
	}

	/*
	 * @see IJavaPrimitiveValue#getFloatValue()
	 */
	public float getFloatValue() {
		return getUnderlyingPrimitiveValue().floatValue();
	}

	/*
	 * @see IJavaPrimitiveValue#getIntValue()
	 */
	public int getIntValue() {
		return getUnderlyingPrimitiveValue().intValue();
	}

	/*
	 * @see IJavaPrimitiveValue#getLongValue()
	 */
	public long getLongValue() {
		return getUnderlyingPrimitiveValue().longValue();
	}

	/*
	 * @see IJavaPrimitiveValue#getShortValue()
	 */
	public short getShortValue() {
		return getUnderlyingPrimitiveValue().shortValue();
	}

}
