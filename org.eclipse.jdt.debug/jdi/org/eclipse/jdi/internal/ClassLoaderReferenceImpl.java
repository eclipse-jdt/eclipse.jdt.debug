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
public class ClassLoaderReferenceImpl extends ObjectReferenceImpl implements ClassLoaderReference {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.CLASS_LOADER_TAG;

	/**
	 * Creates new ClassLoaderReferenceImpl.
	 */
	public ClassLoaderReferenceImpl(VirtualMachineImpl vmImpl, JdwpClassLoaderID classLoaderID) {
		super("ClassLoaderReference", vmImpl, classLoaderID);
	}

	/**
	 * @returns Value tag.
	 */
	public byte getTag() {
		return tag;
	}
	
	/**
	 * @returns Returns a list of all loaded classes that were defined by this class loader.
	 */
	public List definedClasses() {
		// Note that this information should not be cached.
		Vector result = new Vector();
		Iterator iter = visibleClasses().iterator();
		while (iter.hasNext()) {
			try {
				ReferenceType type = (ReferenceType)iter.next();
				// Note that classLoader() is null for the bootstrap classloader.
				if (type.classLoader() != null && type.classLoader().equals(this))
					result.add(type);
			} catch (ClassNotPreparedException e) {
				continue;
			}
		}
		return result;
	}

	/**
	 * @returns Returns a list of all loaded classes that were defined by this class loader.
	 */
	public List visibleClasses() {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.CLR_VISIBLE_CLASSES, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			Vector elements = new Vector();
			int nrOfElements = readInt("elements", replyData);
			for (int i = 0; i < nrOfElements; i++) {
				ReferenceTypeImpl elt = ReferenceTypeImpl.readWithTypeTag(this, replyData);
				if (elt == null)
					continue;
				elements.add(elt);
			}
			return elements;
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
	public static ClassLoaderReferenceImpl read(MirrorImpl target, DataInputStream in)  throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpClassLoaderID ID = new JdwpClassLoaderID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("classLoaderReference", ID.value());

		if (ID.isNull())
			return null;

		ClassLoaderReferenceImpl mirror = new ClassLoaderReferenceImpl(vmImpl, ID);
		return mirror;
	}
}
