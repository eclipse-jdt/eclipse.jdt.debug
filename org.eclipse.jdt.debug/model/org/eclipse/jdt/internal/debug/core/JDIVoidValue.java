package org.eclipse.jdt.internal.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
import java.util.Collections;
import java.util.List;
import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * Represents a value of "void"
 */
public class JDIVoidValue extends JDIValue {
	
	
	public JDIVoidValue(IJavaThread thread) {
		super(null, thread);
	}

	/**
	 * @see IDebugElement
	 */
	protected List getChildren0() {
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * @see IDebugElement
	 */
	public boolean hasChildren() {
		return false;
	}

	/**
	 * @see IValue
	 */
	public String getReferenceTypeName() {
		return "void";
	}
	
	/**
	 * @see IValue
	 */
	public String getValueString() {
		return "null";
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
		return "V";
	}

	/**
	 * @see IJavaValue
	 */
	public int getArrayLength() {
		return -1;
	}
}