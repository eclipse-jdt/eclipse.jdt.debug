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
public class ClassTypeImpl extends ReferenceTypeImpl implements ClassType {
	/** Modifier bit flag: Is public; may be accessed from outside its package. */
	public static final int MODIFIER_ACC_PUBLIC = 0x0001;
	/** Modifier bit flag: Is final; no overriding is allowed. */
	public static final int MODIFIER_ACC_FINAL = 0x0010;
	/** Modifier bit flag: Treat superclass methods specially in invokespecial. */
	public static final int MODIFIER_ACC_SUPER = 0x0020;
	/** Modifier bit flag: Is an interface. */
	public static final int MODIFIER_ACC_INTERFACE = 0x0200;
	/** Modifier bit flag: Is abstract; no implementation is provided. */
	public static final int MODIFIER_ACC_ABSTRACT = 0x0400;
	
	/** JDWP Tag. */
	public static final byte typeTag = JdwpID.TYPE_TAG_CLASS;

	/** The following are the stored results of JDWP calls. */
	private ClassTypeImpl fSuperclass = null;
	
	/**
	 * Creates new ClassTypeImpl.
	 */
	public ClassTypeImpl(VirtualMachineImpl vmImpl, JdwpClassID classID) {
		super("ClassType", vmImpl, classID);
	}
	
	/**
	 * Creates new ClassTypeImpl.
	 */
	public ClassTypeImpl(VirtualMachineImpl vmImpl, JdwpClassID classID, String signature) {
		super("ClassType", vmImpl, classID, signature);
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
		return new ClassObjectReferenceImpl(virtualMachineImpl(), new JdwpClassObjectID(virtualMachineImpl()));
	}

	/**
	 * Flushes all stored Jdwp results.
	 */
	public void flushStoredJdwpResults() {
		super.flushStoredJdwpResults();

		// For all classes that have this class cached as superclass, this cache must be undone.
		Enumeration enum = virtualMachineImpl().allCachedRefTypesEnum();
		while (enum.hasMoreElements()) {
			ReferenceTypeImpl refType = (ReferenceTypeImpl)enum.nextElement();
			if (refType instanceof ClassTypeImpl) {
				ClassTypeImpl classType = (ClassTypeImpl)refType;
				if (classType.fSuperclass != null && classType.fSuperclass.equals(this)) {
					classType.flushStoredJdwpResults();
				}
			}
		}
		
		fSuperclass = null;
	}
	
	/**
	 * @return Returns Jdwp version of given options.
	 */
	private int optionsToJdwpOptions(int options) {
		int jdwpOptions = 0;
   		if ((options & INVOKE_SINGLE_THREADED) != 0)
   			jdwpOptions |= MethodImpl.INVOKE_SINGLE_THREADED_JDWP;
   		return jdwpOptions;
	}
	
	/**
	 * @return Returns a the single non-abstract Method visible from this class that has the given name and signature.
	 */
	public Method concreteMethodByName(String name, String signature) {
		/* Recursion is used to find the method:
		 * The methods of its own (own methods() command);
		 * The methods of it's superclass.
		 */
		 
	 	Iterator methods = methods().iterator();
	 	MethodImpl method;
	 	while (methods.hasNext()) {
	 		method = (MethodImpl)methods.next();
	 		if (method.name().equals(name) && method.signature().equals(signature)) {
	 			if (method.isAbstract())
	 				return null;
	 			else
	 				return method;
	 		}
	 	}
	 	
		if (superclass() != null)
			return superclass().concreteMethodByName(name, signature);
		
		return null;
	}

	/**
	 * Invokes the specified static Method in the target VM.
	 * @return Returns a Value mirror of the invoked method's return value.
	 */
	public Value invokeMethod(ThreadReference thread, Method method, List arguments, int options) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException {
		checkVM(thread);
		checkVM(method);
		ThreadReferenceImpl threadImpl = (ThreadReferenceImpl)thread;
		MethodImpl methodImpl = (MethodImpl)method;

		// Perform some checks for IllegalArgumentException.
		if (!visibleMethods().contains(method))
			throw new IllegalArgumentException("Class does not contain given method.");
		if (method.argumentTypeNames().size() != arguments.size())
			throw new IllegalArgumentException("Number of arguments doesn't match.");
		if (method.isConstructor() || method.isStaticInitializer())
			throw new IllegalArgumentException("Method is constructor or intitializer.");
		
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			write(this, outData);
			threadImpl.write(this, outData);
			methodImpl.write(this, outData);
			
			writeInt(arguments.size(), "size", outData);
			Iterator iter = arguments.iterator();
			while(iter.hasNext()) {
				ValueImpl elt = (ValueImpl)iter.next();
				if (elt != null) {
					checkVM(elt);
					elt.writeWithTag(this, outData);
				} else {
					ValueImpl.writeNullWithTag(this, outData);
				}
			}
			
			writeInt(optionsToJdwpOptions(options),"options", MethodImpl.invokeOptionsVector(), outData);
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.CT_INVOKE_METHOD, outBytes);
			switch (replyPacket.errorCode()) {
				case JdwpReplyPacket.INVALID_METHODID:
					throw new IllegalArgumentException();
				case JdwpReplyPacket.TYPE_MISMATCH:
					throw new InvalidTypeException();
				case JdwpReplyPacket.INVALID_CLASS:
					throw new ClassNotLoadedException(name());
				case JdwpReplyPacket.INVALID_THREAD:
					throw new IncompatibleThreadStateException();
				case JdwpReplyPacket.THREAD_NOT_SUSPENDED:
					throw new IncompatibleThreadStateException();
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			ValueImpl value = ValueImpl.readWithTag(this, replyData);
			ObjectReferenceImpl exception = ObjectReferenceImpl.readObjectRefWithTag(this, replyData);
			if (exception != null)
				throw new InvocationException(exception);
			return value;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}
	
