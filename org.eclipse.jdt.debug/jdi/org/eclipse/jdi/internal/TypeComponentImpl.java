package org.eclipse.jdi.internal;/*
 * JDI class Implementation
 *
 * (BB)
 * (C) Copyright IBM Corp. 2000
 */



import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import org.eclipse.jdi.internal.connect.*;
import org.eclipse.jdi.internal.request.*;
import org.eclipse.jdi.internal.event.*;
import org.eclipse.jdi.internal.jdwp.*;
import org.eclipse.jdi.internal.spy.*;
import java.util.*;
import java.io.*;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public abstract class TypeComponentImpl extends AccessibleImpl implements TypeComponent {
	/** Text representation of this type. */
	private String fName = null;
	/** JNI-style signature for this type. */
	private String fSignature = null;
	/** ReferenceType that holds field or method. */
	private ReferenceTypeImpl fDeclaringType;
	/** Modifier bits. */
	protected int fModifierBits;

	/**
	 * Creates new instance.
	 */
	public TypeComponentImpl(String description, VirtualMachineImpl vmImpl, ReferenceTypeImpl declaringType, String name, String signature, int modifierBits) {
		super(description, vmImpl);
		fName = name;
		fSignature = signature;
		fDeclaringType= declaringType;
		fModifierBits = modifierBits;
	}

	/**
	 * @return Returns modifier bits.
	 */
	public int modifiers() {
		return fModifierBits;
	}
	
	/**
	 * @return Returns the ReferenceTypeImpl in which this component was declared.
	 */
 	public ReferenceTypeImpl referenceTypeImpl() {
 		return fDeclaringType;
 	}	
 	
	/**
	 * @return Returns the type in which this component was declared.
	 */
	public ReferenceType declaringType() {
 		return fDeclaringType;
	}
	
	/** 
	 * @return Returns true if type component is final.
	 */
	public boolean isFinal() {
		return (fModifierBits & MODIFIER_ACC_FINAL) != 0;
	}
	
	/** 
	 * @return Returns true if type component is static.
	 */
	public boolean isStatic() {
		return (fModifierBits & MODIFIER_ACC_STATIC) != 0;
	}
	
	/** 
	 * @return Returns true if type component is synthetic.
	 */
	public boolean isSynthetic() {
		return (fModifierBits & MODIFIER_SYNTHETIC) != 0;
	}
	
	/**
	 * @return Returns text representation of this type.
	 */
	public String name() {
		return fName;
	}

	/**
	 * @return JNI-style signature for this type.
	 */
	public String signature() {
		return fSignature;
	}
	
	/**
	 * @return Returns description of Mirror object.
	 */
	public String toString() {
		return fName;
	}
}
