package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
/**
 * A variable containing the value of a field.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @since 2.0
 */
public interface IJavaFieldVariable extends IJavaVariable {

	/**
	 * Returns the declaring type of this field.
	 * 
	 * @return the declaring type of this field
	 */
	public IJavaType getDeclaringType();

}