	/**
	 * Constructs a new instance of this type, using the given constructor Method in the target VM.
	 * @return Returns Mirror of this type.
	 */
	public ObjectReference newInstance(ThreadReference thread, Method method, List arguments, int options)  throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException {
		checkVM(thread);
		checkVM(method);
		ThreadReferenceImpl threadImpl = (ThreadReferenceImpl)thread;
		MethodImpl methodImpl = (MethodImpl)method;
		
		// Perform some checks for IllegalArgumentException.
		if (!methods().contains(method))
			throw new IllegalArgumentException("Class does not contain given method.");
		if (method.argumentTypeNames().size() != arguments.size())
			throw new IllegalArgumentException("Number of arguments doesn't match.");
		if (!method.isConstructor())
			throw new IllegalArgumentException("Method is not a constructor.");

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			write(this, outData);
			threadImpl.write(this, outData);
			methodImpl.write(this, outData);
			
			writeInt(arguments.size(), "size", outData);
			Iterator iter = arguments.iterator();
			while(iter.hasNext()) {
				ValueImpl elt = (ValueImpl)iter.next();
				if (elt != null) {
					checkVM(elt);
					elt.writeWithTag(this, outData);
				} else {
					ValueImpl.writeNullWithTag(this, outData);
				}
			}
			
			writeInt(optionsToJdwpOptions(options),"options", MethodImpl.invokeOptionsVector(), outData);
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.CT_NEW_INSTANCE, outBytes);
			switch (replyPacket.errorCode()) {
				case JdwpReplyPacket.INVALID_METHODID:
					throw new IllegalArgumentException();
				case JdwpReplyPacket.TYPE_MISMATCH:
					throw new InvalidTypeException();
				case JdwpReplyPacket.INVALID_CLASS:
					throw new ClassNotLoadedException(name());
				case JdwpReplyPacket.INVALID_THREAD:
					throw new IncompatibleThreadStateException();
				case JdwpReplyPacket.THREAD_NOT_SUSPENDED:
					throw new IncompatibleThreadStateException();
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			ObjectReferenceImpl object = ObjectReferenceImpl.readObjectRefWithTag(this, replyData);
			ObjectReferenceImpl exception = ObjectReferenceImpl.readObjectRefWithTag(this, replyData);
			if (exception != null)
				throw new InvocationException(exception);
			return object;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}
	
	/**
	 * Assigns a value to a static field. .
	 */
	public void setValue(Field field, Value value)  throws InvalidTypeException, ClassNotLoadedException {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			write(this, outData);
			writeInt(1, "size", outData);	// We only set one field
			checkVM(field);
			((FieldImpl)field).write(this, outData);
			
			if (value != null) {
				checkVM(value);
				((ValueImpl)value).write(this, outData);
			} else {
				ValueImpl.writeNull(this, outData);
			}
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.CT_SET_VALUES, outBytes);
			switch (replyPacket.errorCode()) {
				case JdwpReplyPacket.TYPE_MISMATCH:
					throw new InvalidTypeException();
				case JdwpReplyPacket.INVALID_CLASS:
					throw new ClassNotLoadedException(name());
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}
	
	/**
	 * @return Returns the the currently loaded, direct subclasses of this class.
	 */
	public List subclasses() {
		// Note that this information should not be cached.
		Vector subclasses = new Vector();
		Enumeration enum = virtualMachineImpl().allRefTypesEnum();
		while (enum.hasMoreElements()) {
			try {
				ReferenceTypeImpl refType = (ReferenceTypeImpl)enum.nextElement();
				if (refType instanceof ClassTypeImpl) {
					ClassTypeImpl classType = (ClassTypeImpl)refType;
					if (classType.superclass() != null && classType.superclass().equals(this))
						subclasses.add(classType);
				}
			} catch (ClassNotPreparedException e) {
				continue;
			}
		}
		return subclasses;
	}
	
	/**
	 * @return Returns the superclass of this class.
	 */
	public ClassType superclass() {
		if (fSuperclass != null)
			return fSuperclass;
			
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.CT_SUPERCLASS, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			fSuperclass = ClassTypeImpl.read(this, replyData);
			return fSuperclass;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}
	
	/*
	 * @return Reads ID and returns known ReferenceTypeImpl with that ID, or if ID is unknown a newly created ReferenceTypeImpl.
	 */
	public static ClassTypeImpl read(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpClassID ID = new JdwpClassID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("classType", ID.value());

		if (ID.isNull())
			return null;

		ClassTypeImpl mirror = (ClassTypeImpl)vmImpl.getCachedMirror(ID);
		if (mirror == null) {
			mirror = new ClassTypeImpl(vmImpl, ID);
			vmImpl.addCachedMirror(mirror);
		}
		return mirror;
	 }
	
	/*
	 * @return Reads ID and returns known ReferenceTypeImpl with that ID, or if ID is unknown a newly created ReferenceTypeImpl.
	 */
	public static ClassTypeImpl readWithSignature(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpClassID ID = new JdwpClassID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("classType", ID.value());

		String signature = target.readString("signature", in);
		if (ID.isNull())
			return null;
			
		ClassTypeImpl mirror = (ClassTypeImpl)vmImpl.getCachedMirror(ID);
		if (mirror == null) {
			mirror = new ClassTypeImpl(vmImpl, ID);
			vmImpl.addCachedMirror(mirror);
		}
		mirror.setSignature(signature);
		return mirror;
	 }
}
