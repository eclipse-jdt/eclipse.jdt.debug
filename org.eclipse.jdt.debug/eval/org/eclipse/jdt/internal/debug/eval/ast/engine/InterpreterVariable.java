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

	public InterpreterVariable(String name, IJavaType referenceType, IDebugTarget debugTarget) {
		fName= name;
		fReferenceType= referenceType;
		fDebugTarget= debugTarget;
	}

	/**
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() throws DebugException {
		return fValue;
	}

	/**
	 * @see org.eclipse.debug.core.model.IVariable#getName()
	 */
	public String getName() throws DebugException {
		return fName;
	}

	/**
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return fReferenceType.getName();
	}

	/**
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() throws DebugException {
		return false;
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return JDIDebugModel.getPluginIdentifier();
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fDebugTarget.getLaunch();
	}

	/**
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(String)
	 */
	public void setValue(String expression) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR,  JDIDebugModel.getPluginIdentifier(), DebugException.NOT_SUPPORTED, EvaluationEngineMessages.getString("InterpreterVariable.setValue(String)_not_supported_for_interpreter_variable_1"), null)); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(IValue)
	 */
	public void setValue(IValue value) throws DebugException {
		fValue= value;
	}

	/**
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		return false;
	}

	/**
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(IValue)
	 */
	public boolean verifyValue(IValue value) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR,  JDIDebugModel.getPluginIdentifier(), DebugException.NOT_SUPPORTED, EvaluationEngineMessages.getString("InterpreterVariable.verifyValue(IValue)_not_supported_for_interpreter_variable_2"), null)); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(String)
	 */
	public boolean verifyValue(String expression) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR,  JDIDebugModel.getPluginIdentifier(), DebugException.NOT_SUPPORTED, EvaluationEngineMessages.getString("InterpreterVariable.verifyValue(String)_not_supported_for_interpreter_variable_3"), null)); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#getJavaType()
	 */
	public IJavaType getJavaType() throws DebugException {
		return fReferenceType;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#getSignature()
	 */
	public String getSignature() throws DebugException {
		return fReferenceType.getSignature();
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isFinal()
	 */
	public boolean isFinal() throws DebugException {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPackagePrivate()
	 */
	public boolean isPackagePrivate() throws DebugException {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPrivate()
	 */
	public boolean isPrivate() throws DebugException {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isProtected()
	 */
	public boolean isProtected() throws DebugException {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPublic()
	 */
	public boolean isPublic() throws DebugException {
		return true;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isStatic()
	 */
	public boolean isStatic() throws DebugException {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isSynthetic()
	 */
	public boolean isSynthetic() throws DebugException {
		return true;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#isLocal()
	 */
	public boolean isLocal() throws DebugException {
		return false;
	}
}
