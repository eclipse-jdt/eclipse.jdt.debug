package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.jdwp.JdwpID;

import com.sun.jdi.FloatValue;
import com.sun.jdi.Type;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class FloatValueImpl extends PrimitiveValueImpl implements FloatValue {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.FLOAT_TAG;

	/**
	 * Creates new instance.
	 */
	public FloatValueImpl(VirtualMachineImpl vmImpl, Float value) {
		super("FloatValue", vmImpl, value); //$NON-NLS-1$
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
		return new FloatTypeImpl(virtualMachineImpl());
	}

	/**
	 * @returns Value.
	 */
	public float value() {
		return floatValue();
	}
	
	/**
	 * @return Reads and returns new instance.
	 */
	public static FloatValueImpl read(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		float value = target.readFloat("floatValue", in); //$NON-NLS-1$
		return new FloatValueImpl(vmImpl, new Float(value));
	}
	
	/**
	 * Writes value without value tag.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		target.writeFloat(((Float)fValue).floatValue(), "floatValue", out); //$NON-NLS-1$
	}
}
