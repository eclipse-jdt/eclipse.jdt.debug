package org.eclipse.jdt.internal.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import com.sun.jdi.*;
import org.eclipse.debug.core.DebugException;

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
	 * Constructs a local variable wrappering the given local from
	 * in a stack frame.
	 */
	public JDILocalVariable(JDIStackFrame parent, LocalVariable local) {
		super(parent);
		fLocal= local;
	}

	/**
	 * Returns this variable's current Value.
	 */
	protected Value retrieveValue() {
		return ((JDIStackFrame) getParent()).fStackFrame.getValue(fLocal);
	}

	/**
	 * @see IDebugElement
	 */
	public String getName() throws DebugException {
		try {
			return fLocal.name();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_GET_NAME, e);
		}
		return getUnknownMessage();
	}

	/**
	 * @see IValueModification
	 */
	public void setValue(Value value) throws DebugException {
		try {
			((JDIStackFrame) getParent()).fStackFrame.setValue(fLocal, value);
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
	 * @see IVariable
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			return fLocal.typeName();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_GET_REFERENCE_TYPE, e);
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
			targetRequestFailed(ERROR_GET_SIGNATURE, e);
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

