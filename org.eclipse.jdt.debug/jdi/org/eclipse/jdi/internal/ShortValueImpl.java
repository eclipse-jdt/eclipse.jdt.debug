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
public class ShortValueImpl extends PrimitiveValueImpl implements ShortValue {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.SHORT_TAG;

	/**
	 * Creates new instance.
	 */
	public ShortValueImpl(VirtualMachineImpl vmImpl, Short value) {
		super("ShortValue", vmImpl, value);
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
		return new ShortTypeImpl(virtualMachineImpl());
	}

	/**
	 * @returns Value.
	 */
	public short value() {
		return shortValue();
	}
	
	/**
	 * @return Reads and returns new instance.
	 */
	public static ShortValueImpl read(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		short value = target.readShort("shortValue", in);
		return new ShortValueImpl(vmImpl, new Short(value));
	}
	
	/**
	 * Writes value without value tag.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		target.writeShort(((Short)fValue).shortValue(), "shortValue", out);
	}
}
