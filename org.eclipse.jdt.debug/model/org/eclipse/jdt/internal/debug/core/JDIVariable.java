package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaVariable;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;

public abstract class JDIVariable extends JDIDebugElement implements IJavaVariable {
	
	/**
	 * Cache of current value - see #getValue().
	 */
	protected JDIValue fValue;
	
	protected final static String jdiStringSignature= "Ljava/lang/String;"; //$NON-NLS-1$
	
	public JDIVariable(JDIDebugTarget target) {
		super(target);
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
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIVariable.exception_retrieving"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * Returns this variable's underlying jdi value
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
			fValue = new JDIValue((JDIDebugTarget)getDebugTarget(), currentValue);
		} else {
			Value previousValue = fValue.getUnderlyingValue();
			if (currentValue == previousValue) {
				return fValue;
			}
			if (previousValue == null || currentValue == null) {
				fValue = new JDIValue((JDIDebugTarget)getDebugTarget(), currentValue);
			} else if (!previousValue.equals(currentValue)) {
				fValue = new JDIValue((JDIDebugTarget)getDebugTarget(), currentValue);
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
		notSupported(JDIDebugModelMessages.getString("JDIVariable.does_not_support_value_modification")); //$NON-NLS-1$
	}

	/**
	 * @see IValueModification
	 */
	public boolean verifyValue(String expression) {
		return false;
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

