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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * A proxy to an object representing the logical structure of that object.
 */
public class LogicalObjectStructureValue implements IJavaObject {

	private final IJavaObject fObject;
	private final JDIPlaceholderVariable[] fVariables;

	/**
	 * Constructs a proxy to the given object, with the given variables as
	 * children.
	 *
	 * @param object
	 *            original object
	 * @param variables
	 *            java variables to add as children to this object
	 */
	public LogicalObjectStructureValue(IJavaObject object,
			JDIPlaceholderVariable[] variables) {
		fObject = object;
		fVariables = variables;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaObject#sendMessage(java.lang.String,
	 * java.lang.String, org.eclipse.jdt.debug.core.IJavaValue[],
	 * org.eclipse.jdt.debug.core.IJavaThread, boolean)
	 */
	@Override
	public IJavaValue sendMessage(String selector, String signature,
			IJavaValue[] args, IJavaThread thread, boolean superSend)
			throws DebugException {
		return fObject
				.sendMessage(selector, signature, args, thread, superSend);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaObject#sendMessage(java.lang.String,
	 * java.lang.String, org.eclipse.jdt.debug.core.IJavaValue[],
	 * org.eclipse.jdt.debug.core.IJavaThread, java.lang.String)
	 */
	@Override
	public IJavaValue sendMessage(String selector, String signature,
			IJavaValue[] args, IJavaThread thread, String typeSignature)
			throws DebugException {
		return fObject.sendMessage(selector, signature, args, thread,
				typeSignature);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getField(java.lang.String,
	 * boolean)
	 */
	@Override
	public IJavaFieldVariable getField(String name, boolean superField)
			throws DebugException {
		return fObject.getField(name, superField);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getField(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public IJavaFieldVariable getField(String name, String typeSignature)
			throws DebugException {
		return fObject.getField(name, typeSignature);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaValue#getSignature()
	 */
	@Override
	public String getSignature() throws DebugException {
		return fObject.getSignature();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaValue#getGenericSignature()
	 */
	@Override
	public String getGenericSignature() throws DebugException {
		return fObject.getGenericSignature();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaValue#getJavaType()
	 */
	@Override
	public IJavaType getJavaType() throws DebugException {
		return fObject.getJavaType();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	@Override
	public String getReferenceTypeName() throws DebugException {
		return fObject.getReferenceTypeName();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	@Override
	public String getValueString() throws DebugException {
		return fObject.getValueString();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	@Override
	public boolean isAllocated() throws DebugException {
		return fObject.isAllocated();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	@Override
	public IVariable[] getVariables() {
		return fVariables;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	@Override
	public boolean hasVariables() {
		return fVariables.length > 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	@Override
	public String getModelIdentifier() {
		return fObject.getModelIdentifier();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	@Override
	public IDebugTarget getDebugTarget() {
		return fObject.getDebugTarget();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	@Override
	public ILaunch getLaunch() {
		return fObject.getLaunch();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return fObject.getAdapter(adapter);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getWaitingThreads()
	 */
	@Override
	public IJavaThread[] getWaitingThreads() throws DebugException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getOwningThread()
	 */
	@Override
	public IJavaThread getOwningThread() throws DebugException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getReferringObjects(long)
	 */
	@Override
	public IJavaObject[] getReferringObjects(long max) throws DebugException {
		return fObject.getReferringObjects(max);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaObject#disableCollection()
	 */
	@Override
	public void disableCollection() throws DebugException {
		fObject.disableCollection();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaObject#enableCollection()
	 */
	@Override
	public void enableCollection() throws DebugException {
		fObject.enableCollection();

		for (JDIPlaceholderVariable variable : fVariables) {
			IValue variableValue = variable.getValue();
			if (variableValue instanceof IJavaObject) {
				((IJavaObject) variableValue).enableCollection();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getUniqueId()
	 */
	@Override
	public long getUniqueId() throws DebugException {
		return fObject.getUniqueId();
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

	@Override
	public String getLabel() throws DebugException {
		return fObject.getLabel();
	}

	@Override
	public void setLabel(String newLabel) throws DebugException {
		fObject.setLabel(newLabel);
	}
}
