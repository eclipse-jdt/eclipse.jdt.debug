package org.eclipse.jdt.internal.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

/**
 * An entry in an array.
 */

public class JDIArrayEntryVariable extends JDIModificationVariable {
		
	/**
	 * The index of the variable entry
	 */
	private int fIndex;
	
	/**
	 * The array object
	 */
	private ArrayReference fArray;
	
	/**
	 * The reference type name of this variable. Cached lazily.
	 */
	private String fReferenceTypeName= null;
	
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
			return ar.getValue(getIndex());
		}
		return null;
	}

	/**
	 * @see IVariable#getName()
	 */
	public String getName() {
		return "[" + getIndex() + "]"; //$NON-NLS-2$ //$NON-NLS-1$
	}

	protected void setValue(Value value) throws DebugException {
		ArrayReference ar= getArrayReference();
		if (ar == null) {
			requestFailed(JDIDebugModelMessages.getString("JDIArrayEntryVariable.value_modification_failed"), null); //$NON-NLS-1$
		}
		try {
			ar.setValue(getIndex(), value);
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
	
	protected int getIndex() {
		return fIndex;
	}
	
	/**
	 * @see IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			if (fReferenceTypeName == null) {
				fReferenceTypeName= stripBrackets(getArrayReference().referenceType().name());
			}
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayEntryVariable.exception_retrieving_reference_type"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;
		}
		return fReferenceTypeName;
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
	 * @see IJavaVariable#getSignature()
	 */
	public String getSignature() throws DebugException {
		try {
			return getArrayReference().type().signature();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayEntryVariable.exception_retrieving_type_signature"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception			
			return null;
		}
	}
	
	/**
	 * @see IValueModification#setValue(IValue)
	 */
	public	void setValue(IValue v) throws DebugException {
		if (verifyValue(v)) {
			JDIValue value = (JDIValue)v;
			try {
				getArrayReference().setValue(getIndex(), value.getUnderlyingValue());
			} catch (InvalidTypeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayEntryVariable.exception_attempting_to_set_value_of_field"), new String[]{e.toString()}), e); //$NON-NLS-1$
			} catch (ClassNotLoadedException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayEntryVariable.exception_attempting_to_set_value_of_field"), new String[]{e.toString()}), e); //$NON-NLS-1$
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayEntryVariable.exception_attempting_to_set_value_of_field"), new String[]{e.toString()}), e); //$NON-NLS-1$
			}
		}
	}	
	
	/**
	 * @see JDIVariable#getUnderlyingType()
	 */
	protected Type getUnderlyingType() throws DebugException {
		try {
			return ((ArrayType)getArrayReference().type()).componentType();
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayEntryVariable.exception_while_retrieving_type_of_array_entry"), new String[]{e.toString()}), e); //$NON-NLS-1$
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayEntryVariable.exception_while_retrieving_type_of_array_entry"), new String[]{e.toString()}), e); //$NON-NLS-1$
		}
		// this line will not be exceucted as an exception
		// will be throw in type retrieval fails
		return null;
	}		
}

