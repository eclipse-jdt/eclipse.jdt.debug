/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaReferenceType;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;

/**
 * References a class, interface, or array type.
 */
public abstract class JDIReferenceType extends JDIType implements IJavaReferenceType {

	/**
	 * Constructs a new reference type in the given target.
	 * 
	 * @param target associated vm
	 * @param type reference type
	 */
	public JDIReferenceType(JDIDebugTarget target, Type type) {
		super(target, type);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getAvailableStrata()
	 */
	public String[] getAvailableStrata() throws DebugException {
		List strata = getReferenceType().availableStrata();
		return (String[])strata.toArray(new String[strata.size()]);
	}

	/**
	 * Returns the underlying reference type.
	 * 
	 * @return the underlying reference type
	 */
	protected ReferenceType getReferenceType() {
		return (ReferenceType)getUnderlyingType();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getDefaultStratum()
	 */
	public String getDefaultStratum() throws DebugException {
		return getReferenceType().defaultStratum();
	}

}
