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
public class BooleanTypeImpl extends PrimitiveTypeImpl implements BooleanType {
	/**
	 * Creates new instance.
	 */
	public BooleanTypeImpl(VirtualMachineImpl vmImpl) {
		super("BooleanType", vmImpl, "boolean" , "Z");
	}
	
	/**
	 * @returns primitive type tag.
	 */
	public byte tag() {
		return BooleanValueImpl.tag;
	}
	
	/**
	 * @return Create a null value instance of the type.
	 */
	public Value createNullValue() {
		return virtualMachineImpl().mirrorOf(false);
	}
}
