package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
public class VoidTypeImpl extends TypeImpl implements VoidType {
	/**
	 * Creates new instance.
	 */
	public VoidTypeImpl(VirtualMachineImpl vmImpl) {
		super("VoidType", vmImpl, "void" , "V");
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
	 * @return Returns modifier bits.
	 */
	public int modifiers() {
		throw new InternalError("A VoidType does not have modifiers.");
	}
	
	/**
	 * @return Create a null value instance of the type.
	 */
	public Value createNullValue() {
		return new VoidValueImpl(virtualMachineImpl());
	}
}
