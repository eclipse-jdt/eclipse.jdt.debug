package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdi.internal.jdwp.JdwpArrayID;
import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpID;
import org.eclipse.jdi.internal.jdwp.JdwpObjectID;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class ArrayTypeImpl extends ReferenceTypeImpl implements ArrayType {
	/** JDWP Tag. */
	public static final byte typeTag = JdwpID.TYPE_TAG_ARRAY;

	/**
	 * Creates new ArrayTypeImpl.
	 */
	public ArrayTypeImpl(VirtualMachineImpl vmImpl, JdwpArrayID arrayID) {
		super("ArrayType", vmImpl, arrayID);
	}

	/**
	 * Creates new ArrayTypeImpl.
	 */
	public ArrayTypeImpl(VirtualMachineImpl vmImpl, JdwpArrayID arrayID, String signature) {
		super("ArrayType", vmImpl, arrayID, signature);
	}

	/**
	 * @return Returns type tag.
	 */
	public byte typeTag() {
		return typeTag;
	}
	
	/**
	 * @return Create a null value instance of the type.
	 */
	public Value createNullValue() {
		return new ArrayReferenceImpl(virtualMachineImpl(), new JdwpObjectID(virtualMachineImpl()));
	}

	/**
	 * @return Returns the JNI signature of the components of this array class.
	 */
	public String componentSignature() {
		return signature().substring(1);
	}

	/**
	 * @return Returns the type of the array components. 
	 */
	public Type componentType() throws ClassNotLoadedException {
		return TypeImpl.create(virtualMachineImpl(), componentSignature(), classLoader());
	}

	/**
	 * @return Returns a text representation of the component type.
	 */
	public String componentTypeName() {
		return TypeImpl.signatureToName(componentSignature());
	}

	/**
	 * @return Creates and returns a new instance of this array class in the target VM. 
	 */
	public ArrayReference newInstance(int length) {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			write(this, outData);
			writeInt(length, "length", outData);
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.AT_NEW_INSTANCE, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());
	
			DataInputStream replyData = replyPacket.dataInStream();
			ArrayReferenceImpl arrayRef = (ArrayReferenceImpl)ObjectReferenceImpl.readObjectRefWithTag(this, replyData);
			return arrayRef;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}
	
	/**
	 * @return Returns a List filled with all Location objects that map to the given line number. 
	 */
	public List locationsOfLine(int line) {
		// If this reference type is an ArrayType, the returned list is always empty. 
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static ArrayTypeImpl read(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpArrayID ID = new JdwpArrayID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("arrayType", ID.value());

		if (ID.isNull())
			return null;
		
		ArrayTypeImpl mirror = (ArrayTypeImpl)vmImpl.getCachedMirror(ID);
		if (mirror == null) {
			mirror = new ArrayTypeImpl(vmImpl, ID);
			vmImpl.addCachedMirror(mirror);
		}
		return mirror;
	 }
	
	/** 
	 * @return Returns the classloader object which loaded the class corresponding to this type.
	 */
	public ClassLoaderReference classLoader() {
		return null;
	}

	/**
	 * @return Returns modifier bits.
	 */
	public int modifiers() {
		return MODIFIER_ACC_PUBLIC | MODIFIER_ACC_FINAL;
	}

	/** 
	 * @return Returns a list containing each Field declared in this type. 
	 */
	public List fields() {
		return Collections.EMPTY_LIST;
	}

	/** 
	 * @return Returns a list containing each Method declared in this type. 
	 */
	public List methods() {
		return Collections.EMPTY_LIST;
	}

	/** 
	 * @return a Map of the requested static Field objects with their Value.
	 */
	public Map getValues(List fields) {
		if (fields.isEmpty())
			return new HashMap();
			
		throw new IllegalArgumentException("getValues not allowed on array.");
	}

	/** 
	 * @return Returns a List containing each ReferenceType declared within this type. 
	 */
	public List nestedTypes() {
		return Collections.EMPTY_LIST;
	}
		
	/** 
	 * @return Returns status of class/interface.
	 */
	protected int status() { 
		return ReferenceTypeImpl.JDWP_CLASS_STATUS_INITIALIZED | ReferenceTypeImpl.JDWP_CLASS_STATUS_PREPARED | ReferenceTypeImpl.JDWP_CLASS_STATUS_VERIFIED;
	}

	/** 
	 * @return Returns the interfaces declared as implemented by this class. Interfaces indirectly implemented (extended by the implemented interface or implemented by a superclass) are not included.
	 */
	public List interfaces() {
		return Collections.EMPTY_LIST;
	}
		
	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static ArrayTypeImpl readWithSignature(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpArrayID ID = new JdwpArrayID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("arrayType", ID.value());

		String signature = target.readString("signature", in);
		if (ID.isNull())
			return null;
			
		ArrayTypeImpl mirror = (ArrayTypeImpl)vmImpl.getCachedMirror(ID);
		if (mirror == null) {
			mirror = new ArrayTypeImpl(vmImpl, ID);
			vmImpl.addCachedMirror(mirror);
		}
		mirror.setSignature(signature);
		return mirror;
	 }
}
