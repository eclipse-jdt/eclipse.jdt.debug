package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import com.sun.jdi.Type;

/**
 * Void type. Since it is not possible to retrieve the
 * void type from the target VM on demand, there is a
 * special implementation for the void type.
 */

public class JDIVoidType extends JDIType {

	/**
	 * Constructs a new void type for the given VM.
	 */
	protected JDIVoidType(JDIDebugTarget target) {
		super(target, null);
	}

	/**
	 * @see IJavaType#getName()
	 */
	public String getName() {
		return "void"; //$NON-NLS-1$
	}
	
	/**
	 * @see IJavaType#getSignature()
	 */
	public String getSignature() {
		return "V"; //$NON-NLS-1$
	}
}

