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
package org.eclipse.jdt.internal.debug.core.model;

 
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.debug.core.IJavaType;

/**
 * Represents a value of "null"
 */
public class JDINullValue extends JDIValue {
	
	
	public JDINullValue(JDIDebugTarget target) {
		super(target, null);
	}

	protected List getVariablesList() {
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * @see IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() {
		return "null"; //$NON-NLS-1$
	}
	
	/**
	 * @see IValue#getValueString()
	 */
	public String getValueString() {
		return "null"; //$NON-NLS-1$
	}

	/**
	 * @see IJavaValue#getSignature()
	 */
	public String getSignature() {
		return null;
	}

	/**
	 * @see IJavaValue#getArrayLength()
	 */
	public int getArrayLength() {
		return -1;
	}
		
	/**
	 * @see IJavaValue#getJavaType()
	 */
	public IJavaType getJavaType() {
		return null;
	}
	
	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		return obj instanceof JDINullValue;
	}

	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return getClass().hashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "null"; //$NON-NLS-1$
	}

}
