/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;


import com.ibm.icu.text.MessageFormat;

import org.eclipse.debug.core.DebugException;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.Value;


/**
 * Represents the receiver in a stack frame.
 */

public class JDIThisVariable extends JDIVariable {
	/**
	 * The wrappered object
	 */
	private ObjectReference fObject;
	
	/**
	 * Constructs a variable representing 'this' in a stack frame.
	 */
	public JDIThisVariable(JDIDebugTarget target, ObjectReference object) {
		super(target);
		fObject= object;
	}

	/**
	 * Returns this variable's current Value.
	 */
	protected Value retrieveValue() {
		return fObject;
	}

	/**
	 * @see IVariable#getName()
	 */
	public String getName() {
		return "this"; //$NON-NLS-1$
	}
	
	/**
	 * @see IJavaVariable#getSignature()
	 */
	public String getSignature() throws DebugException {
		try {
			return retrieveValue().type().signature();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIThisVariableexception_retrieving_type_signature, new String[] {e.toString()}), e); 
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;			
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#getGenericSignature()
	 */
	public String getGenericSignature() throws DebugException {
		return getSignature();
	}

	/**
	 * @see IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			return getValue().getReferenceTypeName();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIThisVariableexception_retrieving_reference_type_name, new String[] {e.toString()}), e); 
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;			
		}
	}
	
	/**
	 * @see JDIVariable#getUnderlyingType()
	 */
	protected Type getUnderlyingType() throws DebugException {
		try {
			return retrieveValue().type();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIThisVariable_exception_while_retrieving_type_this, new String[]{e.toString()}), e); 
		}
		// this line will not be exceucted as an exception
		// will be throw in type retrieval fails
		return null;
	}	
	
	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPrivate()
	 */
	public boolean isPrivate() throws DebugException {
		try {
			return ((ReferenceType)getUnderlyingType()).isPrivate(); 
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThisVariable_Exception_occurred_while_retrieving_modifiers__1, e); 
		}
		// this line will not be exceucted as an exception
		// will be throw		
		return false;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isProtected()
	 */
	public boolean isProtected() throws DebugException {
		try {
			return ((ReferenceType)getUnderlyingType()).isProtected();
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThisVariable_Exception_occurred_while_retrieving_modifiers__1, e); 
		}			
		// this line will not be exceucted as an exception
		// will be throw
		return false;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPublic()
	 */
	public boolean isPublic() throws DebugException {
		try {
			return ((ReferenceType)getUnderlyingType()).isPublic();
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThisVariable_Exception_occurred_while_retrieving_modifiers__1, e); 
		}			
		// this line will not be exceucted as an exception
		// will be throw		
		return false;
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof JDIThisVariable) {
			return ((JDIThisVariable)o).fObject.equals(fObject);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return fObject.hashCode();
	}

}
