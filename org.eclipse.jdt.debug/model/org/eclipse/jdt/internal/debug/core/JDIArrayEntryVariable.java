package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.debug.core.model.IDebugElement;

/**
 * An entry in an array.
 */

public class JDIArrayEntryVariable extends JDIModificationVariable {
		
	/**
	 * The index of the variable entry
	 */
	protected int fIndex;
	
	/**
	 * Constructs an array entry at the given index in an array.
	 */
	public JDIArrayEntryVariable(JDIDebugElement parent, int index) {
		super(parent);
		fIndex= index;
	}

	/**
	 * Returns this variable's current underlying value.
	 */
	protected Value retrieveValue() {
		ArrayReference ar= getArrayReference();
		if (ar != null) {
			return ar.getValue(fIndex);
		}
		return null;
	}

	/**
	 * @see IDebugElement
	 */
	public String getName() {
		return "[" + fIndex + "]";
	}

	public void setValue(Value value) throws DebugException {
		ArrayReference ar= getArrayReference();
		if (ar == null) {
			requestFailed(ERROR_SET_VALUE, null);
		}
		try {
			ar.setValue(fIndex, value);
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(ERROR_SET_VALUE, e);
		} catch (InvalidTypeException e) {
			targetRequestFailed(ERROR_SET_VALUE, e);
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_SET_VALUE, e);
		}

	}

	protected ArrayReference getArrayReference() {
		IDebugElement parent = getParent();
		if (parent instanceof JDIValue) {
			return ((JDIValue)parent).getArrayReference();
		} else {
			return ((JDIArrayPartitionValue)parent).getArrayReference();
		}
	}
	
	/**
	 * @see IVariable
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			return stripBrackets(getArrayReference().referenceType().name());
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_GET_REFERENCE_TYPE, e);
		}
		return getUnknownMessage();
	}
	
	/**
	 * Given a type name, strip out one set of array brackets and
	 * return the result.  Example:  "int[][][]" becomes "int[][]".
	 */
	protected String stripBrackets(String typeName) {
		int lastLeft= typeName.lastIndexOf("[]");
		if (lastLeft < 0) {
			return typeName;
		}
		StringBuffer buffer= new StringBuffer(typeName);
		buffer.replace(lastLeft, lastLeft + 2, "");
		return buffer.toString();
	}
	
	/**
	 * @see IJavaVariable
	 */
	public String getSignature() throws DebugException {
		try {
			return getArrayReference().type().signature();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_GET_SIGNATURE, e);
		}
		return getUnknownMessage();
	}
	
	protected VirtualMachine getVirtualMachine() {
		return getArrayReference().virtualMachine();
	}
}

