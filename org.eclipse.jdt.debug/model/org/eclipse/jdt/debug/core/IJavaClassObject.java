/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.core;

 
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

