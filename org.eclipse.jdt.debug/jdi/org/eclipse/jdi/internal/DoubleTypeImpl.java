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
public class DoubleTypeImpl extends PrimitiveTypeImpl implements DoubleType {
	/**
	 * Creates new instance.
	 */
	public DoubleTypeImpl(VirtualMachineImpl vmImpl) {
		super("DoubleType", vmImpl, "double" , "D");
	}
	
	/**
	 * @returns primitive type tag.
	 */
	public byte tag() {
		return DoubleValueImpl.tag;
	}
	
	/**
	 * @return Create a null value instance of the type.
	 */
	public Value createNullValue() {
		return virtualMachineImpl().mirrorOf((double)0);
	}
}
