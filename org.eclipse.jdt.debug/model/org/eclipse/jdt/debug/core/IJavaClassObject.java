package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * A Java class object is a Java object that references
 * an instance of <code>java.lang.Class</code> on the
 * target VM.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IJavaValue
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

