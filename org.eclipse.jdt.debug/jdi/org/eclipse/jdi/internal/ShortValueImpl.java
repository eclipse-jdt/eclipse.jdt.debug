package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.jdwp.JdwpID;

import com.sun.jdi.ShortValue;
import com.sun.jdi.Type;

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
		super("ShortValue", vmImpl, value); //$NON-NLS-1$
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
		return virtualMachineImpl().getShortType();
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
		short value = target.readShort("shortValue", in); //$NON-NLS-1$
		return new ShortValueImpl(vmImpl, new Short(value));
	}
	
	/**
	 * Writes value without value tag.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		target.writeShort(((Short)fValue).shortValue(), "shortValue", out); //$NON-NLS-1$
	}
}
