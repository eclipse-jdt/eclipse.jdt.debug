/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * Encapsulates a name and a value. Used when a variable is required (such as
 * for the VariablesView), but we only have a value available (such as the
 * result of an evaluation for an object browser).
 *
 * @since 3.0
 */
public class JDIPlaceholderVariable extends PlatformObject implements
		IJavaVariable {

	private String fName;
	private IJavaValue fValue;

	/**
	 * When created for a logical structure we hold onto the original
	 * non-logical value for purposes of equality. This way a logical
	 * structure's children remain more stable in the variables view.
	 *
	 * This is <code>null</code> when not created for a logical structure.
	 */
	private IJavaValue fLogicalParent;

	public JDIPlaceholderVariable(String name, IJavaValue value) {
		fName = name;
		fValue = value;
	}

	/**
	 * Constructs a place holder with the given name and value originating from
	 * the specified logical parent.
	 *
	 * @param name
	 *            variable name
	 * @param value
	 *            variable value
	 * @param logicalParent
	 *            original value this value is a logical child for or
	 *            <code>null</code> if none
	 */
	public JDIPlaceholderVariable(String name, IJavaValue value,
			IJavaValue logicalParent) {
		this(name, value);
		fLogicalParent = logicalParent;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#getSignature()
	 */
	@Override
	public String getSignature() throws DebugException {
		return ((IJavaValue) getValue()).getSignature();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#getGenericSignature()
	 */
	@Override
	public String getGenericSignature() throws DebugException {
		return ((IJavaValue) getValue()).getGenericSignature();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#getJavaType()
	 */
	@Override
	public IJavaType getJavaType() throws DebugException {
		return ((IJavaValue) getValue()).getJavaType();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#isLocal()
	 */
	@Override
	public boolean isLocal() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	@Override
	public IValue getValue() {
		return fValue;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IVariable#getName()
	 */
	@Override
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	@Override
	public String getReferenceTypeName() throws DebugException {
		return ((IJavaValue) getValue()).getReferenceTypeName();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	@Override
	public boolean hasValueChanged() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPublic()
	 */
	@Override
	public boolean isPublic() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPrivate()
	 */
	@Override
	public boolean isPrivate() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isProtected()
	 */
	@Override
	public boolean isProtected() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPackagePrivate()
	 */
	@Override
	public boolean isPackagePrivate() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isFinal()
	 */
	@Override
	public boolean isFinal() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isStatic()
	 */
	@Override
	public boolean isStatic() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isSynthetic()
	 */
	@Override
	public boolean isSynthetic() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	@Override
	public String getModelIdentifier() {
		return getValue().getModelIdentifier();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	@Override
	public IDebugTarget getDebugTarget() {
		return ((IJavaValue) getValue()).getDebugTarget();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	@Override
	public ILaunch getLaunch() {
		return getValue().getLaunch();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.debug.core.model.IValueModification#setValue(java.lang.String
	 * )
	 */
	@Override
	public void setValue(String expression) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.debug.core.model.IValueModification#setValue(org.eclipse.
	 * debug.core.model.IValue)
	 */
	@Override
	public void setValue(IValue value) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.debug.core.model.IValueModification#supportsValueModification
	 * ()
	 */
	@Override
	public boolean supportsValueModification() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.debug.core.model.IValueModification#verifyValue(java.lang
	 * .String)
	 */
	@Override
	public boolean verifyValue(String expression) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.debug.core.model.IValueModification#verifyValue(org.eclipse
	 * .debug.core.model.IValue)
	 */
	@Override
	public boolean verifyValue(IValue value) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (IJavaVariable.class.equals(adapter)
				|| IJavaModifiers.class.equals(adapter)) {
			return (T) this;
		}
		return super.getAdapter(adapter);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JDIPlaceholderVariable) {
			JDIPlaceholderVariable var = (JDIPlaceholderVariable) obj;
			if (fLogicalParent != null) {
				return var.getName().equals(getName())
						&& fLogicalParent.equals(var.fLogicalParent);
			}
			return var.getName().equals(getName())
					&& var.getValue().equals(getValue());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (fLogicalParent != null) {
			return fLogicalParent.hashCode() + fName.hashCode();
		}
		return fName.hashCode() + fValue.hashCode();
	}

}
