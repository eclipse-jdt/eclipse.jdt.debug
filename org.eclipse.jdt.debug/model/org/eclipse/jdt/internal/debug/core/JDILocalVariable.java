package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;

import com.sun.jdi.*;

/**
 * A <code>JDILocalVariable</code> represents a local variable in a stack
 * frame.
 */

public class JDILocalVariable extends JDIModificationVariable {
	/**
	 * The wrappered local variable
	 */
	protected LocalVariable fLocal;
	
	/**
	 * The stack frame the local is contained in
	 */
	protected JDIStackFrame fStackFrame;
	
	/**
	 * Constructs a local variable wrappering the given local from
	 * in a stack frame.
	 */
	public JDILocalVariable(JDIStackFrame frame, LocalVariable local) {
		super((JDIDebugTarget)frame.getDebugTarget());
		fStackFrame= frame;
		fLocal= local;
	}

	/**
	 * Returns this variable's current Value.
	 */
	protected Value retrieveValue() {
		return fStackFrame.getUnderlyingStackFrame().getValue(fLocal);
	}

	/**
	 * @see IDebugElement
	 */
	public String getName() throws DebugException {
		try {
			return fLocal.name();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDILocalVariable.exception_retrieving_local_variable_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return getUnknownMessage();
	}

	/**
	 * @see IValueModification
	 */
	public void setValue(Value value) throws DebugException {
		try {
			fStackFrame.getUnderlyingStackFrame().setValue(fLocal, value);
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDILocalVariable.exception_modifying_local_variable_value_1"), new String[] {e.toString()}), e); //$NON-NLS-1$
		} catch (InvalidTypeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDILocalVariable.exception_modifying_local_variable_value_2"), new String[] {e.toString()}), e); //$NON-NLS-1$
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDILocalVariable.exception_modifying_local_variable_value_3"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
	}
	
	/**
	 * @see IVariable
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			return fLocal.typeName();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDILocalVariable.exception_retrieving_local_variable_type_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return getUnknownMessage();
	}
	
	/**
	 * @see IJavaVariable
	 */
	public String getSignature() throws DebugException {
		try {
			return fLocal.signature();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDILocalVariable.exception_retrieving_local_variable_type_signature"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return getUnknownMessage();
	}
	
	/** 
	 * Updates this local's underlying variable. Called by enclosing stack 
	 * frame when doing an incremental update.
	 */
	protected void setLocal(LocalVariable local) {
		fLocal = local;
	}
	
	protected LocalVariable getLocal() {
		return fLocal;
	}
	
	protected VirtualMachine getVirtualMachine() {
		return fLocal.virtualMachine();
	}
}

