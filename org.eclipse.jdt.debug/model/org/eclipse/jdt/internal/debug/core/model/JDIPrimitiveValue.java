/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	@Override
	public boolean getBooleanValue() {
		return getUnderlyingPrimitiveValue().booleanValue();
	}

	/*
	 * @see IJavaPrimitiveValue#getByteValue()
	 */
	@Override
	public byte getByteValue() {
		return getUnderlyingPrimitiveValue().byteValue();
	}

	/*
	 * @see IJavaPrimitiveValue#getCharValue()
	 */
	@Override
	public char getCharValue() {
		return getUnderlyingPrimitiveValue().charValue();
	}

	/*
	 * @see IJavaPrimitiveValue#getDoubleValue()
	 */
	@Override
	public double getDoubleValue() {
		return getUnderlyingPrimitiveValue().doubleValue();
	}

	/*
	 * @see IJavaPrimitiveValue#getFloatValue()
	 */
	@Override
	public float getFloatValue() {
		return getUnderlyingPrimitiveValue().floatValue();
	}

	/*
	 * @see IJavaPrimitiveValue#getIntValue()
	 */
	@Override
	public int getIntValue() {
		return getUnderlyingPrimitiveValue().intValue();
	}

	/*
	 * @see IJavaPrimitiveValue#getLongValue()
	 */
	@Override
	public long getLongValue() {
		return getUnderlyingPrimitiveValue().longValue();
	}

	/*
	 * @see IJavaPrimitiveValue#getShortValue()
	 */
	@Override
	public short getShortValue() {
		return getUnderlyingPrimitiveValue().shortValue();
	}

}
