package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.jdwp.JdwpID;

import com.sun.jdi.LongValue;
import com.sun.jdi.Type;

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
		super("LongValue", vmImpl, value); //$NON-NLS-1$
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
		long value = target.readLong("longValue", in); //$NON-NLS-1$
		return new LongValueImpl(vmImpl, new Long(value));
	}
	
	/**
	 * Writes value without value tag.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		target.writeLong(((Long)fValue).longValue(), "longValue", out); //$NON-NLS-1$
	}
}
