package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;

import org.eclipse.jdt.debug.core.IJavaVariable;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

/**
 * Represents the receiver in a stack frame.
 */

public class JDIThisVariable extends JDIVariable {
	/**
	 * The wrappered object
	 */
	protected ObjectReference fObject;
	
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
			return fObject.type().signature();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThisVariableexception_retrieving_type_signature"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return getUnknownMessage();
	}

	/**
	 * @see IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			return getValue().getReferenceTypeName();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThisVariableexception_retrieving_reference_type_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return getUnknownMessage();
	}
}
