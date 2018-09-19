/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.logicalstructures;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.JDIDebugModel;

/**
 *
 */
public class JavaStructureErrorValue implements IJavaValue {

	private String[] fMessages;
	private IJavaValue fValue;

	public JavaStructureErrorValue(String errorMessage, IJavaValue value) {
		fMessages = new String[] { errorMessage };
		fValue = value;
	}

	public JavaStructureErrorValue(String[] errorMessages, IJavaValue value) {
		fMessages = errorMessages;
		fValue = value;
	}

	/**
	 * Returns this error node's parent value. This is the value for which a
	 * logical structure could not be calculated.
	 *
	 * @return the parent value of this error node
	 */
	public IJavaValue getParentValue() {
		return fValue;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaValue#getSignature()
	 */
	@Override
	public String getSignature() throws DebugException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaValue#getGenericSignature()
	 */
	@Override
	public String getGenericSignature() throws DebugException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaValue#getJavaType()
	 */
	@Override
	public IJavaType getJavaType() throws DebugException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	@Override
	public String getReferenceTypeName() throws DebugException {
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	@Override
	public String getValueString() throws DebugException {
		return fMessages[0];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	@Override
	public boolean isAllocated() throws DebugException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	@Override
	public IVariable[] getVariables() throws DebugException {
		IVariable[] variables = new IVariable[fMessages.length];
		for (int i = 0; i < variables.length; i++) {
			StringBuilder varName = new StringBuilder();
			if (variables.length > 1) {
				varName.append(
						LogicalStructuresMessages.JavaStructureErrorValue_0)
						.append('[').append(i).append(']');
			} else {
				varName.append(LogicalStructuresMessages.JavaStructureErrorValue_1);
			}
			variables[i] = new JDIPlaceholderVariable(varName.toString(),
					new JavaStructureErrorValue(fMessages[i], fValue));
		}
		return variables;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	@Override
	public boolean hasVariables() throws DebugException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	@Override
	public String getModelIdentifier() {
		return JDIDebugModel.getPluginIdentifier();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	@Override
	public IDebugTarget getDebugTarget() {
		return fValue.getDebugTarget();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	@Override
	public ILaunch getLaunch() {
		return getDebugTarget().getLaunch();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaValue#isNull()
	 */
	@Override
	public boolean isNull() {
		return false;
	}
}
