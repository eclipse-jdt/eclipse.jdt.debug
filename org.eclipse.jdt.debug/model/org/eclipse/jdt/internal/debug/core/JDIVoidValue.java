package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Collections;
import java.util.List;

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
	 * @see IValue
	 */
	public String getReferenceTypeName() {
		return "void"; //$NON-NLS-1$
	}
	
	/**
	 * @see IValue
	 */
	public String getValueString() {
		return "null"; //$NON-NLS-1$
	}

	/**
	 * @see IJavaValue
	 */
	public String evaluateToString() {
		return getValueString();
	}

	/**
	 * @see IJavaValue
	 */
	public String getSignature() {
		return "V"; //$NON-NLS-1$
	}

	/**
	 * @see IJavaValue
	 */
	public int getArrayLength() {
		return -1;
	}
}