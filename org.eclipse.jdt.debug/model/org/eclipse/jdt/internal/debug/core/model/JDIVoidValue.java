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

import java.util.Collections;
import java.util.List;

import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * Represents a value of "void"
 */
public class JDIVoidValue extends JDIValue {

	public JDIVoidValue(JDIDebugTarget target) {
		super(target, target.getVM() != null ? target.getVM().mirrorOfVoid()
				: null);
	}

	@Override
	protected List<IJavaVariable> getVariablesList() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * @see IValue#getReferenceTypeName()
	 */
	@Override
	public String getReferenceTypeName() {
		return "void"; //$NON-NLS-1$
	}

	/**
	 * @see IValue#getValueString()
	 */
	@Override
	public String getValueString() {
		return "null"; //$NON-NLS-1$
	}

	/**
	 * @see IJavaValue#getSignature()
	 */
	@Override
	public String getSignature() {
		return "V"; //$NON-NLS-1$
	}

	/**
	 * @see IJavaValue#getArrayLength()
	 */
	@Override
	public int getArrayLength() {
		return -1;
	}

	/**
	 * @see IJavaValue#getJavaType()
	 */
	@Override
	public IJavaType getJavaType() {
		return new JDIVoidType((JDIDebugTarget) getDebugTarget());
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "void"; //$NON-NLS-1$
	}
}
