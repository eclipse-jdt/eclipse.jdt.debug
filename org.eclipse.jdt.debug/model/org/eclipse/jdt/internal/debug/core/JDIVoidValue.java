package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * Represents a value of "void"
 */
public class JDIVoidValue extends JDIValue {
	
	
	public JDIVoidValue(JDIDebugTarget target) {
		super(target, null);
	}

	protected List getVariables0() {
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * @see IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() {
		return "void"; //$NON-NLS-1$
	}
	
	/**
	 * @see IValue#getValueString()
	 */
	public String getValueString() {
		return "null"; //$NON-NLS-1$
	}

	/**
	 * @see IJavaValue#evaluateToString(IJavaThread)
	 */
	public String evaluateToString(IJavaThread thread) {
		return getValueString();
	}

	/**
	 * @see IJavaValue#getSignature()
	 */
	public String getSignature() {
		return "V"; //$NON-NLS-1$
	}

	/**
	 * @see IJavaValue#getArrayLength()
	 */
	public int getArrayLength() {
		return -1;
	}
}