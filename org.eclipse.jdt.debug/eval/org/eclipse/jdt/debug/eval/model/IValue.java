package org.eclipse.jdt.debug.eval.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * A value assigned to a field or variable on a virtual
 * machine.
 * <p>
 * Clients are intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public interface IValue {
	
	/**
	 * Returns the type of this vlaue.
	 * 
	 * @return the type of this value
	 * @exception
	 */
	IType getType() throws CoreException;
	
}


