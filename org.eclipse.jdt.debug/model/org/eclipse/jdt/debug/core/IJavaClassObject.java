package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * An object referencing an instance of <code>java.lang.Class</code> on a
 * target VM.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @see IJavaValue
 * @since 2.0
 */

public interface IJavaClassObject extends IJavaObject {
	
	/**
	 * Returns the type associated with instances of this
	 * class.
	 * 
	 * @return the type associated with instances of this
	 * 	class
	 */ 
	IJavaType getInstanceType();

}

