package org.eclipse.jdt.internal.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaVariable;

public abstract class JDIVariable extends JDIDebugElement implements IJavaVariable {
	
	// NLS
	protected final static String PREFIX= "jdi_variable.";
	protected static final String ERROR = PREFIX + "error.";
	protected static final String ERROR_GET_NAME = ERROR + "get_name";
	protected static final String ERROR_GET_REFERENCE_TYPE = ERROR + "get_reference_type";
	protected static final String ERROR_GET_SIGNATURE = ERROR + "get_signature";
	protected static final String ERROR_GET_VALUE= ERROR + "get_value";
	protected static final String ERROR_SET_VALUE= ERROR + "set_value";
	protected static final String ERROR_SET_VALUE_NOT_SUPPORTED= ERROR + "set_value.not_supported";
	
	/**
	 * Cache of current value - see #getValue().
	 */
	protected JDIValue fValue;
	
	//non NLS
	protected final static String jdiStringSignature= "Ljava/lang/String;";

	/**
	 * Creates a new variable with the given parent. Parents can 
	 * be stack frames (for locals), or values for field members.	
	 */
	public JDIVariable(JDIDebugElement parent) {
		super(parent);
	}
	
	/**
	 * @see IAdaptable
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IJavaVariable.class || adapter == IJavaModifiers.class) {
			return this;
		}			
		return super.getAdapter(adapter);
	}


	/**
	 * @see IDebugElement
	 */
	public int getElementType() {
		return VARIABLE;
	}

	/**
	 * Returns this variable's current underlying jdi value.
	 * Subclasses must implement #retrieveValue() and do not
	 * need to guard against JDI exceptions, as this method
	 * handles them.
	 *
	 * @exception DebugException if unable to access the value
	 */
	protected final Value getCurrentValue() throws DebugException {
		try {
			return retrieveValue();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_GET_VALUE, e);
		}
		return null;
	}
	
	/**
	 * Returns this variables underlying jdi value
	 */
	protected abstract Value retrieveValue();
	
	/**
	 * Returns the current value of this variable. The value
	 * is cached, but on each access we see if the value has changed
	 * and update if required.
	 *
	 * @see IVariable
	 */
	public IValue getValue() throws DebugException {
		Value currentValue = getCurrentValue();
		if (fValue == null) {
			fValue = new JDIValue(this, currentValue);
		} else {
			Value previousValue = fValue.getUnderlyingValue();
			if (currentValue == previousValue) {
				return fValue;
			}
			if (previousValue == null || currentValue == null) {
				fValue = new JDIValue(this, currentValue);
			} else if (!previousValue.equals(currentValue)) {
				fValue = new JDIValue(this, currentValue);
			}
		}
		return fValue;
	}

	/**
	 * @see IValueModification
	 */
	public boolean supportsValueModification() {
		return false;
	}

	/**
	 * @see IValueModification
	 */
	public void setValue(String expression) throws DebugException {
		notSupported(ERROR_SET_VALUE_NOT_SUPPORTED);
	}

	/**
	 * @see IValueModification
	 */
	public boolean verifyValue(String expression) {
		return false;
	}	

	/**
	 * @see IDebugElement
	 */
	public IStackFrame getStackFrame() {
		return getParent().getStackFrame();
	}
	
	/**
	 * @see IDebugElement
	 */
	public IThread getThread() {
		return getParent().getThread();
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
}

