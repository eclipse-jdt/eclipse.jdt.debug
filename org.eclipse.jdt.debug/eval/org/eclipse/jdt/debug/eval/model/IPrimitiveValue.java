package org.eclipse.jdt.debug.eval.model;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
/**
 * A primitive value.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public interface IPrimitiveValue extends IValue {
	
	/**
	 * Returns this value as a boolean.
	 * 
	 * @return this value as a boolean
	 */
	public boolean getBooleanValue();
	
	/**
	 * Returns this value as a byte
	 * 
	 * @return this value as a byte
	 */
	public byte getByteValue();
	
	/**
	 * Returns this value as a char
	 * 
	 * @return this value as a char
	 */
	public char getCharValue();
	
	/**
	 * Returns this value as a double
	 * 
	 * @return this value as a double
	 */
	public double getDoubleValue();
	
	/**
	 * Returns this value as a float
	 * 
	 * @return this value as a float
	 */
	public float getFloatValue();
	
	/**
	 * Returns this value as an int
	 * 
	 * @return this value as an int
	 */
	public int getIntValue();
	
	/**
	 * Returns this value as a long
	 * 
	 * @return this value as a long
	 */
	public long getLongValue();
	
	/**
	 * Returns this value as a short
	 * 
	 * @return this value as a short
	 */
	public short getShortValue();
}
