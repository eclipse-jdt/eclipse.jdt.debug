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


import java.text.MessageFormat;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;

import com.sun.jdi.ArrayReference;

/**
 * The value for an array partition.
 */
public class JDIArrayPartitionValue extends JDIDebugElement implements IJavaValue {
	
	private JDIArrayPartition fPartition;
	
	public JDIArrayPartitionValue(JDIArrayPartition partition) {
		super(null);
		fPartition = partition;
	}
	
	public String getReferenceTypeName() {
		return ""; //$NON-NLS-1$
	}
	
	public String getValueString() {
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * @see IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		List list = getVariables0();
		return (IVariable[])list.toArray(new IVariable[list.size()]);
	}
	
	protected List getVariables0() {
		return JDIArrayPartition.splitArray((JDIDebugTarget)getPartition().getDebugTarget(), getPartition().getArrayReference(), getPartition().getStart(), getPartition().getEnd());
	}

	public ArrayReference getArrayReference() {
		return getPartition().getArrayReference();
	}
	
	/**
	 * @see JDIDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return getPartition().getDebugTarget();
	}

	/**
	 * Returns true if this value is allocated, otherwise false.
	 */
	public boolean isAllocated() throws DebugException {
		try {
			return !getArrayReference().isCollected();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayPartitionValue.exception_is_garbage_collected"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return false;
		}
		
	}
	
	/**
	 * @see IJavaValue#getSignature()
	 */
	public String getSignature() {
		return ""; //$NON-NLS-1$
	}
	
	protected JDIArrayPartition getPartition() {
		return fPartition;
	}
	/**
	 * @see IJavaValue#getJavaType()
	 */
	public IJavaType getJavaType() throws DebugException {
			return null;
	}

	/**
	 * @see IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return getVariables0().size() > 0;
	}

}
