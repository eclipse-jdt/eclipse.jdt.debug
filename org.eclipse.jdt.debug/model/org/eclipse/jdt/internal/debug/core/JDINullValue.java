package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import com.sun.jdi.Type;
import java.util.Collections;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;

/**
 * Represents a value of "null"
 */
public class JDINullValue extends JDIValue {
	
	
	public JDINullValue(JDIDebugTarget target) {
		super(target, null);
	}

	protected List getVariables0() {
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * @see IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() {
		return "null"; //$NON-NLS-1$
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
		return null;
	}

	/**
	 * @see IJavaValue#getArrayLength()
	 */
	public int getArrayLength() {
		return -1;
	}
		
	/**
	 * @see IJavaValue#getJavaType()
	 */
	public IJavaType getJavaType() throws DebugException {
		return null;
	}
}