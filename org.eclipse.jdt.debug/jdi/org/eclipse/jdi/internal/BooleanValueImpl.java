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
public class BooleanValueImpl extends PrimitiveValueImpl implements BooleanValue {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.BOOLEAN_TAG;

	/**
	 * Creates new instance.
	 */
	public BooleanValueImpl(VirtualMachineImpl vmImpl, Boolean value) {
		super("BooleanValue", vmImpl, value);
	}
	
	/**
	 * @returns tag.
	 */
	public byte getTag() {
		return tag;
	}

	/**
	 * @returns type of value.
   	 */
	public Type type() {
		return new BooleanTypeImpl(virtualMachineImpl());
	}

	/**
	 * @returns Value.
	 */
	public boolean value() {
		return booleanValue();
	}
	
	/**
	 * @return Reads and returns new instance.
	 */
	public static BooleanValueImpl read(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		boolean value = target.readBoolean("booleanValue", in);
		return new BooleanValueImpl(vmImpl, new Boolean(value));
	}

	/**
	 * Writes value without value tag.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		target.writeBoolean(((Boolean)fValue).booleanValue(), "booleanValue", out);
	}
}
