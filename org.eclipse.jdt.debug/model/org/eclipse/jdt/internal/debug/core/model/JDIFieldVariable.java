/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;


import java.text.MessageFormat;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

/**
 * A field member.
 */
public class JDIFieldVariable extends JDIModificationVariable implements IJavaFieldVariable {
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
	 * The type containing the field.
	 */
	private ReferenceType fType;
	
	/**
	 * Constructs a field wrappering the given field.
	 */
	public JDIFieldVariable(JDIDebugTarget target, Field field, ObjectReference objectRef) {
		super(target);
		fField= field;
		fObject= objectRef;
		fType= (ReferenceType)objectRef.type();
	}

	/**
	 * Constructs a field wrappering the given field.
	 */
	public JDIFieldVariable(JDIDebugTarget target, Field field, ReferenceType refType) {
		super(target);
		fField= field;
		fType= refType;
	}

	/**
	 * Returns this variable's current <code>Value</code>.
	 */
	protected Value retrieveValue() {
		if (getField().isStatic()) {
			return (getField().declaringType().getValue(getField()));
		}
		return getObjectReference().getValue(getField());			
	}
	
	/**
	 * @see IJavaFieldVariable#getDeclaringType()
	 */
	public IJavaType getDeclaringType() {
		return JDIType.createType((JDIDebugTarget)getDebugTarget(), fField.declaringType());
	}

	/**
	 * @see IVariable#getName()
	 */
	public String getName() throws DebugException {
		try {
			return getField().name();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIFieldVariable_exception_retrieving_field_name, new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;
		}
	}

	protected void setJDIValue(Value value) throws DebugException {
		try {
			if (isStatic()) { 
				((ClassType)getField().declaringType()).setValue(getField(), value);
			} else {
				getObjectReference().setValue(getField(), value);
			}
			fireChangeEvent(DebugEvent.CONTENT);
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIFieldVariable_exception_modifying_value, new String[] {e.toString()}), e); //$NON-NLS-1$
		} catch (InvalidTypeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIFieldVariable_exception_modifying_value, new String[] {e.toString()}), e); //$NON-NLS-1$
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIFieldVariable_exception_modifying_value, new String[] {e.toString()}), e); //$NON-NLS-1$
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
	public String getReferenceTypeName() throws DebugException {
		String genericSignature= getField().genericSignature();
		if (genericSignature != null) {
			return JDIReferenceType.getTypeName(genericSignature);
		}
		Type underlyingType= getUnderlyingType();
		if (underlyingType instanceof ReferenceType) {
			return JDIReferenceType.getGenericName((ReferenceType)underlyingType);
		}
		return getField().typeName();
	}
	
	/**
	 * @see IJavaVariable#getSignature()
	 */
	public String getSignature() throws DebugException {
		try {
			return getField().signature();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIFieldVariable_exception_retrieving_field_signature, new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception			
			return null;
		} 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#getGenericSignature()
	 */
	public String getGenericSignature() throws DebugException {
		try {
			String genericSignature= fField.genericSignature();
			if (genericSignature != null) {
				return genericSignature;
			}
			return fField.signature();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIFieldVariable_exception_retrieving_field_signature, new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception			
			return null;
		} 
	}
	
	public Field getField() {
		return fField;
	}
	
	public ObjectReference getObjectReference() {
		return fObject;
	}
	
	public ReferenceType getReferenceType() {
		return fType;
	}
	
	public boolean supportsValueModification() {
		if (getField().declaringType()instanceof InterfaceType) {
			return false;
		}
		return super.supportsValueModification();
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getField().toString();
	}
	
	/**
	 * @see IValueModification#setValue(IValue)
	 */
	public	void setValue(IValue v) throws DebugException {
		if (verifyValue(v)) {
			setJDIValue(((JDIValue)v).getUnderlyingValue());
		}
	}
	
	/**
	 * @see JDIVariable#getUnderlyingType()
	 */
	protected Type getUnderlyingType() throws DebugException {
		try {
			return getField().type();
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIFieldVariable_exception_while_retrieving_type_of_field, new String[]{e.toString()}), e); //$NON-NLS-1$
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIFieldVariable_exception_while_retrieving_type_of_field, new String[]{e.toString()}), e); //$NON-NLS-1$
		}
		// this line will not be exceucted as an exception
		// will be throw in type retrieval fails
		return null;
	}	
	
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof JDIFieldVariable) {
			JDIFieldVariable f = (JDIFieldVariable)o;
			if (fObject != null) {
				return fObject.equals(f.fObject) && f.fField.equals(fField);
			}
			return f.fField.equals(fField);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (fObject != null) {
			return fField.hashCode() + fObject.hashCode();
		}
		return fField.hashCode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaFieldVariable#getObject()
	 */
	public IJavaObject getReceiver() {
		ObjectReference objectReference= getObjectReference();
		if (objectReference == null) {
			return null;
		}
		return (IJavaObject)JDIValue.createValue(getJavaDebugTarget(), objectReference);
	}
	
	public IJavaReferenceType getReceivingType() {
		return (IJavaReferenceType)JDIType.createType(getJavaDebugTarget(), getReferenceType());
	}

}

