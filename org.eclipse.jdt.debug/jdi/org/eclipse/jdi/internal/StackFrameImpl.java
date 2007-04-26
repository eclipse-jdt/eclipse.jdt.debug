/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal;


import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpFrameID;
import org.eclipse.jdi.internal.jdwp.JdwpID;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Locatable;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMMismatchException;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class StackFrameImpl extends MirrorImpl implements StackFrame, Locatable {
	/** FrameID that corresponds to this reference. */
	private JdwpFrameID fFrameID;
	/** Thread under which this frame's method is running. */
	private ThreadReferenceImpl fThread;
	/** Location of the current instruction in the frame. */
	private LocationImpl fLocation;
	
	/**
	 * Creates new StackFrameImpl.
	 */
	public StackFrameImpl(VirtualMachineImpl vmImpl, JdwpFrameID ID, ThreadReferenceImpl thread, LocationImpl location) {
		super("StackFrame", vmImpl); //$NON-NLS-1$
		fFrameID = ID;
		fThread = thread;
		fLocation = location;
	}

	/**
	 * @return Returns the Value of a LocalVariable in this frame. 
	 */
	public Value getValue(LocalVariable variable) throws IllegalArgumentException, InvalidStackFrameException, VMMismatchException {
		ArrayList list = new ArrayList(1);
		list.add(variable);
		return (ValueImpl)getValues(list).get(variable);
	}
	
	/**
	 * @return Returns the values of multiple local variables in this frame.
	 */
	public Map getValues(List variables) throws IllegalArgumentException, InvalidStackFrameException, VMMismatchException {
		// Note that this information should not be cached.
		Map map = new HashMap(variables.size());
		// if the variable list is empty, nothing to do
		if (variables.isEmpty()) {
			return map;
		}
		/*
		 * If 'this' is requested, we have to use a special JDWP request.
		 * Therefore, we remember the positions in the list of requests for 'this'.
		 */
		int sizeAll = variables.size();
		int sizeThis = 0;
		boolean[] isThisValue = new boolean[sizeAll];
		for (int i = 0; i < sizeAll; i++) {
			LocalVariableImpl var = (LocalVariableImpl)variables.get(i);
			isThisValue[i] = var.isThis();
			if (isThisValue[i]) {
				sizeThis++;
			}
		}
		int sizeNotThis = sizeAll - sizeThis;
		
		if (sizeThis > 0) {
			Value thisValue = thisObject();
			for (int i = 0; i < sizeAll; i++) {
				if (isThisValue[i]) {
					map.put(variables.get(i), thisValue);
				}
			}
		}
		
		// If only 'this' was requested, we're finished.
		if (sizeNotThis == 0) {
			return map;
		}
			
		// Request values for local variables other than 'this'.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeWithThread(this, outData);
			writeInt(sizeNotThis, "size", outData); //$NON-NLS-1$
			for (int i = 0; i < sizeAll; i++) {
				if (!isThisValue[i]) {
					LocalVariableImpl var = (LocalVariableImpl)variables.get(i);
					checkVM(var);
					writeInt(var.slot(), "slot", outData); //$NON-NLS-1$
					writeByte(var.tag(), "tag", JdwpID.tagMap(), outData); //$NON-NLS-1$
				}
			}
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.SF_GET_VALUES, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());
		
			DataInputStream replyData = replyPacket.dataInStream();
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			if (nrOfElements != sizeNotThis) 
				throw new InternalError(JDIMessages.StackFrameImpl_Retrieved_a_different_number_of_values_from_the_VM_than_requested_1); 
			
			for (int i = 0, j = 0; i < sizeAll; i++) {
				if (!isThisValue[i])
					map.put(variables.get(j++), ValueImpl.readWithTag(this, replyData));
			}
			return map;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @see com.sun.jdi.StackFrame#getArgumentValues()
	 * @since 3.3
	 */
	public List getArgumentValues() throws InvalidStackFrameException {
		if(!thread().isSuspended()) {
			throw new InvalidStackFrameException(JDIMessages.StackFrameImpl_no_argument_values_available);
		}
		try {
			List list = location().method().variables();
			ArrayList ret = new ArrayList();
			LocalVariable var = null;
			for(Iterator iter = list.iterator(); iter.hasNext();){
				var = (LocalVariable) iter.next();
				if(var.isArgument()) {
					ret.add(getValue(var));
				}
			}
			return ret;
		} 
		catch (AbsentInformationException e) {
			JDIDebugPlugin.log(e);
			return null;
		}
	}
	
	/**
	 * @return Returns the Location of the current instruction in the frame.
	 */
	public Location location() {
		return fLocation;
	}
	
	/**
	 * Sets the Value of a LocalVariable in this frame. 
	 */
	public void setValue(LocalVariable var, Value value) throws InvalidTypeException, ClassNotLoadedException {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			((ThreadReferenceImpl)thread()).write(this, outData);
			write(this, outData);
			writeInt(1, "size", outData);	// We only set one field //$NON-NLS-1$
			checkVM(var);
			writeInt(((LocalVariableImpl)var).slot(), "slot", outData); //$NON-NLS-1$
			
			// check the type and the vm of the value, convert the value if needed.
			ValueImpl checkedValue= ValueImpl.checkValue(value, var.type(), virtualMachineImpl());
			
			if (checkedValue != null) {
				checkedValue.writeWithTag(this, outData);
			} else {
				ValueImpl.writeNullWithTag(this, outData);
			}
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.SF_SET_VALUES, outBytes);
			switch (replyPacket.errorCode()) {
				case JdwpReplyPacket.INVALID_CLASS:
					throw new ClassNotLoadedException(var.typeName());
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns the value of 'this' for the current frame.
	 */
	public ObjectReference thisObject() throws InvalidStackFrameException {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeWithThread(this, outData);
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.SF_THIS_OBJECT, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());
			
			DataInputStream replyData = replyPacket.dataInStream();
			ObjectReference result = ObjectReferenceImpl.readObjectRefWithTag(this, replyData);
			return result;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns the thread under which this frame's method is running.
	 */
	public ThreadReference thread() {
		return fThread;
	}
	
	/**
	 * @return Returns a LocalVariable that matches the given name and is visible at the current frame location.
	 */
	public LocalVariable visibleVariableByName(String name) throws AbsentInformationException {
		Iterator iter = visibleVariables().iterator();
		while (iter.hasNext()) {
			LocalVariableImpl var = (LocalVariableImpl)iter.next();
			if (var.name().equals(name)) {
				return var;
			}
		}
		
		return null;
	}
	
	/**
	 * @return Returns the values of multiple local variables in this frame. 
	 */
	public List visibleVariables() throws AbsentInformationException {
		List variables= fLocation.method().variables();
		Iterator iter = variables.iterator();
		List visibleVars = new ArrayList(variables.size());
		while (iter.hasNext()) {
			LocalVariableImpl var = (LocalVariableImpl)iter.next();
			// Only return local variables other than the this pointer.
			if (var.isVisible(this) && !var.isThis()) {
				visibleVars.add(var);
			}
		}
		return visibleVars;
	}
	
	/** 
	 * @return Returns the hash code value.
	 */
	public int hashCode() {
		return fThread.hashCode() + fFrameID.hashCode();
	}
	
	/**
	 * @return Returns true if two mirrors refer to the same entity in the target VM.
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		return object != null && object.getClass().equals(this.getClass()) && fThread.equals(((StackFrameImpl)object).fThread) && fFrameID.equals(((StackFrameImpl)object).fFrameID);
	}
	
	/**
	 * Writes JDWP representation.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		fFrameID.write(out);
		if (target.fVerboseWriter != null) {
			target.fVerboseWriter.println("stackFrame", fFrameID.value()); //$NON-NLS-1$
		}
	}
	
	/**
	 * Writes JDWP representation.
	 */
	public void writeWithThread(MirrorImpl target, DataOutputStream out) throws IOException {
		fThread.write(target, out);
		write(target, out);
	}
	
	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static StackFrameImpl readWithLocation(MirrorImpl target, ThreadReferenceImpl thread, DataInputStream in)  throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpFrameID ID = new JdwpFrameID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null) {
			target.fVerboseWriter.println("stackFrame", ID.value()); //$NON-NLS-1$
		}

		if (ID.isNull()) {
			return null;
		}
		LocationImpl location = LocationImpl.read(target, in);
		if (location == null) {
			return null;
		}

		return new StackFrameImpl(vmImpl, ID, thread, location);
	}
}
