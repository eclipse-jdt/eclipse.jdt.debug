package org.eclipse.jdt.debug.eval.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * The virtual machine on which an evaluation is being
 * performed.
 * <p>
 * Clients are intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */

public interface IVirtualMachine {

	/**
	 * Creates and returns a new boolean value that can be
	 * used for comparing and setting values on the target VM.
	 * 
	 * @param value boolean value to create
	 * @return value
	 */
	IValue newValue(boolean value);
	
	/**
	 * Creates and returns a new byte value that can be
	 * used for comparing and setting values on the target VM.
	 * 
	 * @param value byte value to create
	 * @return value
	 */
	IValue newValue(byte value);	

	/**
	 * Creates and returns a new char value that can be
	 * used for comparing and setting values on the target VM.
	 * 
	 * @param value char value to create
	 * @return value
	 */
	IValue newValue(char value);
	
	/**
	 * Creates and returns a new double value that can be
	 * used for comparing and setting values on the target VM.
	 * 
	 * @param value double value to create
	 * @return value
	 */
	IValue newValue(double value);
				
	/**
	 * Creates and returns a new float value that can be
	 * used for comparing and setting values on the target VM.
	 * 
	 * @param value float value to create
	 * @return value
	 */
	IValue newValue(float value);
					
	/**
	 * Creates and returns a new int value that can be
	 * used for comparing and setting values on the target VM.
	 * 
	 * @param value int value to create
	 * @return value
	 */
	IValue newValue(int value);
	
	/**
	 * Creates and returns a new long value that can be
	 * used for comparing and setting values on the target VM.
	 * 
	 * @param value long value to create
	 * @return value
	 */
	IValue newValue(long value);
	
	/**
	 * Creates and returns a new short value that can be
	 * used for comparing and setting values on the target VM.
	 * 
	 * @param value short value to create
	 * @return value
	 */
	IValue newValue(short value);
	
	/**
	 * Creates and returns a new String object that can be
	 * used for comparing and setting values on the target VM.
	 * 
	 * @param value String value to create
	 * @return value
	 */
	IObject newValue(String value);	

	/**
	 * Returns the loaded types (class or interfaces) on the target
	 * VM with the given fully qualified name.
	 * 
	 * @param name fully qualified type name, for example,
	 * 	<code>java.lang.String</code>
	 * @return type
	 */
	IType[]classesByName(String qualifiedName) throws CoreException;

	/**
	 * Returns the value representing a null value in the target VM.
	 * 
	 * @return null value
	 */
	IValue nullValue();
	
	/**
	 * Returns the value representing a void value in the target VM.
	 * 
	 * @return void value
	 */
	IValue voidValue();
}

