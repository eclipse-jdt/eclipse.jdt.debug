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
import org.eclipse.jdt.debug.core.IJavaVariable;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

/**
 * A <code>JDILocalVariable</code> represents a local variable in a stack frame.
 */

public class JDILocalVariable extends JDIModificationVariable {
	/**
	 * The underlying local variable
	 */
	private LocalVariable fLocal;

	/**
	 * The stack frame the local is contained in
	 */
	private JDIStackFrame fStackFrame;

	/**
	 * Constructs a local variable for the given local in a stack frame.
	 */
	public JDILocalVariable(JDIStackFrame frame, LocalVariable local) {
		super((JDIDebugTarget) frame.getDebugTarget());
		fStackFrame = frame;
		fLocal = local;
	}

	/**
	 * Returns this variable's current Value.
	 */
	@Override
	protected Value retrieveValue() throws DebugException {
		synchronized (fStackFrame.getThread()) {
			if (getStackFrame().isSuspended()) {
				return getStackFrame().getUnderlyingStackFrame().getValue(
						fLocal);
			}
		}
		// bug 6518
		return getLastKnownValue();
	}

	/**
	 * @see IVariable#getName()
	 */
	public String getName() throws DebugException {
		try {
			return getLocal().name();
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDILocalVariable_exception_retrieving_local_variable_name,
							e.toString()), e);
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;
		}
	}

	/**
	 * @see JDIModificationVariable#setValue(Value)
	 */
	@Override
	protected void setJDIValue(Value value) throws DebugException {
		try {
			synchronized (getStackFrame().getThread()) {
				getStackFrame().getUnderlyingStackFrame().setValue(getLocal(),
						value);
			}
			fireChangeEvent(DebugEvent.CONTENT);
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDILocalVariable_exception_modifying_local_variable_value,
							e.toString()), e);
		} catch (InvalidTypeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDILocalVariable_exception_modifying_local_variable_value,
							e.toString()), e);
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDILocalVariable_exception_modifying_local_variable_value,
							e.toString()), e);
		}
	}

	/**
	 * @see IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			String genericSignature = getLocal().genericSignature();
			if (genericSignature != null) {
				return JDIReferenceType.getTypeName(genericSignature);
			}
			try {
				Type underlyingType = getUnderlyingType();
				if (underlyingType instanceof ReferenceType) {
					return JDIReferenceType
							.getGenericName((ReferenceType) underlyingType);
				}
			} catch (DebugException e) {
				if (!(e.getStatus().getException() instanceof ClassNotLoadedException)) {
					throw e;
				}
			}
			return getLocal().typeName();
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDILocalVariable_exception_retrieving_local_variable_type_name,
							e.toString()), e);
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;
		}
	}

	/**
	 * @see IJavaVariable#getSignature()
	 */
	public String getSignature() throws DebugException {
		try {
			return getLocal().signature();
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDILocalVariable_exception_retrieving_local_variable_type_signature,
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
			String genericSignature = fLocal.genericSignature();
			if (genericSignature != null) {
				return genericSignature;
			}
			return fLocal.signature();
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDILocalVariable_exception_retrieving_local_variable_type_signature,
							e.toString()), e);
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;
		}
	}

	/**
	 * Updates this local's underlying variable. Called by enclosing stack frame
	 * when doing an incremental update.
	 */
	protected void setLocal(LocalVariable local) {
		fLocal = local;
	}

	protected LocalVariable getLocal() {
		return fLocal;
	}

	protected JDIStackFrame getStackFrame() {
		return fStackFrame;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getLocal().toString();
	}

	/**
	 * @see JDIVariable#getUnderlyingType()
	 */
	@Override
	protected Type getUnderlyingType() throws DebugException {
		try {
			return getLocal().type();
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDILocalVariable_exception_while_retrieving_type_of_local_variable,
							e.toString()), e);
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDILocalVariable_exception_while_retrieving_type_of_local_variable,
							e.toString()), e);
		}
		// this line will not be executed as an exception
		// will be throw in type retrieval fails
		return null;
	}

	/**
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaVariable#isLocal()
	 */
	@Override
	public boolean isLocal() {
		return true;
	}
}
