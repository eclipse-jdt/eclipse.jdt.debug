package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.jdwp.JdwpID;

import com.sun.jdi.CharValue;
import com.sun.jdi.Type;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class CharValueImpl extends PrimitiveValueImpl implements CharValue {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.CHAR_TAG;

	/**
	 * Creates new instance.
	 */
	public CharValueImpl(VirtualMachineImpl vmImpl, Character value) {
		super("CharValue", vmImpl, value); //$NON-NLS-1$
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
		return new CharTypeImpl(virtualMachineImpl());
	}

	/**
	 * @returns Value.
	 */
	public char value() {
		return charValue();
	}
	
	/**
	 * @return Reads and returns new instance.
	 */
	public static CharValueImpl read(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		char value = target.readChar("charValue", in); //$NON-NLS-1$
		return new CharValueImpl(vmImpl, new Character(value));
	}
	
	/**
	 * Writes value without value tag.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		target.writeChar(((Character)fValue).charValue(), "charValue", out); //$NON-NLS-1$
	}
}
