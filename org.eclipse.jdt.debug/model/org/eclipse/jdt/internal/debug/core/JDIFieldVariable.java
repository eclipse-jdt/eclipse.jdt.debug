package org.eclipse.jdt.internal.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
	 * Constructs a field wrappering the given field.
	 */
	public JDIFieldVariable(JDIDebugElement parent, Field field) {
		super(parent);
		fField= field;
	}

	/**
	 * Returns this variable's current <code>Value</code>.
	 */
	protected Value retrieveValue() {
		if (fField.isStatic()) {
			return (fField.declaringType().getValue(fField));
		} else {
			IDebugElement parent= getParent();
			ObjectReference or= (ObjectReference) ((JDIValue) parent).fValue;
			if (or == null) {
				return null;
			}
			return or.getValue(fField);
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
			IDebugElement parent= getParent();
			if (isStatic()) { 
				((ClassType)fField.declaringType()).setValue(fField, value);
			} else {
				ObjectReference or= (ObjectReference) ((JDIValue) parent).fValue;
				if (or == null) {
					requestFailed(ERROR_SET_VALUE, null);
				}
				or.setValue(fField, value);
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
}

