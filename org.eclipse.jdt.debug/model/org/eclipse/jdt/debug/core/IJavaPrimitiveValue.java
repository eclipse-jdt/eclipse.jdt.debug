package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * A primitive value on a Java debug target.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @since 2.0
 */
public interface IJavaPrimitiveValue extends IJavaValue {
	
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
