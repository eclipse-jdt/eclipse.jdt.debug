package org.eclipse.jdt.internal.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import com.sun.jdi.ArrayReference;
import com.sun.jdi.VMDisconnectedException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.*;
import org.eclipse.jdt.debug.core.IJavaValue;
import java.util.List;

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

	public IVariable getVariable() {
		return fPartition;
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
	
	public String evaluateToString() {
		return getValueString();
	}
		
	public boolean hasChildren() {
		return true;
	}
	
	protected List getChildren0() {
		return JDIArrayPartition.splitArray(this, fPartition.getStart(), fPartition.getEnd());
	}

	public ArrayReference getArrayReference() {
		return fPartition.getArrayReference();
	}
	
	/**
	 * @see IDebugElement
	 */
	public IDebugTarget getDebugTarget() {
		return getVariable().getDebugTarget();
	}
	
	/**
	 * Returns the stack frame this value originated from
	 */
	public IStackFrame getStackFrame() {
		return getVariable().getStackFrame();
	}
	
	/**
	 * Returns the thread this value originated from
	 */
	public IThread getThread() {
		return getVariable().getThread();
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
