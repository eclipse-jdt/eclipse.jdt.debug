/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaReferenceType;

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;

/**
 * References a class, interface, or array type.
 */
public abstract class JDIReferenceType extends JDIType implements IJavaReferenceType {
	
	// field names declared in this type
	private String[] fDeclaredFields = null;
	// field names declared in this type, super types, implemented interaces and superinterfaces
	private String[] fAllFields = null;

	/**
	 * Constructs a new reference type in the given target.
	 * 
	 * @param target associated vm
	 * @param type reference type
	 */
	public JDIReferenceType(JDIDebugTarget target, Type type) {
		super(target, type);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getAvailableStrata()
	 */
	public String[] getAvailableStrata() throws DebugException {
		List strata = getReferenceType().availableStrata();
		return (String[])strata.toArray(new String[strata.size()]);
	}

	/**
	 * Returns the underlying reference type.
	 * 
	 * @return the underlying reference type
	 */
	protected ReferenceType getReferenceType() {
		return (ReferenceType)getUnderlyingType();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getDefaultStratum()
	 */
	public String getDefaultStratum() throws DebugException {
		return getReferenceType().defaultStratum();
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getField(java.lang.String)
	 */
	public IJavaFieldVariable getField(String name) throws DebugException {
		try {
			Field field = ((ReferenceType)getUnderlyingType()).fieldByName(name);
			if (field != null) {
				ClassObjectReference classObject = ((JDIClassObjectValue)getClassObject()).getUnderlyingClassObject();
				return new JDIFieldVariable(getDebugTarget(), field, classObject);
			}			
		} catch (RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIClassType.exception_while_retrieving_field"), new String[] {e.toString(), name}), e); //$NON-NLS-1$
		}
		// it is possible to return null		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getClassObject()
	 */
	public IJavaClassObject getClassObject() throws DebugException {
		try {
			ReferenceType type= (ReferenceType)getUnderlyingType();
			return (IJavaClassObject)JDIValue.createValue(getDebugTarget(), type.classObject());
		} catch (RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIClassType.exception_while_retrieving_class_object"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		// execution will not fall through to here,
		// as #requestFailed will throw an exception
		return null;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getAllFieldNames()
	 */
	public String[] getAllFieldNames() throws DebugException {
		if (fAllFields == null) {
			try {
				List fields = ((ReferenceType)getUnderlyingType()).allFields();
				fAllFields = new String[fields.size()];
				Iterator iterator = fields.iterator();
				int i = 0;
				while (iterator.hasNext()) {
					Field field = (Field)iterator.next();
					fAllFields[i] = field.name();
					i++;
				}
			} catch (RuntimeException e) {
				getDebugTarget().targetRequestFailed(JDIDebugModelMessages.getString("JDIReferenceType.2"), e); //$NON-NLS-1$
			}			
		}			
		return fAllFields;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getDeclaredFieldNames()
	 */
	public String[] getDeclaredFieldNames() throws DebugException {
		if (fDeclaredFields == null) {
			try {
				List fields = ((ReferenceType)getUnderlyingType()).fields();
				fDeclaredFields = new String[fields.size()];
				Iterator iterator = fields.iterator();
				int i = 0;
				while (iterator.hasNext()) {
					Field field = (Field)iterator.next();
					fDeclaredFields[i] = field.name();
					i++;
				}
			} catch (RuntimeException e) {
				getDebugTarget().targetRequestFailed(JDIDebugModelMessages.getString("JDIReferenceType.3"), e); //$NON-NLS-1$
			}			
		}
		return fDeclaredFields;
	}

}
