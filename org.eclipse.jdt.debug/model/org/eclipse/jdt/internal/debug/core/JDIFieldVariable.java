package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

/**
 * A field member.
 */
public class JDIFieldVariable extends JDIModificationVariable {
	/**
	 * The wrappered field
	 */
	private Field fField;
	/**
	 * The object containing the field,
	 * or <code>null</code> for a static field.
	 */
	private ObjectReference fObject;
	
	/**
	 * Constructs a field wrappering the given field.
	 */
	public JDIFieldVariable(JDIDebugTarget target, Field field, ObjectReference objectRef) {
		super(target);
		fField= field;
		fObject= objectRef;
	}

	/**
	 * Returns this variable's current <code>Value</code>.
	 */
	protected Value retrieveValue() {
		if (getField().isStatic()) {
			return (getField().declaringType().getValue(getField()));
		} else {
			return getObjectReference().getValue(getField());
		}			
	}

	/**
	 * @see IVariable#getName()
	 */
	public String getName() throws DebugException {
		try {
			return getField().name();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIFieldVariable.exception_retrieving_field_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;
		}
	}

	protected void setValue(Value value) throws DebugException {
		try {
			if (isStatic()) { 
				((ClassType)getField().declaringType()).setValue(getField(), value);
			} else {
				getObjectReference().setValue(getField(), value);
			}
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIFieldVariable.exception_modifying_value_1"), new String[] {e.toString()}), e); //$NON-NLS-1$
		} catch (InvalidTypeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIFieldVariable.exception_modifying_value_2"), new String[] {e.toString()}), e); //$NON-NLS-1$
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIFieldVariable.exception_modifying_value_3"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}

	}
		
	/**
	 * @see IJavaVariable#isVolatile()
	 */
	public boolean isVolatile() {
		return getField().isVolatile();
	}
	
	/**
	 * @see IJavaVariable#isTransient()
	 */
	public boolean isTransient() {
		return getField().isTransient();
	}
	
	/**
	 * @see IJavaModifiers#isSynthetic()
	 */
	public boolean isSynthetic() {
		return getField().isSynthetic();
	}
	
	/**
	 * @see IJavaModifiers#isPublic()
	 */
	public boolean isPublic() {
		return getField().isPublic();
	}
	
	/**
	 * @see IJavaModifiers#isPrivate()
	 */
	public boolean isPrivate() {
		return getField().isPrivate();
	}
	
	/**
	 * @see IJavaModifiers#isProtected()
	 */
	public boolean isProtected() {
		return getField().isProtected();
	}
	
	/**
	 * @see IJavaModifiers#isPackagePrivate()
	 */
	public boolean isPackagePrivate() {
		return getField().isPackagePrivate();
	}
	
	/**
	 * @see IJavaModifiers#isStatic()
	 */
	public boolean isStatic() {
		return getField().isStatic();
	}
	
	/**
	 * @see IJavaModifiers#isFinal()
	 */
	public boolean isFinal() {
		return getField().isFinal();
	}

	/**
	 * @see IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() {
		return getField().typeName();
	}
	
	/**
	 * @see IJavaVariable#getSignature()
	 */
	public String getSignature() throws DebugException {
		try {
			return getField().signature();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIFieldVariable.exception_retrieving_field_signature"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception			
			return null;
		} 
	}

	protected Field getField() {
		return fField;
	}
	
	protected ObjectReference getObjectReference() {
		return fObject;
	}
	
	public boolean supportsValueModification() {
		if (getField().declaringType()instanceof InterfaceType) {
			return false;
		}
		return super.supportsValueModification();
	}
}

