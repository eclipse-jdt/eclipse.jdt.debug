package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.VMDisconnectedException;

/**
 * The value for an array partition.
 */
public class JDIArrayPartitionValue extends JDIDebugElement implements IJavaValue {
	
	protected JDIArrayPartition fPartition;
	
	public JDIArrayPartitionValue(JDIArrayPartition partition) {
		super(null);
		fPartition = partition;
	}
	
	public int getElementType() {
		return VALUE;
	}

	public String getName() {
		return null;
	}
	
	public String getReferenceTypeName() {
		return "";
	}
	
	public String getValueString(boolean qualified) {
		return "";
	}
	
	public String getValueString() {
		return "";
	}
	
	public String evaluateToString(IJavaThread thread) {
		return getValueString();
	}
		
	public boolean hasChildren() {
		return true;
	}
	
	/**
	 * @see IValue
	 */
	public IVariable[] getVariables() throws DebugException {
		List list = getVariables0();
		return (IVariable[])list.toArray(new IVariable[list.size()]);
	}
	
	protected List getVariables0() {
		return JDIArrayPartition.splitArray((JDIDebugTarget)fPartition.getDebugTarget(), fPartition.getArrayReference(), fPartition.getStart(), fPartition.getEnd());
	}

	public ArrayReference getArrayReference() {
		return fPartition.getArrayReference();
	}
	
	/**
	 * @see IDebugElement
	 */
	public IDebugTarget getDebugTarget() {
		return fPartition.getDebugTarget();
	}

	/**
	 * Returns true if this value is allocated, otherwise false.
	 */
	public boolean isAllocated() throws DebugException {
		try {
			return !getArrayReference().isCollected();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(JDIValue.ERROR_IS_ALLOCATED, e);
		}
		return false;
	}
	
	/**
	 * @see IJavaValue
	 */
	public int getArrayLength() {
		return -1;
	}

	/**
	 * @see IJavaValue
	 */
	public String getSignature() {
		return null;
	}

}
