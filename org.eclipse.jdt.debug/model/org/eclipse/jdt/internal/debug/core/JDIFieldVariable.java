package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.debug.core.model.IDebugElement;
import com.sun.jdi.*;

/**
 * A field member.
 */
public class JDIFieldVariable extends JDIModificationVariable {
	/**
	 * The wrappered field
	 */
	protected Field fField;
	/**
	 * The object containing the field,
	 * or <code>null</code> for a static field.
	 */
	protected ObjectReference fObject;
	
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
	 * @see IDebugElement
	 */
	public String getName() throws DebugException {
		try {
			return fField.name();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_GET_NAME, e);
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
			targetRequestFailed(ERROR_SET_VALUE, e);
		} catch (InvalidTypeException e) {
			targetRequestFailed(ERROR_SET_VALUE, e);
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_SET_VALUE, e);
		}

	}
		
	/**
	 * @see IJavaVariable
	 */
	public boolean isVolatile() {
		return fField.isVolatile();
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isTransient() {
		return fField.isTransient();
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isSynthetic() {
		return fField.isSynthetic();
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isPublic() {
		return fField.isPublic();
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isPrivate() {
		return fField.isPrivate();
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isProtected() {
		return fField.isProtected();
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isPackagePrivate() {
		return fField.isPackagePrivate();
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isStatic() {
		return fField.isStatic();
	}
	
	/**
	 * @see IJavaVariable
	 */
	public boolean isFinal() {
		return fField.isFinal();
	}

	/**
	 * @see IVariable
	 */
	public String getReferenceTypeName() {
		return fField.typeName();
	}
	
	/**
	 * @see IJavaVariable
	 */
	public String getSignature() throws DebugException {
		try {
			return fField.signature();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_GET_SIGNATURE, e);
		} 
		return getUnknownMessage();
	}

	/**
	 * Returns this variables underlying jdi field
	 */
	protected Field getField() {
		return fField;
	}
	
	protected VirtualMachine getVirtualMachine() {
		return fField.virtualMachine();
	}
	
	public boolean supportsValueModification() {
		if (fField.declaringType()instanceof InterfaceType) {
			return false;
		}
		return super.supportsValueModification();
	}
}

