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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * Encapsulates a name and a value.  Used when a variable is required (such as for
 * the VariablesView), but we only have a value available (such as the result of 
 * an evaluation for a custom content provider).
 * 
 * @since 3.0
 */
public class JDIPlaceholderVariable implements IJavaVariable {

	private String fName;
	private IJavaValue fValue;

	public JDIPlaceholderVariable(String name, IJavaValue value) {
		setName(name);
		setValue(value);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#getSignature()
	 */
	public String getSignature() throws DebugException {
		return ((IJavaValue)getValue()).getSignature();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#getJavaType()
	 */
	public IJavaType getJavaType() throws DebugException {
		return ((IJavaValue)getValue()).getJavaType();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#isLocal()
	 */
	public boolean isLocal() throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() {
		return fValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getName()
	 */
	public String getName() throws DebugException {
		return fName;
	}
	
	private void setName(String name) {
		fName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return ((IJavaValue)getValue()).getReferenceTypeName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPublic()
	 */
	public boolean isPublic() throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPrivate()
	 */
	public boolean isPrivate() throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isProtected()
	 */
	public boolean isProtected() throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPackagePrivate()
	 */
	public boolean isPackagePrivate() throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isFinal()
	 */
	public boolean isFinal() throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isStatic()
	 */
	public boolean isStatic() throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isSynthetic()
	 */
	public boolean isSynthetic() throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return getValue().getModelIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return ((IJavaValue)getValue()).getDebugTarget();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return getValue().getLaunch();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(java.lang.String)
	 */
	public void setValue(String expression) throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(org.eclipse.debug.core.model.IValue)
	 */
	public void setValue(IValue value) {
		fValue = (IJavaValue) value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(java.lang.String)
	 */
	public boolean verifyValue(String expression) throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(org.eclipse.debug.core.model.IValue)
	 */
	public boolean verifyValue(IValue value) throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IJavaVariable.class.equals(adapter) ||
			IJavaModifiers.class.equals(adapter)) {
			return this;
		}
		return null;
	}

}
