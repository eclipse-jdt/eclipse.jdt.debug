/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

 
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IIndexedValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaValue;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.Value;

public class JDIArrayValue extends JDIObjectValue implements IJavaArray, IIndexedValue{
	
	private int fLength = -1; 

	/**
	 * Constructs a value which is a reference to an array.
	 * @param target debug target on which the array exists
	 * @param value the reference to the array
	 */
	public JDIArrayValue(JDIDebugTarget target, ArrayReference value) {
		super(target, value);
	}

	/**
	 * @see IJavaArray#getValues()
	 */
	public IJavaValue[] getValues() throws DebugException {
		List list = getUnderlyingValues();

		int count = list.size();
		IJavaValue[] values = new IJavaValue[count];
		JDIDebugTarget target = (JDIDebugTarget) getDebugTarget();
		for (int i = 0; i < count; i++) {
			Value value = (Value)list.get(i);
			values[i] = JDIValue.createValue(target, value);
		}
		return values;
	}

	/**
	 * @see IJavaArray#getValue(int)
	 */
	public IJavaValue getValue(int index) throws DebugException {
		Value v = getUnderlyingValue(index);
		return JDIValue.createValue((JDIDebugTarget)getDebugTarget(), v);
	}

	/**
	 * @see IJavaArray#getLength()
	 */
	public synchronized int getLength() throws DebugException {
		if (fLength == -1) {
			try {
				fLength = getArrayReference().length();
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIArrayValue_exception_while_retrieving_array_length, new String[] {e.toString()}), e); //$NON-NLS-1$
			}
		}
		return fLength;
	}

	/**
	 * @see IJavaArray#setValue(int, IJavaValue)
	 */
	public void setValue(int index, IJavaValue value) throws DebugException {
		try {
			getArrayReference().setValue(index, ((JDIValue)value).getUnderlyingValue());
		} catch (IndexOutOfBoundsException e) {
			throw e;
		} catch (InvalidTypeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIArrayValue_exception_while_setting_value_in_array, new String[] {e.toString()}), e); //$NON-NLS-1$
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIArrayValue_exception_while_setting_value_in_array, new String[] {e.toString()}), e); //$NON-NLS-1$
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIArrayValue_exception_while_setting_value_in_array, new String[] {e.toString()}), e); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the underlying array reference for this
	 * array.
	 * 
	 * @return underlying array reference
	 */
	protected ArrayReference getArrayReference() {
		return (ArrayReference)getUnderlyingValue();
	}
	
	/**
	 * Returns the underlying value at the given index
	 * from the underlying array reference.
	 * 
	 * @param index the index at which to retrieve a value
	 * @return value
	 * @exception DebugException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	protected Value getUnderlyingValue(int index) throws DebugException {
		try {
			return getArrayReference().getValue(index);
		} catch (IndexOutOfBoundsException e) {
			throw e;
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIArrayValue_exception_while_retrieving_value_from_array, new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		// execution will not reach this line as
		// an exception will be thrown
		return null;
	}
	
	/**
	 * Returns the underlying values
	 * from the underlying array reference.
	 * 
	 * @return list of values
	 * @exception DebugException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	protected List getUnderlyingValues() throws DebugException {
		try {
			return getArrayReference().getValues();
		} catch (IndexOutOfBoundsException e) {
			return Collections.EMPTY_LIST;
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIArrayValue_exception_while_retrieving_values_from_array, new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		// execution will not reach this line as
		// an exception will be thrown
		return null;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getSize()
	 */
	public int getSize() throws DebugException {
		return getLength();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getVariable(int)
	 */
	public IVariable getVariable(int offset) throws DebugException {
		if (offset >= getLength()) {
			requestFailed(JDIDebugModelMessages.JDIArrayValue_6, null); //$NON-NLS-1$
		}
		return new JDIArrayEntryVariable(getJavaDebugTarget(), getArrayReference(), offset);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getVariables(int, int)
	 */
	public IVariable[] getVariables(int offset, int length) throws DebugException {
		if (offset >= getLength()) {
			requestFailed(JDIDebugModelMessages.JDIArrayValue_6, null); //$NON-NLS-1$
		}
		if ((offset + length - 1) >= getLength()) {
			requestFailed(JDIDebugModelMessages.JDIArrayValue_8, null); //$NON-NLS-1$
		}
		IVariable[] variables = new IVariable[length];
		int index = offset;
		for (int i = 0; i < length; i++) {
			variables[i] = new JDIArrayEntryVariable(getJavaDebugTarget(), getArrayReference(), index);
			index++;
		}
		return variables;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getInitialOffset()
	 */
	public int getInitialOffset() {
		return 0;
	}

}

