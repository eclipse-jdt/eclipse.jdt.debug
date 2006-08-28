package org.eclipse.jdt.internal.debug.core.logicalstructures;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.model.JDIArrayValue;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.Value;

public class JDIAllInstancesValue extends JDIArrayValue {

	private IJavaObject[] fElements = null;
	private IJavaArrayType fType = null;
	
	/**
	 * Constructor
	 * @param target the target VM
	 * @param value the underlying ArrayReference
	 * @param values the values to set in the array
	 */
	public JDIAllInstancesValue(JDIDebugTarget target, IJavaObject[] values) {
		super(target, null);
		fElements = values;
		try {
			IJavaType[] javaTypes = target.getJavaTypes("java.lang.Object[]"); //$NON-NLS-1$
			if (javaTypes.length > 0) {
				fType = (IJavaArrayType) javaTypes[0];
			}
		} catch (DebugException e) {}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#getLength()
	 */
	public synchronized int getLength() throws DebugException {
		return (fElements != null ? fElements.length : 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#getSize()
	 */
	public int getSize() throws DebugException {
		return (fElements != null ? fElements.length : 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#getValue(int)
	 */
	public IJavaValue getValue(int index) throws DebugException {
		if(fElements != null) {
			if(index > fElements.length-1 || index < 0) {
				internalError(LogicalStructuresMessages.JDIAllInstancesValue_0);
			}
			return fElements[index];
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#getValues()
	 */
	public IJavaValue[] getValues() throws DebugException {
		return fElements;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#getVariable(int)
	 */
	public IVariable getVariable(int offset) throws DebugException {
		if(fElements != null) {
			if(offset > fElements.length-1 || offset < 0) {
				internalError(LogicalStructuresMessages.JDIAllInstancesValue_1);
			}
			return new JDIPlaceholderVariable("[" + offset + "]", fElements[offset]); //$NON-NLS-1$ //$NON-NLS-2$
		}
		internalError(LogicalStructuresMessages.JDIAllInstancesValue_2);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#getVariables(int, int)
	 */
	public IVariable[] getVariables(int offset, int length) throws DebugException {
		if(fElements != null) {
			if(offset > fElements.length-1 || offset < 0) {
				internalError(LogicalStructuresMessages.JDIAllInstancesValue_1);
			}
			IVariable[] vars = new JDIPlaceholderVariable[length];
			for (int i = 0; i < length; i++) {
				vars[i] = getVariable(i + offset);
			}
			return vars;
		}
		internalError(LogicalStructuresMessages.JDIAllInstancesValue_2);
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		return getVariables(0, fElements.length);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIObjectValue#getReferringObjects(long)
	 */
	public IJavaObject[] getReferringObjects(long max) throws DebugException {
		return new IJavaObject[0];
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException {
		return getJavaDebugTarget().isAvailable();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#getInitialOffset()
	 */
	public int getInitialOffset() {
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIArrayValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return (fElements != null ? fElements.length > 0 : false);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#getJavaType()
	 */
	public IJavaType getJavaType() throws DebugException {
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#getSignature()
	 */
	public String getSignature() throws DebugException {
		return fType.getSignature();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIObjectValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return fType.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIValue#getValueString()
	 */
	public String getValueString() throws DebugException {
		StringBuffer buf = new StringBuffer();
		buf.append("["); //$NON-NLS-1$
		for (int i = 0; i < fElements.length; i++) {
			buf.append(fElements[i].getValueString());
			if (i < (fElements.length - 1)) {
				buf.append(", "); //$NON-NLS-1$
			}
		}
		buf.append("]"); //$NON-NLS-1$
		return buf.toString();
	}

	protected Value getUnderlyingValue() {
		// TODO Auto-generated method stub
		return super.getUnderlyingValue();
	}
	
}
