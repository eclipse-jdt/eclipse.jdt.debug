/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;

import com.ibm.icu.text.MessageFormat;

/**
 * Represents a value of "null"
 */
public class JDINullValue extends JDIObjectValue {
	
	
	/**
	 * Constructor
	 * @param target
	 */
	public JDINullValue(JDIDebugTarget target) {
		super(target, null);
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#getVariablesList()
	 */
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

	/**
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIObjectValue#getField(java.lang.String, boolean)
	 */
	public IJavaFieldVariable getField(String name, boolean superField) {
		return null;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIObjectValue#getField(java.lang.String, java.lang.String)
	 */
	public IJavaFieldVariable getField(String name, String typeSignature) {
		return null;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIObjectValue#getWaitingThreads()
	 */
	public IJavaThread[] getWaitingThreads() {
		return null;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIObjectValue#getOwningThread()
	 */
	public IJavaThread getOwningThread() {
		return null;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIObjectValue#getReferringObjects(long)
	 */
	public IJavaObject[] getReferringObjects(long max)  {
		return new IJavaObject[0];
	}
	
	/**
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIObjectValue#sendMessage(java.lang.String, java.lang.String, org.eclipse.jdt.debug.core.IJavaValue[], org.eclipse.jdt.debug.core.IJavaThread, boolean)
	 */
	public IJavaValue sendMessage(String selector, String signature, IJavaValue[] args, IJavaThread thread, boolean superSend) throws DebugException{
		return npe(selector, signature);
	}
	
	/**
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIObjectValue#sendMessage(java.lang.String, java.lang.String, org.eclipse.jdt.debug.core.IJavaValue[], org.eclipse.jdt.debug.core.IJavaThread, java.lang.String)
	 */
	public IJavaValue sendMessage(String selector, String signature, IJavaValue[] args, IJavaThread thread, String typeSignature) throws DebugException{
		return npe(selector, signature);
	}
	
	/**
	 * Creates an artificial NPE for display to the user as an error message
	 * @param selector
	 * @param signature
	 * @return
	 * @throws DebugException
	 */
	private IJavaValue npe(String selector, String signature) throws DebugException {
		StringBuffer buffer = new StringBuffer();
		buffer.append(selector);
		String[] parameterTypes = Signature.getParameterTypes(signature);
		buffer.append('(');
		for (int i = 0; i < parameterTypes.length; i++) {
			buffer.append(Signature.getSignatureSimpleName(parameterTypes[i].replace('/', '.')));
			if (i+1 < parameterTypes.length) {
				buffer.append(", "); //$NON-NLS-1$
			}
		}			
		buffer.append(')');
		requestFailed(MessageFormat.format(JDIDebugModelMessages.JDINullValue_0, new String[]{buffer.toString()}), null);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIObjectValue#disableCollection()
	 */
	public void disableCollection() throws DebugException {
		// Do nothing for null values
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIObjectValue#enableCollection()
	 */
	public void enableCollection() throws DebugException {
		// Do nothing for null values
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#isNull()
	 */
	public boolean isNull() {
		return true;
	}
	
}
