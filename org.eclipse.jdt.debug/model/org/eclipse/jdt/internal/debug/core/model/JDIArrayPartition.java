package org.eclipse.jdt.internal.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

/**
 * A sub-range of an array.
 */

public class JDIArrayPartition extends JDIVariable {
	
	private int fStart;
	private int fEnd;
	private ArrayReference fArray;
	
	/**
	 * Cache of value
	 */
	private JDIArrayPartitionValue fArrayPartitionValue;

	public JDIArrayPartition(JDIDebugTarget target, ArrayReference array, int start, int end) {
		super(target);
		fArray= array;
		fStart= start;
		fEnd= end;
	}

	public String getName() throws DebugException {
		StringBuffer name = new StringBuffer();
		name.append('[');  //$NON-NLS-1$
		name.append(getStart());
		name.append(".."); //$NON-NLS-1$
		name.append(getEnd());
		name.append(']');  //$NON-NLS-1$
		return name.toString();
	}
	
	/**
	 * This method is not called for an array partition, as this class
	 * overrides #getValue().
	 */
	protected Value retrieveValue() {
		return null;
	}

	/**
	 * @see IVariable#getValue()
	 */
	public IValue getValue() {
		if (fArrayPartitionValue == null) {
			fArrayPartitionValue = new JDIArrayPartitionValue(this);
		} 
		return fArrayPartitionValue;
	}

	public static List splitArray(JDIDebugTarget target, ArrayReference array, int start, int end) {
		ArrayList children= new ArrayList();
		int perSlot = 1;
		int l= end - start;
		while (perSlot * 100 < l) {
			perSlot = perSlot * 100;
		}

		while (start <= end) {
			if (start + perSlot > end) {
				perSlot= end - start + 1;
			}
			JDIVariable var= null;
			if (perSlot == 1) {
				var= new JDIArrayEntryVariable(target, array, start);
			} else {
				var= new JDIArrayPartition(target, array, start, start + perSlot - 1);
			}
			children.add(var);
			start += perSlot;
		}
		return children;
	}
	
	protected int getStart() {
		return fStart;
	}
	
	protected int getEnd() {
		return fEnd;
	}	

	protected ArrayReference getArrayReference() {
		return fArray;
	}
	
	/**
	 * @see IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			return getArrayReference().referenceType().name();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayPartition.exception_retrieving_reference_type"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception			
			return null;
		}
	}

	/**
	 * @see IJavaVariable#getSignature()
	 */
	public String getSignature() throws DebugException {
		try {
			return getArrayReference().type().signature();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayPartition.exception_retrieving_signature"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;			
		}
	}
	
	/**
	 * @see JDIVariable#getUnderlyingType()
	 */
	protected Type getUnderlyingType() throws DebugException {
		try {
			return getArrayReference().type();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayPartition.exception_while_retrieving_type_of_array"), new String[]{e.toString()}), e); //$NON-NLS-1$
		}
		// this line will not be exceucted as an exception
		// will be throw in type retrieval fails
		return null;
	}		
}

