package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.text.MessageFormat;
import java.util.List;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaArray;

import org.eclipse.jdt.debug.core.IJavaValue;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.Value;

public class JDIArrayValue extends JDIValue implements IJavaArray {

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
		for (int i = 0; i < count; i++) {
			values[i] = getValue(i);
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
	public int getLength() throws DebugException {
		try {
			return getArrayReference().length();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format("{0} occurred while retrieving array length.", new String[] {e.toString()}), e);
		}
		// exectution will not reach this line as an
		// exception will be thrown
		return 0;
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
			targetRequestFailed(MessageFormat.format("{0} occurred while setting value in array.", new String[] {e.toString()}), e);
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(MessageFormat.format("{0} occurred while setting value in array.", new String[] {e.toString()}), e);
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format("{0} occurred while setting value in array.", new String[] {e.toString()}), e);
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
			targetRequestFailed(MessageFormat.format("{0} occurred while retrieving value from array.", new String[] {e.toString()}), e);
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
			throw e;
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format("{0} occurred while retrieving values from array.", new String[] {e.toString()}), e);
		}
		// execution will not reach this line as
		// an exception will be thrown
		return null;
	}	
}

