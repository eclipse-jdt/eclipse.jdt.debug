package org.eclipse.jdt.internal.debug.eval.ast;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.jdt.debug.eval.ast.model.IPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * A primitive value
 */
public class EvaluationPrimitiveValue
	extends EvaluationValue
	implements IPrimitiveValue {

	/**
	 * Constructs a primitive value.
	 * 
	 * @param value underlying Java debug model value
	 */
	protected EvaluationPrimitiveValue(IJavaPrimitiveValue value) {
		super(value);
	}

	/**
	 * Returns the underlying Java debug model primitive value.
	 * 
	 * @return underlying Java debug model primitive value
	 */
	protected IJavaPrimitiveValue getJavaPrimitiveValue() {
		return (IJavaPrimitiveValue)getJavaValue();
	}
	
	/*
	 * @see IPrimitiveValue#getBooleanValue()
	 */
	public boolean getBooleanValue() {
		return getJavaPrimitiveValue().getBooleanValue();
	}

	/*
	 * @see IPrimitiveValue#getByteValue()
	 */
	public byte getByteValue() {
		return getJavaPrimitiveValue().getByteValue();
	}

	/*
	 * @see IPrimitiveValue#getCharValue()
	 */
	public char getCharValue() {
		return getJavaPrimitiveValue().getCharValue();
	}

	/*
	 * @see IPrimitiveValue#getDoubleValue()
	 */
	public double getDoubleValue() {
		return getJavaPrimitiveValue().getDoubleValue();
	}

	/*
	 * @see IPrimitiveValue#getFloatValue()
	 */
	public float getFloatValue() {
		return getJavaPrimitiveValue().getFloatValue();
	}

	/*
	 * @see IPrimitiveValue#getIntValue()
	 */
	public int getIntValue() {
		return getJavaPrimitiveValue().getIntValue();
	}

	/*
	 * @see IPrimitiveValue#getLongValue()
	 */
	public long getLongValue() {
		return getJavaPrimitiveValue().getLongValue();
	}

	/*
	 * @see IPrimitiveValue#getShortValue()
	 */
	public short getShortValue() {
		return getJavaPrimitiveValue().getShortValue();
	}

}

