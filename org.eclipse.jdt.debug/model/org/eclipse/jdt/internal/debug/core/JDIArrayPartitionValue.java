package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaThread;
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
	
	public int getElementType() {
		return VALUE;
	}
	
	public String getReferenceTypeName() {
		return ""; //$NON-NLS-1$
	}
	
	public String getValueString(boolean qualified) {
		return ""; //$NON-NLS-1$
	}
	
	public String getValueString() {
		return ""; //$NON-NLS-1$
	}
	
	public String evaluateToString(IJavaThread thread) {
		return getValueString();
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
	 * @see IJavaValue#getArrayLength()
	 */
	public int getArrayLength() {
		return -1;
	}

	/**
	 * @see IJavaValue#getSignature()
	 */
	public String getSignature() {
		return null;
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
	 * @see IJavaValue#sendMessage(String, String, IJavaValue[], IJavaThread)
	 */
	public IJavaValue sendMessage(
		String selector,
		String signature,
		IJavaValue[] args,
		IJavaThread thread,
		boolean superSend)
		throws DebugException {
			requestFailed(JDIDebugModelMessages.getString("JDIArrayPartitionValue.Cannot_send_a_message_to_an_array"), null); //$NON-NLS-1$
			// execution will not fall through, as
			// #requestFailed will throw an exception
			return null;
	}

}
