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
public class LongValueImpl extends PrimitiveValueImpl implements LongValue {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.LONG_TAG;

	/**
	 * Creates new instance.
	 */
	public LongValueImpl(VirtualMachineImpl vmImpl, Long value) {
		super("LongValue", vmImpl, value);
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
		return new LongTypeImpl(virtualMachineImpl());
	}

	/**
	 * @returns Value.
	 */
	public long value() {
		return longValue();
	}
	
	/**
	 * @return Reads and returns new instance.
	 */
	public static LongValueImpl read(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		long value = target.readLong("longValue", in);
		return new LongValueImpl(vmImpl, new Long(value));
	}
	
	/**
	 * Writes value without value tag.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		target.writeLong(((Long)fValue).longValue(), "longValue", out);
	}
}
