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
		if (fField.isStatic()) {
			return (fField.declaringType().getValue(fField));
		} else {
			return fObject.getValue(fField);
		}			
	}

	/**
	 * @see IVariable#getName()
	 */
	public String getName() throws DebugException {
		try {
			return fField.name();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIFieldVariable.exception_retrieving_field_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return getUnknownMessage();
	}

	protected void setValue(Value value) throws DebugException {
		try {
			if (isStatic()) { 
				((ClassType)fField.declaringType()).setValue(fField, value);
			} else {
				fObject.setValue(fField, value);
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
		return fField.isVolatile();
	}
	
	/**
	 * @see IJavaVariable#isTransient()
	 */
	public boolean isTransient() {
		return fField.isTransient();
	}
	
	/**
	 * @see IJavaModifiers#isSynthetic()
	 */
	public boolean isSynthetic() {
		return fField.isSynthetic();
	}
	
	/**
	 * @see IJavaModifiers#isPublic()
	 */
	public boolean isPublic() {
		return fField.isPublic();
	}
	
	/**
	 * @see IJavaModifiers#isPrivate()
	 */
	public boolean isPrivate() {
		return fField.isPrivate();
	}
	
	/**
	 * @see IJavaModifiers#isProtected()
	 */
	public boolean isProtected() {
		return fField.isProtected();
	}
	
	/**
	 * @see IJavaModifiers#isPackagePrivate()
	 */
	public boolean isPackagePrivate() {
		return fField.isPackagePrivate();
	}
	
	/**
	 * @see IJavaModifiers#isStatic()
	 */
	public boolean isStatic() {
		return fField.isStatic();
	}
	
	/**
	 * @see IJavaModifiers#isFinal()
	 */
	public boolean isFinal() {
		return fField.isFinal();
	}

	/**
	 * @see IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() {
		return fField.typeName();
	}
	
	/**
	 * @see IJavaVariable#getSignature()
	 */
	public String getSignature() throws DebugException {
		try {
			return fField.signature();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIFieldVariable.exception_retrieving_field_signature"), new String[] {e.toString()}), e); //$NON-NLS-1$
		} 
		return getUnknownMessage();
	}

	/**
	 * Returns this variables underlying JDI field
	 */
	protected Field getField() {
		return fField;
	}
	
	public boolean supportsValueModification() {
		if (fField.declaringType()instanceof InterfaceType) {
			return false;
		}
		return super.supportsValueModification();
	}
}

