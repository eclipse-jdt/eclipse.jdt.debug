package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
public class StringReferenceImpl extends ObjectReferenceImpl implements StringReference {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.STRING_TAG;

	/**
	 * Creates new StringReferenceImpl.
	 */
	public StringReferenceImpl(VirtualMachineImpl vmImpl, JdwpStringID stringID) {
		super("StringReference", vmImpl, stringID);
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
			String result = readString("value", replyData);
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
			target.fVerboseWriter.println("stringReference", ID.value());

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
