package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.ObjectReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import org.eclipse.debug.core.DebugException;

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
	public JDIThisVariable(JDIStackFrame parent, ObjectReference object) {
		super(parent);
		fObject= object;
	}

	/**
	 * Returns this variable's current Value.
	 */
	protected Value retrieveValue() {
		return fObject;
	}

	/**
	 * @see IDebugElement
	 */
	public String getName() {
		return "this";
	}
	
	/**
	 * @see IJavaVariable
	 */
	public String getSignature() throws DebugException {
		try {
			return fObject.type().signature();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_GET_SIGNATURE, e);
		}
		return getUnknownMessage();
	}

	/**
	 * @see IVariable
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			return getValue().getReferenceTypeName();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_GET_SIGNATURE, e);
		}
		return getUnknownMessage();
	}
}
