package org.eclipse.jdt.internal.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaType;

import com.sun.jdi.ClassObjectReference;

/**
 * An object on the target VM that is an instance of
 * <code>java.lang.Class</code>.
 * 
 * @see IJavaClassObject
 */
public class JDIClassObjectValue extends JDIObjectValue implements IJavaClassObject {

	/**
	 * Constructs a reference to a class object.
	 */
	public JDIClassObjectValue(JDIDebugTarget target, ClassObjectReference object) {
		super(target, object);
	}
	

	/**
	 * @see IJavaClassObject#getInstanceType()
	 */
	public IJavaType getInstanceType() {
		return JDIType.createType((JDIDebugTarget)getDebugTarget(),getUnderlyingClassObject().reflectedType());
	}

	/**
	 * Returns the underlying class object
	 */
	protected ClassObjectReference getUnderlyingClassObject() {
		return (ClassObjectReference)getUnderlyingValue();
	}
}

