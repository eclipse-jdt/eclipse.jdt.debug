package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;

import com.sun.jdi.Type;
import com.sun.jdi.Value;

public abstract class JDIVariable extends JDIDebugElement implements IJavaVariable {
	
	/**
	 * Cache of current value - see #getValue().
	 */
	private JDIValue fValue;
	
	protected final static String jdiStringSignature= "Ljava/lang/String;"; //$NON-NLS-1$
	
	public JDIVariable(JDIDebugTarget target) {
		super(target);
	}
	
	/**
	 * @see PlatformObject#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IJavaVariable.class || adapter == IJavaModifiers.class) {
			return this;
		}			
		return super.getAdapter(adapter);
	}


	/**
	 * @see IDebugElement#getElementType()
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
			// execution will not reach this line, as
			// #targetRequestFailed will throw an exception			
			return null;
		}
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
	 * @see IVariable#getValue()
	 */
	public IValue getValue() throws DebugException {
		Value currentValue = getCurrentValue();
		if (fValue == null) {
			fValue = JDIValue.createValue((JDIDebugTarget)getDebugTarget(), currentValue);
		} else {
			Value previousValue = fValue.getUnderlyingValue();
			if (currentValue == previousValue) {
				return fValue;
			}
			if (previousValue == null || currentValue == null) {
				fValue = JDIValue.createValue((JDIDebugTarget)getDebugTarget(), currentValue);
			} else if (!previousValue.equals(currentValue)) {
				fValue = JDIValue.createValue((JDIDebugTarget)getDebugTarget(), currentValue);
			}
		}
		return fValue;
	}

	/**
	 * @see IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		return false;
	}

	/**
	 * @see IValueModification#setValue(String)
	 */
	public void setValue(String expression) throws DebugException {
		notSupported(JDIDebugModelMessages.getString("JDIVariable.does_not_support_value_modification")); //$NON-NLS-1$
	}
	
	/**
	 * @see IValueModification#setValue(IValue)
	 */
	public void setValue(IValue value) throws DebugException {
		notSupported(JDIDebugModelMessages.getString("JDIVariable.does_not_support_value_modification")); //$NON-NLS-1$
	}	

	/**
	 * @see IValueModification#verifyValue(String)
	 */
	public boolean verifyValue(String expression) {
		return false;
	}
	
	/**
	 * @see IValueModification#verifyValue(IValue)
	 */
	public boolean verifyValue(IValue value) {
		return false;
	}		
	 
	/**
	 * @see IJavaVariable#isVolatile()
	 */
	public boolean isVolatile() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaVariable#isTransient()
	 */
	public boolean isTransient() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaModifiers#isSynthetic()
	 */
	public boolean isSynthetic() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaModifiers#isPublic()
	 */
	public boolean isPublic() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaModifiers#isPrivate()
	 */
	public boolean isPrivate() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaModifiers#isProtected()
	 */
	public boolean isProtected() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaModifiers#isPackagePrivate()
	 */
	public boolean isPackagePrivate() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaModifiers#isStatic()
	 */
	public boolean isStatic() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaModifiers#isFinal()
	 */
	public boolean isFinal() throws DebugException {
		return false;
	}
	
	/**
	 * @see IJavaVariable#getJavaType()
	 */
	public IJavaType getJavaType() throws DebugException {
		return JDIType.createType((JDIDebugTarget)getDebugTarget(), getUnderlyingType());
	}
	
	/**
	 * Returns the undlerying type of this variable
	 * 
	 * @return the undlerying type of this variable
	 * 
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The type associated with this variable is not yet loaded</li></ul>
	 */
	protected abstract Type getUnderlyingType() throws DebugException;
}

