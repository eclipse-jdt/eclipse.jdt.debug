package org.eclipse.jdt.internal.debug.core.model;

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */



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
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThisVariableexception_retrieving_type_signature"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;			
		}
	}

	/**
	 * @see IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			return getValue().getReferenceTypeName();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThisVariableexception_retrieving_reference_type_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
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
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThisVariable.exception_while_retrieving_type_this"), new String[]{e.toString()}), e); //$NON-NLS-1$
		}
		// this line will not be exceucted as an exception
		// will be throw in type retrieval fails
		return null;
	}	
	
	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPrivate()
	 */
	public boolean isPrivate() throws DebugException {
		return ((ReferenceType)getUnderlyingType()).isPrivate();
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isProtected()
	 */
	public boolean isProtected() throws DebugException {
		return ((ReferenceType)getUnderlyingType()).isProtected();
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaModifiers#isPublic()
	 */
	public boolean isPublic() throws DebugException {
		return ((ReferenceType)getUnderlyingType()).isPublic();
	}

}
