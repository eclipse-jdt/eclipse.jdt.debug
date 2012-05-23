/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import org.eclipse.jdt.debug.core.IJavaType;

/**
 * Void type. Since it is not possible to retrieve the void type from the target
 * VM on demand, there is a special implementation for the void type.
 */

public class JDIVoidType extends JDIType {

	/**
	 * Constructs a new void type for the given VM.
	 */
	protected JDIVoidType(JDIDebugTarget target) {
		super(target, null);
	}

	/**
	 * @see IJavaType#getName()
	 */
	@Override
	public String getName() {
		return "void"; //$NON-NLS-1$
	}

	/**
	 * @see IJavaType#getSignature()
	 */
	@Override
	public String getSignature() {
		return "V"; //$NON-NLS-1$
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object object) {
		return object instanceof JDIVoidType
				&& getDebugTarget().equals(
						((JDIVoidType) object).getDebugTarget());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
