package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpID;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;
import org.eclipse.jdi.internal.jdwp.JdwpStringID;

import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.StringReference;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class StringReferenceImpl extends ObjectReferenceImpl implements StringReference {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.STRING_TAG;

	/**
	 * Creates new StringReferenceImpl.
	 */
	public StringReferenceImpl(VirtualMachineImpl vmImpl, JdwpStringID stringID) {
		super("StringReference", vmImpl, stringID); //$NON-NLS-1$
	}

	/**
	 * @returns Value tag.
	 */
	public byte getTag() {
		return tag;
	}
	
	/**
	 * @returns Returns the StringReference as a String. 
	 */
	public String value() {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.SR_VALUE, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			
			DataInputStream replyData = replyPacket.dataInStream();
			String result = readString("value", replyData); //$NON-NLS-1$
			return result;
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
	public static StringReferenceImpl read(MirrorImpl target, DataInputStream in)  throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpStringID ID = new JdwpStringID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("stringReference", ID.value()); //$NON-NLS-1$

		if (ID.isNull())
			return null;

		StringReferenceImpl mirror = new StringReferenceImpl(vmImpl, ID);
		return mirror;
	}
	
	/**
	 * @return Returns description of Mirror object.
	 */
	public String toString() {
		try {
			return "\"" + value() + "\"";
		} catch (ObjectCollectedException e) {
			return "(Garbage Collected) StringReference " + idString();
		} catch (Exception e) {
			return fDescription;
		}
	}
}
