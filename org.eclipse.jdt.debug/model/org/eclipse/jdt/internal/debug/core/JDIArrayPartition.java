package org.eclipse.jdt.internal.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
import com.sun.jdi.ArrayReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import java.util.ArrayList;
import java.util.List;

/**
 * A sub-range of an array.
 */

public class JDIArrayPartition extends JDIVariable {
	private int fStart;
	private int fEnd;

	/**
	 * Cache of value
	 */
	protected JDIArrayPartitionValue fArrayPartitionValue;

	public JDIArrayPartition(JDIDebugElement parent, int start, int end) {
		super(parent);
		fStart= start;
		fEnd= end;
	}

	public String getName() throws DebugException {
		StringBuffer name = new StringBuffer();
		IJavaVariable jv = getRootVariable();
		if (jv != null) {
			name.append(jv.getName());
		}
		name.append('[');
		name.append(fStart);
		name.append("..");
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
	public static List splitArray(JDIDebugElement parent, int start, int end) {
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
				var= new JDIArrayEntryVariable(parent, start);
			} else {
				var= new JDIArrayPartition(parent, start, start + perSlot - 1);
			}
			children.add(var);
			start += perSlot;
		}
		return children;
	}

	protected IJavaVariable getRootVariable() {
		IDebugElement parent = getParent();
		if (parent instanceof JDIValue) {
			return (JDIVariable)((JDIValue)parent).getVariable();
		} else {
			return ((JDIArrayPartition)((JDIArrayPartitionValue)parent).getVariable()).getRootVariable();
		}
	}
	
	public int getStart() {
		return fStart;
	}
	
	public int getEnd() {
		return fEnd;
	}
	

	public ArrayReference getArrayReference() {
		IDebugElement parent = getParent();
		if (parent instanceof JDIValue) {
			return ((JDIValue)parent).getArrayReference();
		} else {
			return ((JDIArrayPartitionValue)parent).getArrayReference();
		}
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isVolatile() throws DebugException {
		IJavaVariable jv = getRootVariable();
		if (jv == null) {
			return false;
		} else {
			return jv.isVolatile();
		}
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isTransient() throws DebugException {
		IJavaVariable jv = getRootVariable();
		if (jv == null) {
			return false;
		} else {
			return jv.isTransient();
		}
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isSynthetic() throws DebugException {
		IJavaVariable jv = getRootVariable();
		if (jv == null) {
			return false;
		} else {
			return jv.isSynthetic();
		}
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isPublic() throws DebugException {
		IJavaVariable jv = getRootVariable();
		if (jv == null) {
			return false;
		} else {
			return jv.isPublic();
		}
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isPrivate() throws DebugException {
		IJavaVariable jv = getRootVariable();
		if (jv == null) {
			return false;
		} else {
			return jv.isPrivate();
		}
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isProtected() throws DebugException {
		IJavaVariable jv = getRootVariable();
		if (jv == null) {
			return false;
		} else {
			return jv.isProtected();
		}
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isPackagePrivate() throws DebugException {
		IJavaVariable jv = getRootVariable();
		if (jv == null) {
			return false;
		} else {
			return jv.isPackagePrivate();
		}
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isStatic() throws DebugException {
		IJavaVariable jv = getRootVariable();
		if (jv == null) {
			return false;
		} else {
			return jv.isStatic();
		}
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isFinal() throws DebugException {
		IJavaVariable jv = getRootVariable();
		if (jv == null) {
			return false;
		} else {
			return jv.isFinal();
		}
	}

	/**
	 * @see IVariable
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			return getArrayReference().referenceType().name();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_GET_REFERENCE_TYPE, e);
		}
		return getUnknownMessage();
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
}

