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
public class DoubleValueImpl extends PrimitiveValueImpl implements DoubleValue {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.DOUBLE_TAG;

	/**
	 * Creates new instance.
	 */
	public DoubleValueImpl(VirtualMachineImpl vmImpl, Double value) {
		super("DoubleValue", vmImpl, value);
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
		return new DoubleTypeImpl(virtualMachineImpl());
	}

	/**
	 * @returns Value.
	 */
	public double value() {
		return doubleValue();
	}
	
	/**
	 * @return Reads and returns new instance.
	 */
	public static DoubleValueImpl read(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		double value = target.readDouble("doubleValue", in);
		return new DoubleValueImpl(vmImpl, new Double(value));
	}
	
	/**
	 * Writes value without value tag.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		target.writeDouble(((Double)fValue).doubleValue(), "doubleValue", out);
	}
}
