package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

import com.sun.jdi.*;

/**
 * A sub-range of an array.
 */

public class JDIArrayPartition extends JDIVariable {
	private int fStart;
	private int fEnd;
	protected ArrayReference fArray;
	/**
	 * Cache of value
	 */
	protected JDIArrayPartitionValue fArrayPartitionValue;

	public JDIArrayPartition(JDIDebugTarget target, ArrayReference array, int start, int end) {
		super(target);
		fArray= array;
		fStart= start;
		fEnd= end;
	}

	public String getName() throws DebugException {
		StringBuffer name = new StringBuffer();
		name.append('[');
		name.append(fStart);
		name.append(".."); //$NON-NLS-1$
		name.append(fEnd);
		name.append(']');
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
	 * @see IVariable
	 */
	public IValue getValue() {
		if (fArrayPartitionValue == null) {
			fArrayPartitionValue = new JDIArrayPartitionValue(this);
		} 
		return fArrayPartitionValue;
	}

	/**
	 */
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
	
	public int getStart() {
		return fStart;
	}
	
	public int getEnd() {
		return fEnd;
	}
	

	public ArrayReference getArrayReference() {
		return fArray;
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isVolatile() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isTransient() throws DebugException {
		return false;

	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isSynthetic() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isPublic() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isPrivate() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isProtected() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isPackagePrivate() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isStatic() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isFinal() throws DebugException {
		return false;
	}

	/**
	 * @see IVariable
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			return getArrayReference().referenceType().name();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayPartition.exception_retrieving_reference_type"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return getUnknownMessage();
	}

	/**
	 * @see IJavaVariable
	 */
	public String getSignature() throws DebugException {
		try {
			return getArrayReference().type().signature();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIArrayPartition.exception_retrieving_signature"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return getUnknownMessage();
	}
}

