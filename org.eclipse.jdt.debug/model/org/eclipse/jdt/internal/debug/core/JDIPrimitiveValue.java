package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
 
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
	 * @param target the Java debug target
	 * @param value the underlying JDI primitive value
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
		return (PrimitiveValue)getUnderlyingValue();
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

