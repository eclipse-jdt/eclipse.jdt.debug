package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;

import org.eclipse.debug.core.model.IValue;
import com.sun.jdi.*;

/**
 * A <code>JDILocalVariable</code> represents a local variable in a stack
 * frame.
 */

public class JDILocalVariable extends JDIModificationVariable {
	/**
	 * The wrappered local variable
	 */
	private LocalVariable fLocal;
	
	/**
	 * The stack frame the local is contained in
	 */
	private JDIStackFrame fStackFrame;
	
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
		return getStackFrame().getUnderlyingStackFrame().getValue(fLocal);
	}

	/**
	 * @see IVariable#getName()
	 */
	public String getName() throws DebugException {
		try {
			return getLocal().name();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDILocalVariable.exception_retrieving_local_variable_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;			
		}
	}

	/**
	 * @see JDIModificationVariable#setValue(Value)
	 */
	public void setValue(Value value) throws DebugException {
		try {
			getStackFrame().getUnderlyingStackFrame().setValue(getLocal(), value);
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDILocalVariable.exception_modifying_local_variable_value_1"), new String[] {e.toString()}), e); //$NON-NLS-1$
		} catch (InvalidTypeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDILocalVariable.exception_modifying_local_variable_value_2"), new String[] {e.toString()}), e); //$NON-NLS-1$
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDILocalVariable.exception_modifying_local_variable_value_3"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
	}
	
	/**
	 * @see IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		try {
			return getLocal().typeName();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDILocalVariable.exception_retrieving_local_variable_type_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
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
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDILocalVariable.exception_retrieving_local_variable_type_signature"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception
			return null;			
		}
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
	
	protected JDIStackFrame getStackFrame() {
		return fStackFrame;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getLocal().toString();
	}
	
	/**
	 * @see IValueModification#setValue(IValue)
	 */
	public	void setValue(IValue v) throws DebugException {
		if (verifyValue(v)) {
			JDIValue value = (JDIValue)v;
			try {
				getStackFrame().getUnderlyingStackFrame().setValue(getLocal(), value.getUnderlyingValue());
			} catch (InvalidTypeException e) {
				targetRequestFailed(MessageFormat.format("{0} occurred while attempting to set value of local variable.", new String[]{e.toString()}), e);
			} catch (ClassNotLoadedException e) {
				targetRequestFailed(MessageFormat.format("{0} occurred while attempting to set value of local variable.", new String[]{e.toString()}), e);
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format("{0} occurred while attempting to set value of local variable.", new String[]{e.toString()}), e);
			}
		}
	}
}

