/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import com.ibm.icu.text.MessageFormat;
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
public class JDIFieldVariable extends JDIModificationVariable implements
		IJavaFieldVariable {
	/**
	 * The underlying field
	 */
	private Field fField;
	/**
	 * The object containing the field, or <code>null</code> for a static field.
	 */
	private ObjectReference fObject;
	/**
	 * The type containing the field.
	 */
	private ReferenceType fType;

	/**
	 * When created for a logical structure we hold onto the original
	 * non-logical value for purposes of equality. This way a logical
	 * structure's children remain more stable in the variables view.
	 * 
	 * This is <code>null</code> when not created for a logical structure.
	 */
	private IJavaValue fLogicalParent;

	/**
	 * Constructs a field for the given field.
	 */
	public JDIFieldVariable(JDIDebugTarget target, Field field,
			ObjectReference objectRef, IJavaValue logicalParent) {
		super(target);
		fField = field;
		if (!field.isStatic()) {
			fObject = objectRef;
		}
		fType = (ReferenceType) objectRef.type();
		fLogicalParent = logicalParent;
	}

	/**
	 * Constructs a field to wrap the given field.
	 */
	public JDIFieldVariable(JDIDebugTarget target, Field field,
			ReferenceType refType) {
		super(target);
		fField = field;
		fType = refType;
	}

	/**
	 * Returns this variable's current <code>Value</code>.
	 */
	@Override
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
		return JDIType.createType((JDIDebugTarget) getDebugTarget(),
				fField.declaringType());
	}

	/**
	 * @see IVariable#getName()
	 */
	public String getName() throws DebugException {
		try {
			return getField().name();
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIFieldVariable_exception_retrieving_field_name,
							e.toString()), e);
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;
		}
	}

	@Override
	protected void setJDIValue(Value value) throws DebugException {
		try {
			if (isStatic()) {
				ReferenceType declaringType = getField().declaringType();
				if (declaringType instanceof InterfaceType) {
					requestFailed(JDIDebugModelMessages.JDIFieldVariable_0,
							null);
				}
				((ClassType) declaringType).setValue(getField(), value);
			} else {
				getObjectReference().setValue(getField(), value);
			}
			fireChangeEvent(DebugEvent.CONTENT);
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIFieldVariable_exception_modifying_value,
							e.toString()), e);
		} catch (InvalidTypeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIFieldVariable_exception_modifying_value,
							e.toString()), e);
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIFieldVariable_exception_modifying_value,
							e.toString()), e);
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
	@Override
	public boolean isSynthetic() {
		return getField().isSynthetic();
	}

	/**
	 * @see IJavaModifiers#isPublic()
	 */
	@Override
	public boolean isPublic() {
		return getField().isPublic();
	}

	/**
	 * @see IJavaModifiers#isPrivate()
	 */
	@Override
	public boolean isPrivate() {
		return getField().isPrivate();
	}

	/**
	 * @see IJavaModifiers#isProtected()
	 */
	@Override
	public boolean isProtected() {
		return getField().isProtected();
	}

	/**
	 * @see IJavaModifiers#isPackagePrivate()
	 */
	@Override
	public boolean isPackagePrivate() {
		return getField().isPackagePrivate();
	}

	/**
	 * @see IJavaModifiers#isStatic()
	 */
	@Override
	public boolean isStatic() {
		return getField().isStatic();
	}

	/**
	 * @see IJavaModifiers#isFinal()
	 */
	@Override
	public boolean isFinal() {
		return getField().isFinal();
	}

	/**
	 * @see IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		String genericSignature = getField().genericSignature();
		if (genericSignature != null) {
			return JDIReferenceType.getTypeName(genericSignature);
		}
		Type underlyingType = getUnderlyingType();
		if (underlyingType instanceof ReferenceType) {
			return JDIReferenceType
					.getGenericName((ReferenceType) underlyingType);
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
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIFieldVariable_exception_retrieving_field_signature,
							e.toString()), e);
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#getGenericSignature()
	 */
	public String getGenericSignature() throws DebugException {
		try {
			String genericSignature = fField.genericSignature();
			if (genericSignature != null) {
				return genericSignature;
			}
			return fField.signature();
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIFieldVariable_exception_retrieving_field_signature,
							e.toString()), e);
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

	@Override
	public boolean supportsValueModification() {
		if (getField().declaringType() instanceof InterfaceType) {
			return false;
		}
		return super.supportsValueModification();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getField().toString();
	}

	/**
	 * @see JDIVariable#getUnderlyingType()
	 */
	@Override
	protected Type getUnderlyingType() throws DebugException {
		try {
			return getField().type();
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIFieldVariable_exception_while_retrieving_type_of_field,
							e.toString()), e);
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIFieldVariable_exception_while_retrieving_type_of_field,
							e.toString()), e);
		}
		// this line will not be executed as an exception
		// will be throw in type retrieval fails
		return null;
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof JDIFieldVariable) {
			JDIFieldVariable f = (JDIFieldVariable) o;
			if (fLogicalParent != null) {
				return fLogicalParent.equals(f.fLogicalParent)
						&& f.fField.equals(fField);
			}
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
	@Override
	public int hashCode() {
		if (fLogicalParent != null) {
			return fLogicalParent.hashCode() + fField.hashCode();
		}
		if (fObject != null) {
			return fField.hashCode() + fObject.hashCode();
		}
		return fField.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaFieldVariable#getObject()
	 */
	public IJavaObject getReceiver() {
		ObjectReference objectReference = getObjectReference();
		if (objectReference == null) {
			return null;
		}
		return (IJavaObject) JDIValue.createValue(getJavaDebugTarget(),
				objectReference);
	}

	public IJavaReferenceType getReceivingType() {
		return (IJavaReferenceType) JDIType.createType(getJavaDebugTarget(),
				getReferenceType());
	}

}
