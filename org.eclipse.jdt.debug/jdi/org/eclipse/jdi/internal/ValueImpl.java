package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.jdwp.JdwpID;
import org.eclipse.jdi.internal.jdwp.JdwpObjectID;

import com.sun.jdi.InternalException;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public abstract class ValueImpl extends MirrorImpl implements Value {
	/**
	 * Creates new ValueImpl.
	 */
	protected ValueImpl(String description, VirtualMachineImpl vmImpl) {
		super(description, vmImpl);
	}

	/**
	 * @returns type of value.
	 */
	public abstract Type type();

	/**
	 * @returns type of value.
	 */
	public abstract byte getTag();
	
	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static ValueImpl readWithTag(MirrorImpl target, DataInputStream in) throws IOException {
		byte tag = target.readByte("object tag", JdwpID.tagMap(), in); //$NON-NLS-1$
	   	return readWithoutTag(target, tag, in);
	}
	
	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static ValueImpl readWithoutTag(MirrorImpl target, int type, DataInputStream in) throws IOException {	
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		// See also ArrayReference Impl.
		switch(type) {
			case ArrayReferenceImpl.tag:
				return ArrayReferenceImpl.read(target, in);
			case ClassLoaderReferenceImpl.tag:
				return ClassLoaderReferenceImpl.read(target, in);
			case ClassObjectReferenceImpl.tag:
				return ClassObjectReferenceImpl.read(target, in);
			case StringReferenceImpl.tag:
				return StringReferenceImpl.read(target, in);
			case ObjectReferenceImpl.tag:
				return ObjectReferenceImpl.readObjectRefWithoutTag(target, in);
			case ThreadGroupReferenceImpl.tag:
				return ThreadGroupReferenceImpl.read(target, in);
			case ThreadReferenceImpl.tag:
				return ThreadReferenceImpl.read(target, in);
			case BooleanValueImpl.tag:
				return BooleanValueImpl.read(target, in);
			case ByteValueImpl.tag:
				return ByteValueImpl.read(target, in);
			case CharValueImpl.tag:
				return CharValueImpl.read(target, in);
			case DoubleValueImpl.tag:
				return DoubleValueImpl.read(target, in);
			case FloatValueImpl.tag:
				return FloatValueImpl.read(target, in);
			case IntegerValueImpl.tag:
				return IntegerValueImpl.read(target, in);
			case LongValueImpl.tag:
				return LongValueImpl.read(target, in);
			case ShortValueImpl.tag:
				return ShortValueImpl.read(target, in);
			case VoidValueImpl.tag:
				return new VoidValueImpl(vmImpl);
			case 0:
				return null;
			default:
				throw new InternalException(JDIMessages.getString("ValueImpl.Invalid_Value_tag_encountered___1") + type); //$NON-NLS-1$
		}
	}

	/**
	 * Writes value with value tag.
	 */
	public void writeWithTag(MirrorImpl target, DataOutputStream out) throws IOException {
		target.writeByte(getTag(), "tag", JdwpID.tagMap(), out); //$NON-NLS-1$
		write(target, out);
	}

	/**
	 * Writes value without value tag.
	 */
	public abstract void write(MirrorImpl target, DataOutputStream out) throws IOException;
	
	/**
	 * Writes null value without value tag.
	 */
	public static void writeNull(MirrorImpl target, DataOutputStream out) throws IOException {
		JdwpObjectID nullID = new JdwpObjectID(target.virtualMachineImpl());
		nullID.write(out);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("objectReference", nullID.value()); //$NON-NLS-1$
	}
	
	/**
	 * Writes null value with value tag.
	 */
	public static void writeNullWithTag(MirrorImpl target, DataOutputStream out) throws IOException {
		target.writeByte(ObjectReferenceImpl.tag, "tag", JdwpID.tagMap(), out); //$NON-NLS-1$
		writeNull(target, out);
	}
}
