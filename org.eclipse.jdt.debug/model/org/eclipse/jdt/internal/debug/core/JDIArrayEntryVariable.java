package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;

import com.sun.jdi.*;

/**
 * An entry in an array.
 */

public class JDIArrayEntryVariable extends JDIModificationVariable {
		
	/**
	 * The index of the variable entry
	 */
	protected int fIndex;
	
	/**
	 * The array object
	 */
	protected ArrayReference fArray;
	
	/**
	 * Constructs an array entry at the given index in an array.
	 */
	public JDIArrayEntryVariable(JDIDebugTarget target, ArrayReference array, int index) {
		super(target);
		fArray= array;
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
		return "[" + fIndex + "]"; //$NON-NLS-2$ //$NON-NLS-1$
	}

	public void setValue(Value value) throws DebugException {
		ArrayReference ar= getArrayReference();
		if (ar == null) {
			requestFailed(JDIDebugModelMessages.getString("JDIArrayEntryVariable.value_modification_failed"), null); //$NON-NLS-1$
		}
		try {
			ar.setValue(fIndex, value);
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayEntryVariable.exception_modifying_variable_value"), new String[] {e.toString()}), e); //$NON-NLS-1$
		} catch (InvalidTypeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayEntryVariable.exception_modifying_variable_value_2"), new String[] {e.toString()}), e); //$NON-NLS-1$
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayEntryVariable.exception_modifying_variable_value_3"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}

	}

	protected ArrayReference getArrayReference() {
		return fArray;
	}
	
	/**
	 * @see IVariable
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			return stripBrackets(getArrayReference().referenceType().name());
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayEntryVariable.exception_retrieving_reference_type"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return getUnknownMessage();
	}
	
	/**
	 * Given a type name, strip out one set of array brackets and
	 * return the result.  Example:  "int[][][]" becomes "int[][]".
	 */
	protected String stripBrackets(String typeName) {
		int lastLeft= typeName.lastIndexOf("[]"); //$NON-NLS-1$
		if (lastLeft < 0) {
			return typeName;
		}
		StringBuffer buffer= new StringBuffer(typeName);
		buffer.replace(lastLeft, lastLeft + 2, ""); //$NON-NLS-1$
		return buffer.toString();
	}
	
	/**
	 * @see IJavaVariable
	 */
	public String getSignature() throws DebugException {
		try {
			return getArrayReference().type().signature();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayEntryVariable.exception_retrieving_type_signature"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return getUnknownMessage();
	}
	
	protected VirtualMachine getVirtualMachine() {
		return getArrayReference().virtualMachine();
	}
}

