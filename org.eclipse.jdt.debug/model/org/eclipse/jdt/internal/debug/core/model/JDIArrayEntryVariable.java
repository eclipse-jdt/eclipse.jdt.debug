/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ReferenceType;
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
	private String fReferenceTypeName = null;

	/**
	 * When created for a logical structure we hold onto the original
	 * non-logical object for purposes of equality. This way a logical
	 * structure's children remain more stable in the variables view.
	 * 
	 * This is <code>null</code> when not created for a logical structure.
	 */
	private IJavaValue fLogicalParent;

	/**
	 * Constructs an array entry at the given index in an array.
	 * 
	 * @param target
	 *            debug target containing the array entry
	 * @param array
	 *            array containing the entry
	 * @param index
	 *            index into the array
	 * @param logicalParent
	 *            original logical parent value, or <code>null</code> if not a
	 *            child of a logical structure
	 */
	public JDIArrayEntryVariable(JDIDebugTarget target, ArrayReference array,
			int index, IJavaValue logicalParent) {
		super(target);
		fArray = array;
		fIndex = index;
		fLogicalParent = logicalParent;
	}

	/**
	 * Returns this variable's current underlying value.
	 */
	@Override
	protected Value retrieveValue() {
		ArrayReference ar = getArrayReference();
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

	@Override
	protected void setJDIValue(Value value) throws DebugException {
		ArrayReference ar = getArrayReference();
		if (ar == null) {
			requestFailed(
					JDIDebugModelMessages.JDIArrayEntryVariable_value_modification_failed,
					null);
		}
		try {
			ar.setValue(getIndex(), value);
			fireChangeEvent(DebugEvent.CONTENT);
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIArrayEntryVariable_exception_modifying_variable_value,
							e.toString()), e);
		} catch (InvalidTypeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIArrayEntryVariable_exception_modifying_variable_value,
							e.toString()), e);
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIArrayEntryVariable_exception_modifying_variable_value,
							e.toString()), e);
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
				fReferenceTypeName = stripBrackets(JDIReferenceType
						.getGenericName(getArrayReference().referenceType()));
			}
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIArrayEntryVariable_exception_retrieving_reference_type,
							e.toString()), e);
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;
		}
		return fReferenceTypeName;
	}

	/**
	 * Given a type name, strip out one set of array brackets and return the
	 * result. Example: "int[][][]" becomes "int[][]".
	 */
	protected String stripBrackets(String typeName) {
		int lastLeft = typeName.lastIndexOf("[]"); //$NON-NLS-1$
		if (lastLeft < 0) {
			return typeName;
		}
		StringBuffer buffer = new StringBuffer(typeName);
		buffer.replace(lastLeft, lastLeft + 2, ""); //$NON-NLS-1$
		return buffer.toString();
	}

	/**
	 * @see IJavaVariable#getSignature()
	 */
	public String getSignature() throws DebugException {
		try {
			return ((ArrayType) getArrayReference().type())
					.componentSignature();
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIArrayEntryVariable_exception_retrieving_type_signature,
							e.toString()), e);
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#getGenericSignature()
	 */
	public String getGenericSignature() throws DebugException {
		try {
			ReferenceType referenceType = getArrayReference().referenceType();
			String genericSignature = referenceType.genericSignature();
			if (genericSignature != null) {
				return genericSignature;
			}
			return referenceType.signature();
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIArrayEntryVariable_exception_retrieving_type_signature,
							e.toString()), e);
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;
		}
	}

	/**
	 * @see JDIVariable#getUnderlyingType()
	 */
	@Override
	protected Type getUnderlyingType() throws DebugException {
		try {
			return ((ArrayType) getArrayReference().type()).componentType();
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIArrayEntryVariable_exception_while_retrieving_type_of_array_entry,
							e.toString()), e);
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIArrayEntryVariable_exception_while_retrieving_type_of_array_entry,
							e.toString()), e);
		}
		// this line will not be executed as an exception
		// will be throw in type retrieval fails
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JDIArrayEntryVariable) {
			JDIArrayEntryVariable entry = (JDIArrayEntryVariable) obj;
			if (fLogicalParent != null) {
				try {
					return fLogicalParent.equals(entry.fLogicalParent)
							&& getValue().equals(entry.getValue());
				} catch (CoreException e) {
				}
			}
			return entry.getArrayReference().equals(getArrayReference())
					&& entry.getIndex() == getIndex();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (fLogicalParent != null) {
			return fLogicalParent.hashCode() + getIndex();
		}
		return getArrayReference().hashCode() + getIndex();
	}

}
