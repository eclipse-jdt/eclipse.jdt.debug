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
public class ClassObjectReferenceImpl extends ObjectReferenceImpl implements ClassObjectReference {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.CLASS_OBJECT_TAG;

	/**
	 * Creates new ClassObjectReferenceImpl.
	 */
	public ClassObjectReferenceImpl(VirtualMachineImpl vmImpl, JdwpClassObjectID classObjectID) {
		super("ClassObjectReference", vmImpl, classObjectID);
	}

	/**
	 * @returns Returns Value tag.
	 */
	public byte getTag() {
		return tag;
	}
	
	/**
	 * @returns Returns the ReferenceType corresponding to this class object. 
  	 */
	public ReferenceType reflectedType() {
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.COR_REFLECTED_TYPE, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			return ReferenceTypeImpl.readWithTypeTag(this, replyData);
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}
	
	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static ClassObjectReferenceImpl read(MirrorImpl target, DataInputStream in)  throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpClassObjectID ID = new JdwpClassObjectID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("classObjectReference", ID.value());

		if (ID.isNull())
			return null;

		ClassObjectReferenceImpl mirror = new ClassObjectReferenceImpl(vmImpl, ID);
		return mirror;
	}
}