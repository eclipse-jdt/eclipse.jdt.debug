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

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IValueModification;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

public abstract class JDIVariable extends JDIDebugElement implements
		IJavaVariable {

	/**
	 * Cache of current value - see #getValue().
	 */
	private JDIValue fValue;

	/**
	 * Counter corresponding to this variable's debug target suspend count
	 * indicating the last time this value changed. This variable's value has
	 * changed on the last suspend event if this counter is equal to the debug
	 * target's suspend count.
	 */
	private int fLastChangeIndex = -1;

	protected final static String jdiStringSignature = "Ljava/lang/String;"; //$NON-NLS-1$

	public JDIVariable(JDIDebugTarget target) {
		super(target);
	}

	/**
	 * @see PlatformObject#getAdapter(Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IJavaVariable.class || adapter == IJavaModifiers.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Returns this variable's current underlying jdi value. Subclasses must
	 * implement #retrieveValue() and do not need to guard against JDI
	 * exceptions, as this method handles them.
	 * 
	 * @exception DebugException
	 *                if unable to access the value
	 */
	protected final Value getCurrentValue() throws DebugException {
		try {
			return retrieveValue();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(
					JDIDebugModelMessages.JDIVariable_exception_retrieving,
					new Object[] { e.toString() }), e);
			// execution will not reach this line, as
			// #targetRequestFailed will throw an exception
			return null;
		}
	}

	/**
	 * Returns this variable's underlying jdi value
	 */
	protected abstract Value retrieveValue() throws DebugException;

	/**
	 * Returns the current value of this variable. The value is cached, but on
	 * each access we see if the value has changed and update if required.
	 * 
	 * @see IVariable#getValue()
	 */
	public IValue getValue() throws DebugException {
		Value currentValue = getCurrentValue();
		if (fValue == null) {
			fValue = JDIValue.createValue((JDIDebugTarget) getDebugTarget(),
					currentValue);
		} else {
			Value previousValue = fValue.getUnderlyingValue();
			if (currentValue == previousValue) {
				return fValue;
			}
			if (previousValue == null || currentValue == null) {
				fValue = JDIValue.createValue(
						(JDIDebugTarget) getDebugTarget(), currentValue);
				setChangeCount(getJavaDebugTarget().getSuspendCount());
			} else if (!previousValue.equals(currentValue)) {
				fValue = JDIValue.createValue(
						(JDIDebugTarget) getDebugTarget(), currentValue);
				setChangeCount(getJavaDebugTarget().getSuspendCount());
			}
		}
		return fValue;
	}

	/**
	 * @see IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		return false;
	}

	/**
	 * @see IValueModification#setValue(String)
	 */
	public void setValue(String expression) throws DebugException {
		notSupported(JDIDebugModelMessages.JDIVariable_does_not_support_value_modification);
	}

	/**
	 * @see IValueModification#setValue(IValue)
	 */
	public void setValue(IValue value) throws DebugException {
		notSupported(JDIDebugModelMessages.JDIVariable_does_not_support_value_modification);
	}

	/**
	 * @see IValueModification#verifyValue(String)
	 */
	public boolean verifyValue(String expression) throws DebugException {
		return false;
	}

	/**
	 * @see IValueModification#verifyValue(IValue)
	 */
	public boolean verifyValue(IValue value) throws DebugException {
		return false;
	}

	/**
	 * @see IJavaModifiers#isSynthetic()
	 */
	public boolean isSynthetic() {
		return false;
	}

	/**
	 * @see IJavaModifiers#isPublic()
	 */
	public boolean isPublic() throws DebugException {
		return false;
	}

	/**
	 * @see IJavaModifiers#isPrivate()
	 */
	public boolean isPrivate() throws DebugException {
		return false;
	}

	/**
	 * @see IJavaModifiers#isProtected()
	 */
	public boolean isProtected() throws DebugException {
		return false;
	}

	/**
	 * @see IJavaModifiers#isPackagePrivate()
	 */
	public boolean isPackagePrivate() {
		return false;
	}

	/**
	 * @see IJavaModifiers#isStatic()
	 */
	public boolean isStatic() {
		return false;
	}

	/**
	 * @see IJavaModifiers#isFinal()
	 */
	public boolean isFinal() {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#isLocal()
	 */
	public boolean isLocal() {
		return false;
	}

	/**
	 * @see IJavaVariable#getJavaType()
	 */
	public IJavaType getJavaType() throws DebugException {
		return JDIType.createType((JDIDebugTarget) getDebugTarget(),
				getUnderlyingType());
	}

	/**
	 * Returns the underlying type of this variable
	 * 
	 * @return the underlying type of this variable
	 * 
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                <li>The type associated with this variable is not yet
	 *                loaded</li>
	 *                </ul>
	 */
	protected abstract Type getUnderlyingType() throws DebugException;

	/**
	 * Returns the last known value for this variable
	 */
	protected Value getLastKnownValue() {
		if (fValue == null) {
			return null;
		}
		return fValue.getUnderlyingValue();
	}

	/**
	 * Sets this variable's change counter to the specified value
	 * 
	 * @param count
	 *            new value
	 */
	protected void setChangeCount(int count) {
		fLastChangeIndex = count;
	}

	/**
	 * Returns this variable's change counter. This corresponds to the last time
	 * this variable changed.
	 * 
	 * @return this variable's change counter
	 */
	protected int getChangeCount() {
		return fLastChangeIndex;
	}

	/**
	 * @see IVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() {
		return getChangeCount() == getJavaDebugTarget().getSuspendCount();
	}
}
