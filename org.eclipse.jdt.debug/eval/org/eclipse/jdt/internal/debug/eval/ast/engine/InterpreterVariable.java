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
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.JDIDebugModel;

public class InterpreterVariable implements IJavaVariable {

	/**
	 * The reference type of this variable.
	 */
	private IJavaType fReferenceType;

	/**
	 * The variable name.
	 */
	private String fName;

	/**
	 * The variable value.
	 */
	private IValue fValue;

	private IDebugTarget fDebugTarget;

	public InterpreterVariable(String name, IJavaType referenceType,
			IDebugTarget debugTarget) {
		fName = name;
		fReferenceType = referenceType;
		fDebugTarget = debugTarget;
	}

	/**
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	@Override
	public IValue getValue() {
		return fValue;
	}

	/**
	 * @see org.eclipse.debug.core.model.IVariable#getName()
	 */
	@Override
	public String getName() {
		return fName;
	}

	/**
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	@Override
	public String getReferenceTypeName() throws DebugException {
		return fReferenceType.getName();
	}

	/**
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	@Override
	public boolean hasValueChanged() {
		return false;
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	@Override
	public String getModelIdentifier() {
		return JDIDebugModel.getPluginIdentifier();
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	@Override
	public IDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	@Override
	public ILaunch getLaunch() {
		return fDebugTarget.getLaunch();
	}

	/**
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(String)
	 */
	@Override
	public void setValue(String expression) throws DebugException {
		throw new DebugException(
				new Status(
						IStatus.ERROR,
						JDIDebugModel.getPluginIdentifier(),
						DebugException.NOT_SUPPORTED,
						EvaluationEngineMessages.InterpreterVariable_setValue_String__not_supported_for_interpreter_variable_1,
						null));
	}

	/**
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(IValue)
	 */
	@Override
	public void setValue(IValue value) {
		fValue = value;
	}

	/**
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	@Override
	public boolean supportsValueModification() {
		return false;
	}

	/**
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(IValue)
	 */
	@Override
	public boolean verifyValue(IValue value) throws DebugException {
		throw new DebugException(
				new Status(
						IStatus.ERROR,
						JDIDebugModel.getPluginIdentifier(),
						DebugException.NOT_SUPPORTED,
						EvaluationEngineMessages.InterpreterVariable_verifyValue_IValue__not_supported_for_interpreter_variable_2,
						null));
	}

	/**
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(String)
	 */
	@Override
	public boolean verifyValue(String expression) throws DebugException {
		throw new DebugException(
				new Status(
						IStatus.ERROR,
						JDIDebugModel.getPluginIdentifier(),
						DebugException.NOT_SUPPORTED,
						EvaluationEngineMessages.InterpreterVariable_verifyValue_String__not_supported_for_interpreter_variable_3,
						null));
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#getJavaType()
	 */
	@Override
	public IJavaType getJavaType() {
		return fReferenceType;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#getSignature()
	 */
	@Override
	public String getSignature() throws DebugException {
		return fReferenceType.getSignature();
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isFinal()
	 */
	@Override
	public boolean isFinal() {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPackagePrivate()
	 */
	@Override
	public boolean isPackagePrivate() {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPrivate()
	 */
	@Override
	public boolean isPrivate() {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isProtected()
	 */
	@Override
	public boolean isProtected() {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPublic()
	 */
	@Override
	public boolean isPublic() {
		return true;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isStatic()
	 */
	@Override
	public boolean isStatic() {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isSynthetic()
	 */
	@Override
	public boolean isSynthetic() {
		return true;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#isLocal()
	 */
	@Override
	public boolean isLocal() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#getGenericSignature()
	 */
	@Override
	public String getGenericSignature() throws DebugException {
		return getSignature();
	}
}
