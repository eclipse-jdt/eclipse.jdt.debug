/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.debug.core;

import org.eclipse.debug.core.DebugException;

public interface IJavaFieldVariable extends IJavaVariable {

	/**
	 * Returns the declaring type of this field.
	 */
	public IJavaType getDeclaringType();

}
